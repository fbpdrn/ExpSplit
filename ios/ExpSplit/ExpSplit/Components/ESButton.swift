import SwiftUI

struct ESButton: View {
    let title: String
    var isLoading: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            if isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity)
            } else {
                Text(title)
                    .frame(maxWidth: .infinity)
            }
        }
        .buttonStyle(.borderedProminent)
        .disabled(isLoading)
    }
}

#Preview {
    VStack(spacing: 12) {
        ESButton(title: "Continua", action: {})
        ESButton(title: "Caricamento...", isLoading: true, action: {})
    }
    .padding()
}
