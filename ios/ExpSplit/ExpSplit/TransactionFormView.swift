import SwiftUI

struct TransactionFormView: View {
    let groupId: UUID
    let members: [APIGroup.Membership]
    let existingTransaction: APITransaction.Transaction?
    let onSaved: () -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var description: String
    @State private var amount: String
    @State private var category: APITransaction.Category
    @State private var selectedMemberIds: Set<UUID>
    @State private var shares: [UUID: Double]
    @State private var isSaving = false
    @State private var errorMessage: String?

    init(groupId: UUID, members: [APIGroup.Membership], existingTransaction: APITransaction.Transaction? = nil,
         onSaved: @escaping () -> Void) {
        self.groupId = groupId
        self.members = members
        self.existingTransaction = existingTransaction
        self.onSaved = onSaved

        if let transaction = existingTransaction {
            _description = State(initialValue: transaction.description)
            _amount = State(initialValue: "\(transaction.amount)")
            _category = State(initialValue: APITransaction.Category(rawValue: transaction.category) ?? .other)
            _selectedMemberIds = State(initialValue: Set(transaction.shares.map { $0.user.id }))
            var initialShares: [UUID: Double] = [:]
            for share in transaction.shares {
                initialShares[share.user.id] = (share.percentage as NSDecimalNumber).doubleValue
            }
            _shares = State(initialValue: initialShares)
        } else {
            _description = State(initialValue: "")
            _amount = State(initialValue: "")
            _category = State(initialValue: .other)
            _selectedMemberIds = State(initialValue: [])
            _shares = State(initialValue: [:])
        }
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Dettagli") {
                    TextField("Descrizione", text: $description)
                    TextField("Importo", text: $amount)
                        .keyboardType(.decimalPad)
                    Picker("Categoria", selection: $category) {
                        ForEach(APITransaction.Category.allCases) { category in
                            Text(category.displayName).tag(category)
                        }
                    }
                }

                Section("Partecipanti") {
                    ForEach(members) { member in
                        Toggle(member.user.displayName, isOn: bindingForSelection(member.user.id))
                    }
                }

                if !selectedMemberIds.isEmpty {
                    Section("Ripartizione") {
                        ForEach(members.filter { selectedMemberIds.contains($0.user.id) }) { member in
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(member.user.displayName)
                                    Spacer()
                                    Text(shareDisplayText(for: member.user.id))
                                        .foregroundStyle(.secondary)
                                }
                                Slider(value: bindingForShare(member.user.id), in: 0...100)
                            }
                        }
                    }
                }

                if let errorMessage {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                }
            }
            .navigationTitle(existingTransaction == nil ? "Nuova transazione" : "Modifica transazione")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Annulla") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Salva") {
                        Task { await save() }
                    }
                    .disabled(isSaving || description.isEmpty || amount.isEmpty || selectedMemberIds.isEmpty)
                }
            }
        }
    }

    private func shareDisplayText(for userId: UUID) -> String {
        let percentage = Int((shares[userId] ?? 0).rounded())
        guard let amountValue = Decimal(string: amount) else {
            return "\(percentage)%"
        }
        let share = amountValue * Decimal(percentage) / 100
        return "\(share) € (\(percentage)%)"
    }

    private func bindingForSelection(_ userId: UUID) -> Binding<Bool> {
        Binding(
            get: { selectedMemberIds.contains(userId) },
            set: { _ in toggleMember(userId) }
        )
    }

    private func bindingForShare(_ userId: UUID) -> Binding<Double> {
        Binding(
            get: { shares[userId] ?? 0 },
            set: { adjustShares(changedId: userId, newValue: $0) }
        )
    }

    private func toggleMember(_ userId: UUID) {
        if selectedMemberIds.contains(userId) {
            selectedMemberIds.remove(userId)
            shares[userId] = nil
        } else {
            selectedMemberIds.insert(userId)
        }
        equalizeShares()
    }

    private func equalizeShares() {
        guard !selectedMemberIds.isEmpty else { return }
        let equalShare = 100.0 / Double(selectedMemberIds.count)
        for id in selectedMemberIds {
            shares[id] = equalShare
        }
    }

    /// Muovendo lo slider di un utente, la differenza viene compensata sugli altri selezionati
    /// in proporzione al loro valore attuale, così da mantenere la somma sempre a 100.
    private func adjustShares(changedId: UUID, newValue: Double) {
        let others = selectedMemberIds.filter { $0 != changedId }
        guard !others.isEmpty else {
            shares[changedId] = 100
            return
        }

        let clampedNewValue = min(max(newValue, 0), 100)
        let oldValue = shares[changedId] ?? 0
        let delta = clampedNewValue - oldValue
        shares[changedId] = clampedNewValue

        let othersTotal = others.reduce(0.0) { $0 + (shares[$1] ?? 0) }

        if othersTotal > 0 {
            for otherId in others {
                let otherValue = shares[otherId] ?? 0
                let proportion = otherValue / othersTotal
                shares[otherId] = max(0, otherValue - delta * proportion)
            }
        } else if delta < 0 {
            let addPerOther = -delta / Double(others.count)
            for otherId in others {
                shares[otherId] = addPerOther
            }
        }

        normalizeShares()
    }

    /// Corregge piccoli errori di arrotondamento accumulati, così che la somma resti esattamente 100.
    private func normalizeShares() {
        let total = selectedMemberIds.reduce(0.0) { $0 + (shares[$1] ?? 0) }
        guard total > 0 else { return }
        let diff = 100 - total
        if let maxId = selectedMemberIds.max(by: { (shares[$0] ?? 0) < (shares[$1] ?? 0) }) {
            shares[maxId] = (shares[maxId] ?? 0) + diff
        }
    }

    private func save() async {
        guard let token = AuthStorage.loadToken() else {
            errorMessage = "Sessione non valida."
            return
        }
        guard let amountValue = Decimal(string: amount) else {
            errorMessage = "Importo non valido."
            return
        }

        let shareInputs: [APITransaction.ShareInput] = selectedMemberIds.compactMap { id in
            guard let percentage = shares[id] else { return nil }
            return APITransaction.ShareInput(userId: id, percentage: Decimal(percentage))
        }

        guard !shareInputs.isEmpty else {
            errorMessage = "Seleziona almeno un partecipante."
            return
        }

        errorMessage = nil
        isSaving = true
        defer { isSaving = false }

        do {
            if let existingTransaction {
                try await APITransaction.update(groupId: groupId, transactionId: existingTransaction.id,
                                                 description: description, amount: amountValue,
                                                 category: category, shares: shareInputs, token: token)
            } else {
                try await APITransaction.create(groupId: groupId, description: description, amount: amountValue,
                                                 category: category, shares: shareInputs, token: token)
            }
            onSaved()
            dismiss()
        } catch {
            errorMessage = existingTransaction == nil ? "Creazione non riuscita." : "Modifica non riuscita."
        }
    }
}

#Preview {
    TransactionFormView(groupId: UUID(), members: [], onSaved: {})
}
