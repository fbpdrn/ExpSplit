import SwiftUI

struct GroupDetailView: View {
    let groupId: UUID
    let groupName: String
    let currentUserId: UUID?

    private enum Tab {
        case transactions
        case members
        case balance
        case statistics
    }

    @State private var selectedTab: Tab = .transactions
    @State private var group: APIGroup.GroupDetail?
    @State private var transactions: [APITransaction.Transaction] = []
    @State private var balance: [APIBalance.GroupBalanceEntry] = []
    @State private var myBalance: APIBalance.MyBalance?
    @State private var statistics: [APIStatistics.CategoryStat] = []
    @State private var isLoading = true
    @State private var errorMessage: String?

    @State private var showInviteSheet = false
    @State private var inviteUserId = ""
    @State private var isInviting = false
    @State private var inviteError: String?

    @State private var showAddMenu = false
    @State private var showTransactionSheet = false
    @State private var showSettlementSheet = false
    @State private var editingTransaction: APITransaction.Transaction?

    var body: some View {
        VStack(spacing: 0) {
            Picker("Sezione", selection: $selectedTab) {
                Text("Transazioni").tag(Tab.transactions)
                Text("Membri").tag(Tab.members)
                Text("Bilancio").tag(Tab.balance)
                Text("Statistiche").tag(Tab.statistics)
            }
            .pickerStyle(.segmented)
            .padding()

            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if let errorMessage {
                Spacer()
                Text(errorMessage)
                    .foregroundStyle(.red)
                Spacer()
            } else {
                switch selectedTab {
                case .transactions:
                    transactionsList
                case .members:
                    membersList
                case .balance:
                    balanceView
                case .statistics:
                    statisticsList
                }
            }
        }
        .navigationTitle(groupName)
        .toolbar {
            if selectedTab == .members {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        inviteUserId = ""
                        inviteError = nil
                        showInviteSheet = true
                    } label: {
                        Image(systemName: "person.badge.plus")
                    }
                }
            } else if selectedTab == .transactions {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showAddMenu = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
        }
        .confirmationDialog("Aggiungi", isPresented: $showAddMenu) {
            Button("Nuova transazione") { showTransactionSheet = true }
            Button("Nuovo pagamento") { showSettlementSheet = true }
            Button("Annulla", role: .cancel) {}
        }
        .sheet(isPresented: $showInviteSheet) {
            inviteSheet
        }
        .sheet(isPresented: $showTransactionSheet) {
            TransactionFormView(groupId: groupId, members: group?.members ?? []) {
                Task { await load() }
            }
        }
        .sheet(item: $editingTransaction) { transaction in
            TransactionFormView(groupId: groupId, members: group?.members ?? [], existingTransaction: transaction) {
                Task { await load() }
            }
        }
        .sheet(isPresented: $showSettlementSheet) {
            SettlementFormView(groupId: groupId, members: group?.members ?? []) {
                Task { await load() }
            }
        }
        .task {
            await load()
        }
    }

    private var inviteSheet: some View {
        VStack(spacing: 16) {
            Text("Invita un utente")
                .font(.title2)

            ESTextField(title: "ID utente", text: $inviteUserId)

            if let inviteError {
                Text(inviteError)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            ESButton(title: "Invita", isLoading: isInviting) {
                Task { await sendInvite() }
            }
            .disabled(isInviting || inviteUserId.isEmpty)

            Button("Annulla") {
                showInviteSheet = false
            }
        }
        .padding()
        .presentationDetents([.medium])
    }

    private var membersList: some View {
        List(group?.members ?? []) { member in
            VStack(alignment: .leading, spacing: 6) {
                Text(displayName(for: member.user))
                HStack(spacing: 6) {
                    ESBadge(text: member.role, color: roleColor(member.role))
                    ESBadge(text: member.status, color: statusColor(member.status))
                }
            }
        }
    }

    private var transactionsList: some View {
        Group {
            if transactions.isEmpty {
                Spacer()
                Text("Nessuna transazione")
                    .foregroundStyle(.secondary)
                Spacer()
            } else {
                List(transactions) { transaction in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(transaction.description)
                        Text("\(displayName(for: transaction.paidBy)) · \(transaction.amount) · \(transaction.category)")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        if canModify(transaction) {
                            editingTransaction = transaction
                        }
                    }
                    .swipeActions {
                        if canModify(transaction) {
                            Button(role: .destructive) {
                                Task { await deleteTransaction(transaction) }
                            } label: {
                                Label("Elimina", systemImage: "trash")
                            }
                        }
                    }
                }
            }
        }
    }

    private var balanceView: some View {
        List {
            if let myBalance {
                Section("Il mio saldo") {
                    HStack {
                        Text("Totale")
                        Spacer()
                        Text("\(myBalance.netAmount)")
                            .foregroundStyle(myBalance.netAmount >= 0 ? .green : .red)
                    }
                    ForEach(myBalance.perCounterpart) { comparison in
                        HStack {
                            Text(displayName(for: comparison.counterpart))
                            Spacer()
                            Text("\(comparison.netAmount)")
                                .foregroundStyle(comparison.netAmount >= 0 ? .green : .red)
                        }
                    }
                }
            }

            Section("Saldi del gruppo") {
                ForEach(balance) { entry in
                    HStack {
                        Text(displayName(for: entry.user))
                        Spacer()
                        Text("\(entry.netAmount)")
                            .foregroundStyle(entry.netAmount >= 0 ? .green : .red)
                    }
                }
            }
        }
    }

    private var statisticsList: some View {
        Group {
            if statistics.isEmpty {
                Spacer()
                Text("Nessuna statistica disponibile")
                    .foregroundStyle(.secondary)
                Spacer()
            } else {
                List(statistics) { stat in
                    HStack {
                        Text(stat.category.displayName)
                        Spacer()
                        VStack(alignment: .trailing) {
                            Text("\(stat.totalAmount) €")
                            Text("\(stat.transactionCount) transazioni")
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
        }
    }

    private func roleColor(_ role: String) -> Color {
        role == "OWNER" ? .orange : .blue
    }

    private func statusColor(_ status: String) -> Color {
        status == "ACCEPTED" ? .green : .yellow
    }

    private func canModify(_ transaction: APITransaction.Transaction) -> Bool {
        guard let currentUserId else { return false }
        if transaction.paidBy.id == currentUserId { return true }
        return group?.members.first(where: { $0.user.id == currentUserId })?.role == "OWNER"
    }

    private func displayName(for user: APIGroup.UserSummary) -> String {
        if let firstName = user.firstName, let lastName = user.lastName {
            return "\(firstName) \(lastName)"
        }
        return user.email
    }

    private func load() async {
        guard let token = AuthStorage.loadToken() else {
            errorMessage = "Sessione non valida."
            isLoading = false
            return
        }

        isLoading = true
        defer { isLoading = false }

        do {
            async let groupDetail = APIGroup.get(groupId: groupId, token: token)
            async let transactionList = APITransaction.list(groupId: groupId, token: token)
            async let balanceList = APIBalance.list(groupId: groupId, token: token)
            async let myBalanceResult = APIBalance.me(groupId: groupId, token: token)
            async let statisticsList = APIStatistics.list(groupId: groupId, token: token)
            group = try await groupDetail
            transactions = try await transactionList
            balance = try await balanceList
            myBalance = try await myBalanceResult
            statistics = try await statisticsList
        } catch {
            errorMessage = "Impossibile caricare il gruppo."
        }
    }

    private func deleteTransaction(_ transaction: APITransaction.Transaction) async {
        guard let token = AuthStorage.loadToken() else { return }
        do {
            try await APITransaction.delete(groupId: groupId, transactionId: transaction.id, token: token)
            await load()
        } catch {
            errorMessage = "Impossibile eliminare la transazione."
        }
    }

    private func sendInvite() async {
        guard let token = AuthStorage.loadToken() else {
            inviteError = "Sessione non valida."
            return
        }
        guard let userId = UUID(uuidString: inviteUserId.trimmingCharacters(in: .whitespacesAndNewlines)) else {
            inviteError = "ID utente non valido."
            return
        }

        inviteError = nil
        isInviting = true
        defer { isInviting = false }

        do {
            try await APIGroup.invite(groupId: groupId, userId: userId, token: token)
            showInviteSheet = false
            await load()
        } catch {
            inviteError = (error as? LocalizedError)?.errorDescription ?? "Invito non riuscito."
        }
    }
}

#Preview {
    NavigationStack {
        GroupDetailView(groupId: UUID(), groupName: "Gruppo", currentUserId: nil)
    }
}
