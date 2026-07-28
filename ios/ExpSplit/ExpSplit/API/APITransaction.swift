import Foundation

enum APITransaction {

    enum Category: String, CaseIterable, Identifiable, Codable {
        case food = "FOOD"
        case transport = "TRANSPORT"
        case accommodation = "ACCOMMODATION"
        case entertainment = "ENTERTAINMENT"
        case utilities = "UTILITIES"
        case other = "OTHER"

        var id: String { rawValue }

        var displayName: String {
            switch self {
            case .food: return "Cibo"
            case .transport: return "Trasporti"
            case .accommodation: return "Alloggio"
            case .entertainment: return "Intrattenimento"
            case .utilities: return "Utenze"
            case .other: return "Altro"
            }
        }
    }

    struct ShareInput: Encodable {
        let userId: UUID
        let percentage: Decimal
    }

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

    static func create(groupId: UUID, description: String, amount: Decimal, category: Category?,
                        shares: [ShareInput], token: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("groups/\(groupId.uuidString)/transactions"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONEncoder().encode(CreateRequest(description: description, amount: amount, category: category, shares: shares))

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }

    private struct CreateRequest: Encodable {
        let description: String
        let amount: Decimal
        let category: Category?
        let shares: [ShareInput]
    }
}
