import Foundation

enum APIAuth {

    @discardableResult
    static func login(email: String, password: String) async throws -> String {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("auth/login"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(LoginRequest(email: email, password: password))

        let (data, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        let decoded = try JSONDecoder().decode(LoginResponse.self, from: data)
        AuthStorage.saveToken(decoded.token)
        AuthStorage.saveLastEmail(email)
        return decoded.token
    }

    static func register(email: String, password: String) async throws {
        var request = URLRequest(url: APIClient.baseURL.appendingPathComponent("auth/register"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(RegisterRequest(email: email, password: password))

        let (_, response) = try await APIClient.session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
    }

    private struct LoginRequest: Encodable {
        let email: String
        let password: String
    }

    private struct LoginResponse: Decodable {
        let token: String
    }

    private struct RegisterRequest: Encodable {
        let email: String
        let password: String
    }
}
