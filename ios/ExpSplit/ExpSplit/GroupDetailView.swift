import SwiftUI

struct GroupDetailView: View {
    let groupId: UUID
    let groupName: String

    private enum Tab {
        case members
        case transactions
    }

    @State private var selectedTab: Tab = .members
    @State private var group: APIGroup.GroupDetail?
    @State private var transactions: [APITransaction.Transaction] = []
    @State private var isLoading = true
    @State private var errorMessage: String?

    @State private var showInviteSheet = false
    @State private var inviteUserId = ""
    @State private var isInviting = false
    @State private var inviteError: String?

    @State private var showAddMenu = false
    @State private var showTransactionSheet = false
    @State private var showSettlementSheet = false

    var body: some View {
        VStack(spacing: 0) {
            Picker("Sezione", selection: $selectedTab) {
                Text("Membri").tag(Tab.members)
                Text("Transazioni").tag(Tab.transactions)
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
                case .members:
                    membersList
                case .transactions:
                    transactionsList
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
            group = try await groupDetail
            transactions = try await transactionList
        } catch {
            errorMessage = "Impossibile caricare il gruppo."
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
        GroupDetailView(groupId: UUID(), groupName: "Gruppo")
    }
}
