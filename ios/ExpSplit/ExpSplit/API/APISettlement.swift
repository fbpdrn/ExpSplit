import Foundation

enum APISettlement {

    struct Settlement: Decodable, Identifiable {
        let id: UUID
        let groupId: UUID
        let payer: APIGroup.UserSummary
        let payee: APIGroup.UserSummary
        let amount: Decimal
        let category: APITransaction.Category?
        let createdAt: Date
    }

    static func list(groupId: UUID, token: String) async throws -> [Settlement] {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/settlements"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try APIClient.jsonDecoder.decode([Settlement].self, from: data)
    }

    static func create(groupId: UUID, payeeId: UUID, amount: Decimal, category: APITransaction.Category?, token: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/settlements"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONEncoder().encode(CreateRequest(payeeId: payeeId, amount: amount, category: category))

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }

    static func delete(groupId: UUID, settlementId: UUID, token: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/settlements/\(settlementId.uuidString)"))
        request.httpMethod = "DELETE"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 204 else {
            throw URLError(.badServerResponse)
        }
    }

    private struct CreateRequest: Encodable {
        let payeeId: UUID
        let amount: Decimal
        let category: APITransaction.Category?
    }
}
