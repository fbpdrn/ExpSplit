import Foundation

enum APIProfile {

    static func get(token: String) async throws -> ProfileResponse {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("profile"))
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        switch httpResponse.statusCode {
        case 200:
            return try JSONDecoder().decode(ProfileResponse.self, from: data)
        case 404:
            throw ProfileError.notFound
        default:
            throw URLError(.badServerResponse)
        }
    }

    static func create(token: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("profile"))
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }

    static func update(token: String, firstName: String, lastName: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("profile"))
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONEncoder().encode(UpdateRequest(firstName: firstName, lastName: lastName))

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }

    private struct UpdateRequest: Encodable {
        let firstName: String
        let lastName: String
    }
    
    enum ProfileError: Error {
        case notFound
    }

    struct GroupSummary: Decodable, Identifiable, Hashable {
        let id: UUID
        let name: String
    }

    struct ProfileResponse: Decodable {
        let id: UUID
        let email: String
        let firstName: String?
        let lastName: String?
        let groups: [GroupSummary]
    }
}
