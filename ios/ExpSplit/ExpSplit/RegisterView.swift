import SwiftUI

struct RegisterView: View {
    let onRegister: () -> Void
    let onSwitchToLogin: () -> Void

    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        VStack(spacing: 16) {
            Text("Registrati")
                .font(.largeTitle)

            ESTextField(title: "Email", text: $email, keyboardType: .emailAddress)
            ESTextFieldPassword(title: "Password", text: $password)
            ESTextFieldPassword(title: "Conferma password", text: $confirmPassword)

            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            ESButton(title: "Registrati", isLoading: isLoading) {
                Task { await register() }
            }
            .disabled(isLoading || email.isEmpty || password.isEmpty || confirmPassword.isEmpty)

            Button("Hai già un account? Accedi", action: onSwitchToLogin)
        }
        .padding()
    }

    private func register() async {
        guard password == confirmPassword else {
            errorMessage = "Le password non coincidono."
            return
        }

        errorMessage = nil
        isLoading = true
        defer { isLoading = false }

        do {
            try await APIAuth.register(email: email, password: password)
            onRegister()
        } catch {
            errorMessage = "Registrazione non riuscita."
        }
    }
}

#Preview {
    RegisterView(onRegister: {}, onSwitchToLogin: {})
}
