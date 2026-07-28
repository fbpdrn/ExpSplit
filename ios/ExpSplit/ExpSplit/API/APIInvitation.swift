import Foundation

enum APIInvitation {

    struct PendingInvitation: Decodable, Identifiable {
        let groupId: UUID
        let groupName: String
        let invitedAt: Date

        var id: UUID { groupId }
    }

    static func list(token: String) async throws -> [PendingInvitation] {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("invitations"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try APIClient.jsonDecoder.decode([PendingInvitation].self, from: data)
    }
}
