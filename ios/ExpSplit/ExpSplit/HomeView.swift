import SwiftUI
import UIKit

struct HomeView: View {
    let onLogout: () -> Void

    @State private var isLoading = true
    @State private var needsProfile = false
    @State private var isEditingProfile = false
    @State private var profile: APIProfile.ProfileResponse?
    @State private var errorMessage: String?

    var body: some View {
        Group {
            if isLoading {
                ProgressView()
            } else if needsProfile || isEditingProfile {
                ProfileFormView(
                    profile: profile,
                    onSaved: {
                        isEditingProfile = false
                        Task {
                            await loadProfile()
                        }
                    },
                    onCancel: isEditingProfile ? { isEditingProfile = false } : nil
                )
            } else {
                groupList
            }
        }
        .task {
            await loadProfile()
        }
    }

    private var groupList: some View {
        NavigationStack {
            groupListContent
        }
    }

    private var groupListContent: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                Button {
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
                    NavigationLink(group.name, value: group)
                }
            } else {
                Text("Nessun gruppo")
                    .foregroundStyle(.secondary)
                Spacer()
            }
        }
        .padding(.top)
        .navigationDestination(for: APIProfile.GroupSummary.self) { group in
            GroupDetailView(groupId: group.id, groupName: group.name)
        }
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
}

#Preview {
    HomeView(onLogout: {})
}
