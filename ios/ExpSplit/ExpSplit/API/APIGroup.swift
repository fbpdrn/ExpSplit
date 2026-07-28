import Foundation

enum APIGroup {

    struct UserSummary: Decodable {
        let id: UUID
        let email: String
        let firstName: String?
        let lastName: String?

        var displayName: String {
            if let firstName, let lastName {
                return "\(firstName) \(lastName)"
            }
            return email
        }
    }

    struct Membership: Decodable, Identifiable {
        let user: UserSummary
        let role: String
        let status: String
        let invitedAt: Date
        let joinedAt: Date?

        var id: UUID { user.id }
    }

    struct GroupDetail: Decodable {
        let id: UUID
        let name: String
        let members: [Membership]
        let createdAt: Date
    }

    enum InviteError: LocalizedError {
        case notOwner
        case userNotFound
        case alreadyMember

        var errorDescription: String? {
            switch self {
            case .notOwner:
                return "Solo il proprietario del gruppo può invitare nuovi membri."
            case .userNotFound:
                return "Nessun utente trovato con questo ID."
            case .alreadyMember:
                return "L'utente è già membro del gruppo."
            }
        }
    }

    static func get(groupId: UUID, token: String) async throws -> GroupDetail {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try APIClient.jsonDecoder.decode(GroupDetail.self, from: data)
    }

    static func invite(groupId: UUID, userId: UUID, token: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/invitations"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONEncoder().encode(InviteRequest(userId: userId))

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        switch httpResponse.statusCode {
        case 200:
            return
        case 403:
            throw InviteError.notOwner
        case 404:
            throw InviteError.userNotFound
        case 409:
            throw InviteError.alreadyMember
        default:
            throw URLError(.badServerResponse)
        }
    }

    static func acceptInvitation(groupId: UUID, token: String) async throws {
        try await respondToInvitation(groupId: groupId, path: "accept", token: token)
    }

    static func rejectInvitation(groupId: UUID, token: String) async throws {
        try await respondToInvitation(groupId: groupId, path: "reject", token: token)
    }

    private static func respondToInvitation(groupId: UUID, path: String, token: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/invitations/\(path)"))
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }

    private struct InviteRequest: Encodable {
        let userId: UUID
    }
}
