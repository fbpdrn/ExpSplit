import SwiftUI

struct IntroView: View {
    let onAdvance: () -> Void

    var body: some View {
        VStack {
            Spacer()
            Text("ExpSplit")
                .font(.largeTitle)
            Spacer()
            ESButton(title: "Avanti", action: onAdvance)
        }
        .padding()
    }
}

#Preview {
    IntroView(onAdvance: {})
}
