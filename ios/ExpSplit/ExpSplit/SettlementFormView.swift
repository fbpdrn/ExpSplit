import SwiftUI

struct SettlementFormView: View {
    let groupId: UUID
    let members: [APIGroup.Membership]
    let onCreated: () -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var payeeId: UUID?
    @State private var amount = ""
    @State private var category: APITransaction.Category = .other
    @State private var isSaving = false
    @State private var errorMessage: String?

    var body: some View {
        NavigationStack {
            Form {
                Section("Dettagli") {
                    Picker("Destinatario", selection: $payeeId) {
                        Text("Seleziona...").tag(UUID?.none)
                        ForEach(members) { member in
                            Text(member.user.displayName).tag(Optional(member.user.id))
                        }
                    }
                    TextField("Importo", text: $amount)
                        .keyboardType(.decimalPad)
                    Picker("Categoria", selection: $category) {
                        ForEach(APITransaction.Category.allCases) { category in
                            Text(category.displayName).tag(category)
                        }
                    }
                }

                if let errorMessage {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                }
            }
            .navigationTitle("Nuovo pagamento")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Annulla") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Salva") {
                        Task { await save() }
                    }
                    .disabled(isSaving || payeeId == nil || amount.isEmpty)
                }
            }
        }
    }

    private func save() async {
        guard let token = AuthStorage.loadToken() else {
            errorMessage = "Sessione non valida."
            return
        }
        guard let payeeId else {
            errorMessage = "Seleziona un destinatario."
            return
        }
        guard let amountValue = Decimal(string: amount) else {
            errorMessage = "Importo non valido."
            return
        }

        errorMessage = nil
        isSaving = true
        defer { isSaving = false }

        do {
            try await APISettlement.create(groupId: groupId, payeeId: payeeId, amount: amountValue, category: category, token: token)
            onCreated()
            dismiss()
        } catch {
            errorMessage = "Creazione non riuscita."
        }
    }
}

#Preview {
    SettlementFormView(groupId: UUID(), members: [], onCreated: {})
}
