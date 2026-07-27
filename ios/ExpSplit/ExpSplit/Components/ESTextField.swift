import SwiftUI

struct ESTextField: View {
    let title: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        TextField(title, text: $text)
            .textFieldStyle(.roundedBorder)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .keyboardType(keyboardType)
    }
}

#Preview {
    ESTextField(title: "Email", text: .constant(""), keyboardType: .emailAddress)
        .padding()
}
