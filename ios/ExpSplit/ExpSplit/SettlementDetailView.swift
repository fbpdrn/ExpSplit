import SwiftUI

struct SettlementDetailView: View {
    let settlement: APISettlement.Settlement

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("Pagamento") {
                    LabeledContent("Pagatore", value: settlement.payer.displayName)
                    LabeledContent("Destinatario", value: settlement.payee.displayName)
                    LabeledContent("Importo", value: settlement.amount.formatted(.currency(code: "EUR")))
                    LabeledContent("Categoria", value: settlement.category?.displayName ?? "Altro")
                    LabeledContent(
                        "Data",
                        value: settlement.createdAt.formatted(date: .abbreviated, time: .shortened)
                    )
                }
            }
            .navigationTitle("Dettaglio pagamento")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Chiudi") { dismiss() }
                }
            }
        }
    }
}