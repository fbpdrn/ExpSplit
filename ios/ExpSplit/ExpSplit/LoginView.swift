import SwiftUI

struct LoginView: View {
    let onLogin: () -> Void
    let onSwitchToRegister: () -> Void

    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        VStack(spacing: 16) {
            Text("Accedi")
                .font(.largeTitle)

            ESTextField(title: "Email", text: $email, keyboardType: .emailAddress)
            ESTextFieldPassword(title: "Password", text: $password)

            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            ESButton(title: "Accedi", isLoading: isLoading) {
                Task { await login() }
            }
            .disabled(isLoading || email.isEmpty || password.isEmpty)

            Button("Non hai un account? Registrati", action: onSwitchToRegister)
        }
        .padding()
    }

    private func login() async {
        errorMessage = nil
        isLoading = true
        defer { isLoading = false }

        do {
            try await APIAuth.login(email: email, password: password)
            onLogin()
        } catch {
            errorMessage = "Accesso non riuscito. Controlla le credenziali."
        }
    }
}

#Preview {
    LoginView(onLogin: {}, onSwitchToRegister: {})
}
