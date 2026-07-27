import SwiftUI

struct ESTextFieldPassword: View {
    let title: String
    @Binding var text: String

    var body: some View {
        SecureField(title, text: $text)
            .textFieldStyle(.roundedBorder)
    }
}

#Preview {
    ESTextFieldPassword(title: "Password", text: .constant(""))
        .padding()
}
