import Foundation

enum APIStatistics {

    struct CategoryStat: Decodable, Identifiable {
        let category: APITransaction.Category
        let totalAmount: Decimal
        let transactionCount: Int

        var id: String { category.rawValue }
    }

    static func list(groupId: UUID, token: String) async throws -> [CategoryStat] {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/statistics"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try APIClient.jsonDecoder.decode([CategoryStat].self, from: data)
    }
}
