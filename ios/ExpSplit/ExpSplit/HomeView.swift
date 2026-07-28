import SwiftUI
import UIKit

struct HomeView: View {
    let onLogout: () -> Void

    private enum HomeTab {
        case groups
        case invitations
    }

    @State private var isLoading = true
    @State private var needsProfile = false
    @State private var isEditingProfile = false
    @State private var profile: APIProfile.ProfileResponse?
    @State private var errorMessage: String?

    @State private var selectedTab: HomeTab = .groups
    @State private var invitations: [APIInvitation.PendingInvitation] = []

    @State private var showCreateGroupSheet = false
    @State private var newGroupName = ""
    @State private var isCreatingGroup = false
    @State private var createGroupError: String?

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

                Button {
                    newGroupName = ""
                    createGroupError = nil
                    showCreateGroupSheet = true
                } label: {
                    Image(systemName: "plus")
                        .padding(10)
                        .background(Circle().fill(Color(.secondarySystemBackground)))
                }
            }
            .padding(.horizontal)

            Picker("Sezione", selection: $selectedTab) {
                Text("Gruppi").tag(HomeTab.groups)
                Text("Inviti").tag(HomeTab.invitations)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)

            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            switch selectedTab {
            case .groups:
                groupsList
            case .invitations:
                invitationsList
            }
        }
        .padding(.top)
        .navigationDestination(for: APIProfile.GroupSummary.self) { group in
            GroupDetailView(groupId: group.id, groupName: group.name, currentUserId: profile?.id)
        }
        .sheet(isPresented: $showCreateGroupSheet) {
            createGroupSheet
        }
        .task {
            await loadInvitations()
        }
    }

    private var createGroupSheet: some View {
        VStack(spacing: 16) {
            Text("Nuovo gruppo")
                .font(.title2)

            ESTextField(title: "Nome gruppo", text: $newGroupName)

            if let createGroupError {
                Text(createGroupError)
                    .foregroundStyle(.red)
                    .font(.footnote)
            }

            ESButton(title: "Crea", isLoading: isCreatingGroup) {
                Task { await createGroup() }
            }
            .disabled(isCreatingGroup || newGroupName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)

            Button("Annulla") {
                showCreateGroupSheet = false
            }
        }
        .padding()
        .presentationDetents([.medium])
    }

    private var groupsList: some View {
        Group {
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
    }

    private var invitationsList: some View {
        Group {
            if invitations.isEmpty {
                Text("Nessun invito")
                    .foregroundStyle(.secondary)
                Spacer()
            } else {
                List(invitations) { invitation in
                    HStack {
                        Text(invitation.groupName)
                        Spacer()
                        Button {
                            Task { await respondToInvitation(invitation, accept: true) }
                        } label: {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundStyle(.green)
                        }
                        .buttonStyle(.plain)

                        Button {
                            Task { await respondToInvitation(invitation, accept: false) }
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(.red)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
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

    private func loadInvitations() async {
        guard let token = AuthStorage.loadToken() else { return }
        do {
            invitations = try await APIInvitation.list(token: token)
        } catch {
            // Non blocchiamo la home se gli inviti non si caricano.
        }
    }

    private func respondToInvitation(_ invitation: APIInvitation.PendingInvitation, accept: Bool) async {
        guard let token = AuthStorage.loadToken() else { return }
        do {
            if accept {
                try await APIGroup.acceptInvitation(groupId: invitation.groupId, token: token)
            } else {
                try await APIGroup.rejectInvitation(groupId: invitation.groupId, token: token)
            }
            await loadInvitations()
            await loadProfile()
        } catch {
            errorMessage = "Impossibile aggiornare l'invito."
        }
    }

    private func createGroup() async {
        guard let token = AuthStorage.loadToken() else {
            createGroupError = "Sessione non valida."
            return
        }

        createGroupError = nil
        isCreatingGroup = true
        defer { isCreatingGroup = false }

        do {
            _ = try await APIGroup.create(name: newGroupName, token: token)
            showCreateGroupSheet = false
            await loadProfile()
        } catch {
            createGroupError = "Creazione non riuscita."
        }
    }
}

#Preview {
    HomeView(onLogout: {})
}
