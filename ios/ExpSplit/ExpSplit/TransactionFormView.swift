import SwiftUI

struct TransactionFormView: View {
    let groupId: UUID
    let members: [APIGroup.Membership]
    let onCreated: () -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var description = ""
    @State private var amount = ""
    @State private var category: APITransaction.Category = .other
    @State private var shares: [UUID: String] = [:]
    @State private var isSaving = false
    @State private var errorMessage: String?

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

                Section("Ripartizione (%)") {
                    ForEach(members) { member in
                        HStack {
                            Text(member.user.displayName)
                            Spacer()
                            TextField("%", text: bindingForShare(member.user.id))
                                .keyboardType(.decimalPad)
                                .multilineTextAlignment(.trailing)
                                .frame(width: 60)
                        }
                    }
                }

                if let errorMessage {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                }
            }
            .navigationTitle("Nuova transazione")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Annulla") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Salva") {
                        Task { await save() }
                    }
                    .disabled(isSaving || description.isEmpty || amount.isEmpty)
                }
            }
            .onAppear(perform: setEqualSplit)
        }
    }

    private func bindingForShare(_ userId: UUID) -> Binding<String> {
        Binding(
            get: { shares[userId] ?? "" },
            set: { shares[userId] = $0 }
        )
    }

    private func setEqualSplit() {
        guard !members.isEmpty, shares.isEmpty else { return }
        let equalShare = 100.0 / Double(members.count)
        let value = String(format: "%.2f", equalShare)
        for member in members {
            shares[member.user.id] = value
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

        let shareInputs: [APITransaction.ShareInput] = members.compactMap { member in
            guard let percentageString = shares[member.user.id],
                  let percentage = Decimal(string: percentageString) else {
                return nil
            }
            return APITransaction.ShareInput(userId: member.user.id, percentage: percentage)
        }

        guard !shareInputs.isEmpty else {
            errorMessage = "Inserisci almeno una quota."
            return
        }

        errorMessage = nil
        isSaving = true
        defer { isSaving = false }

        do {
            try await APITransaction.create(groupId: groupId, description: description, amount: amountValue,
                                             category: category, shares: shareInputs, token: token)
            onCreated()
            dismiss()
        } catch {
            errorMessage = "Creazione non riuscita."
        }
    }
}

#Preview {
    TransactionFormView(groupId: UUID(), members: [], onCreated: {})
}
