import Foundation

enum APIBalance {

    struct GroupBalanceEntry: Decodable, Identifiable {
        let user: APIGroup.UserSummary
        let netAmount: Decimal

        var id: UUID { user.id }
    }

    struct Comparison: Decodable, Identifiable {
        let counterpart: APIGroup.UserSummary
        let netAmount: Decimal

        var id: UUID { counterpart.id }
    }

    struct MyBalance: Decodable {
        let user: APIGroup.UserSummary
        let netAmount: Decimal
        let perCounterpart: [Comparison]
    }

    static func list(groupId: UUID, token: String) async throws -> [GroupBalanceEntry] {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/balance"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try APIClient.jsonDecoder.decode([GroupBalanceEntry].self, from: data)
    }

    static func me(groupId: UUID, token: String) async throws -> MyBalance {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/balance/me"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try APIClient.jsonDecoder.decode(MyBalance.self, from: data)
    }
}
