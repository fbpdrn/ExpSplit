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
}
