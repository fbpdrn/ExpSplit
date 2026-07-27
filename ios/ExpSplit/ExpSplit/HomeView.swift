import SwiftUI

struct HomeView: View {
    @State private var isLoading = true
    @State private var needsProfile = false
    @State private var groups: [APIProfile.GroupSummary] = []
    @State private var errorMessage: String?

    @State private var firstName = ""
    @State private var lastName = ""
    @State private var isSavingProfile = false

    var body: some View {
        Group {
            if isLoading {
                ProgressView()
            } else if needsProfile {
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
            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            if groups.isEmpty {
                Text("Nessun gruppo")
                    .foregroundStyle(.secondary)
            } else {
                List(groups) { group in
                    Text(group.name)
                }
            }
        }
    }

    private var profileForm: some View {
        VStack(spacing: 16) {
            Text("Completa il profilo")
                .font(.largeTitle)

            ESTextField(title: "Nome", text: $firstName)
            ESTextField(title: "Cognome", text: $lastName)

            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            ESButton(title: "Salva", isLoading: isSavingProfile) {
                Task { await saveProfile() }
            }
            .disabled(isSavingProfile || firstName.isEmpty || lastName.isEmpty)
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
            let profile = try await APIProfile.get(token: token)
            groups = profile.groups
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
            try await APIProfile.create(token: token)
            try await APIProfile.update(token: token, firstName: firstName, lastName: lastName)
            needsProfile = false
            await loadProfile()
        } catch {
            errorMessage = "Salvataggio non riuscito."
        }
    }
}

#Preview {
    HomeView()
}
