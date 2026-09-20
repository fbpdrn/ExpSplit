import SwiftUI

struct ESMovementIcon: View {
    enum Kind {
        case transaction
        case settlement

        var symbol: String {
            switch self {
            case .transaction: return "doc.text.fill"
            case .settlement: return "banknote.fill"
            }
        }

        var color: Color {
            switch self {
            case .transaction: return .indigo
            case .settlement: return .teal
            }
        }

        var label: String {
            switch self {
            case .transaction: return "Transazione"
            case .settlement: return "Pagamento"
            }
        }
    }

    let kind: Kind
    @ScaledMetric(relativeTo: .body) private var size = 40.0

    var body: some View {
        Image(systemName: kind.symbol)
            .font(.system(size: size * 0.45, weight: .semibold))
            .symbolRenderingMode(.hierarchical)
            .foregroundStyle(kind.color)
            .frame(width: size, height: size)
            .background(kind.color.opacity(0.12), in: RoundedRectangle(cornerRadius: size * 0.3))
            .accessibilityLabel(kind.label)
    }
}