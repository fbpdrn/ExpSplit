import Foundation

enum APIClient {
    static let baseURL = URL(string: "https://localhost:8080")!

    static let session: URLSession = {
        #if DEBUG
        URLSession(configuration: .default, delegate: TrustAnySessionDelegate(), delegateQueue: nil)
        #else
        URLSession(configuration: .default)
        #endif
    }()

    static let jsonDecoder: JSONDecoder = {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .custom { decoder in
            let container = try decoder.singleValueContainer()
            let dateString = try container.decode(String.self)

            let withFractionalSeconds = ISO8601DateFormatter()
            withFractionalSeconds.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
            if let date = withFractionalSeconds.date(from: dateString) {
                return date
            }

            let plain = ISO8601DateFormatter()
            plain.formatOptions = [.withInternetDateTime]
            if let date = plain.date(from: dateString) {
                return date
            }

            throw DecodingError.dataCorruptedError(in: container, debugDescription: "Formato data non valido: \(dateString)")
        }
        return decoder
    }()
}
