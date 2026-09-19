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

    static func formerMemberBalances(currentBalances: [GroupBalanceEntry], transactions: [APITransaction.Transaction],
                                     settlements: [APISettlement.Settlement], relativeTo userId: UUID? = nil) -> [GroupBalanceEntry] {
        let currentIds = Set(currentBalances.map { $0.user.id })
        var users: [UUID: APIGroup.UserSummary] = [:]
        var amounts: [UUID: Decimal] = [:]

        func add(_ user: APIGroup.UserSummary, _ amount: Decimal) {
            guard !currentIds.contains(user.id) else { return }
            users[user.id] = user
            amounts[user.id, default: .zero] += amount
        }

        for transaction in transactions {
            if userId == nil { add(transaction.paidBy, .zero) }
            for share in transaction.shares where share.user.id != transaction.paidBy.id {
                var rawAmount = transaction.amount * share.percentage / 100
                var owed = Decimal.zero
                // Match the service's per-share HALF_UP rounding to two decimal places.
                NSDecimalRound(&owed, &rawAmount, 2, .plain)
                if let userId {
                    if transaction.paidBy.id == userId { add(share.user, owed) }
                    if share.user.id == userId { add(transaction.paidBy, -owed) }
                } else {
                    add(transaction.paidBy, owed)
                    add(share.user, -owed)
                }
            }
        }

        for settlement in settlements {
            if let userId {
                if settlement.payer.id == userId { add(settlement.payee, settlement.amount) }
                if settlement.payee.id == userId { add(settlement.payer, -settlement.amount) }
            } else {
                add(settlement.payer, settlement.amount)
                add(settlement.payee, -settlement.amount)
            }
        }

        return users.values.map { GroupBalanceEntry(user: $0, netAmount: amounts[$0.id, default: .zero]) }
            .filter { userId == nil || $0.netAmount != .zero }
            .sorted {
                let comparison = $0.user.displayName.localizedStandardCompare($1.user.displayName)
                return comparison == .orderedSame ? $0.id.uuidString < $1.id.uuidString : comparison == .orderedAscending
            }
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
