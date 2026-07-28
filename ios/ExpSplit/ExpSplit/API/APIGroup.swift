import Foundation

enum APIGroup {

    struct UserSummary: Decodable {
        let id: UUID
        let email: String
        let firstName: String?
        let lastName: String?
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
}
