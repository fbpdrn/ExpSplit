import Foundation

enum APISettlement {

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

    private struct CreateRequest: Encodable {
        let payeeId: UUID
        let amount: Decimal
        let category: APITransaction.Category?
    }
}
