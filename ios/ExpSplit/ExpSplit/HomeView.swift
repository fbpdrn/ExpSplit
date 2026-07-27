import SwiftUI
import UIKit

struct HomeView: View {
    let onLogout: () -> Void

    @State private var isLoading = true
    @State private var needsProfile = false
    @State private var isEditingProfile = false
    @State private var profile: APIProfile.ProfileResponse?
    @State private var errorMessage: String?

    @State private var firstName = ""
    @State private var lastName = ""
    @State private var isSavingProfile = false

    var body: some View {
        Group {
            if isLoading {
                ProgressView()
            } else if needsProfile || isEditingProfile {
                profileForm
            } else {
                groupList
            }
        }
        .task {
            await loadProfile()
        }
    }

    private var groupList: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                Button {
                    firstName = profile?.firstName ?? ""
                    lastName = profile?.lastName ?? ""
                    isEditingProfile = true
                } label: {
                    Image(systemName: "person")
                        .padding(10)
                        .background(Circle().fill(Color(.secondarySystemBackground)))
                }

                Button(role: .destructive) {
                    AuthStorage.deleteToken()
                    onLogout()
                } label: {
                    Image(systemName: "rectangle.portrait.and.arrow.right")
                        .padding(10)
                        .background(Circle().fill(Color(.secondarySystemBackground)))
                }

                Spacer()
            }
            .padding(.horizontal)

            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            if let groups = profile?.groups, !groups.isEmpty {
                List(groups) { group in
                    Text(group.name)
                }
            } else {
                Text("Nessun gruppo")
                    .foregroundStyle(.secondary)
                Spacer()
            }
        }
        .padding(.top)
    }

    private var profileForm: some View {
        VStack(spacing: 16) {
            Text(needsProfile ? "Completa il profilo" : "Modifica profilo")
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

            ESButton(title: "Salva", isLoading: isSavingProfile) {
                Task { await saveProfile() }
            }
            .disabled(isSavingProfile || firstName.isEmpty || lastName.isEmpty)

            if isEditingProfile {
                Button("Annulla") {
                    isEditingProfile = false
                }
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

    private func loadProfile() async {
        guard let token = AuthStorage.loadToken() else {
            errorMessage = "Sessione non valida."
            isLoading = false
            return
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let loaded = try await APIProfile.get(token: token)
            profile = loaded
            needsProfile = false
        } catch APIProfile.ProfileError.notFound {
            needsProfile = true
        } catch {
            errorMessage = "Impossibile caricare il profilo."
        }
    }

    private func saveProfile() async {
        guard let token = AuthStorage.loadToken() else { return }

        errorMessage = nil
        isSavingProfile = true
        defer { isSavingProfile = false }

        do {
            if needsProfile {
                try await APIProfile.create(token: token)
            }
            try await APIProfile.update(token: token, firstName: firstName, lastName: lastName)
            needsProfile = false
            isEditingProfile = false
            await loadProfile()
        } catch {
            errorMessage = "Salvataggio non riuscito."
        }
    }
}

#Preview {
    HomeView(onLogout: {})
}
