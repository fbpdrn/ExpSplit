import SwiftUI
import UIKit

struct ProfileFormView: View {
    let profile: APIProfile.ProfileResponse?
    let onSaved: () -> Void
    let onCancel: (() -> Void)?

    @State private var firstName: String
    @State private var lastName: String
    @State private var isSaving = false
    @State private var errorMessage: String?

    init(profile: APIProfile.ProfileResponse?, onSaved: @escaping () -> Void, onCancel: (() -> Void)? = nil) {
        self.profile = profile
        self.onSaved = onSaved
        self.onCancel = onCancel
        _firstName = State(initialValue: profile?.firstName ?? "")
        _lastName = State(initialValue: profile?.lastName ?? "")
    }

    var body: some View {
        VStack(spacing: 16) {
            Text(profile == nil ? "Completa il profilo" : "Modifica profilo")
                .font(.largeTitle)

            VStack(spacing: 12) {
                ESTextField(title: "Nome", text: $firstName)
                ESTextField(title: "Cognome", text: $lastName)
            }

            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            ESButton(title: "Salva", isLoading: isSaving) {
                Task { await save() }
            }
            .disabled(isSaving || firstName.isEmpty || lastName.isEmpty)

            if let onCancel {
                Button("Annulla", action: onCancel)
            }

            if let profile {
                Divider()

                VStack(alignment: .leading, spacing: 4) {
                    Text("ID utente")
                        .font(.footnote)
                        .foregroundStyle(.secondary)

                    HStack {
                        Text(profile.id.uuidString)
                            .font(.footnote.monospaced())

                        Spacer()

                        Button {
                            UIPasteboard.general.string = profile.id.uuidString
                        } label: {
                            Image(systemName: "doc.on.doc")
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding()
    }

    private func save() async {
        guard let token = AuthStorage.loadToken() else { return }

        errorMessage = nil
        isSaving = true
        defer { isSaving = false }

        do {
            if profile == nil {
                try await APIProfile.create(token: token)
            }
            try await APIProfile.update(token: token, firstName: firstName, lastName: lastName)
            onSaved()
        } catch {
            errorMessage = "Salvataggio non riuscito."
        }
    }
}

#Preview {
    ProfileFormView(profile: nil, onSaved: {})
}
