import Foundation

enum APITransaction {

    struct Share: Decodable, Identifiable {
        let user: APIGroup.UserSummary
        let percentage: Decimal

        var id: UUID { user.id }
    }

    struct Transaction: Decodable, Identifiable {
        let id: UUID
        let groupId: UUID
        let paidBy: APIGroup.UserSummary
        let description: String
        let amount: Decimal
        let category: String
        let shares: [Share]
        let createdAt: Date
    }

    static func list(groupId: UUID, token: String) async throws -> [Transaction] {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/transactions"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try APIClient.jsonDecoder.decode([Transaction].self, from: data)
    }
}
