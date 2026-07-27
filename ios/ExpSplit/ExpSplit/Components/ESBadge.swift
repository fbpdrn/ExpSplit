import SwiftUI

struct ESBadge: View {
    let text: String
    var color: Color = .gray

    var body: some View {
        Text(text)
            .font(.caption2.bold())
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(color.opacity(0.15))
            .foregroundStyle(color)
            .clipShape(Capsule())
    }
}

#Preview {
    HStack {
        ESBadge(text: "OWNER", color: .orange)
        ESBadge(text: "MEMBER", color: .blue)
        ESBadge(text: "ACCEPTED", color: .green)
        ESBadge(text: "PENDING", color: .yellow)
    }
    .padding()
}
