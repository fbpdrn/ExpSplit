import SwiftUI

struct ContentView: View {
    @State private var showHome = false

    var body: some View {
        if showHome {
            HomeView()
        } else {
            IntroView {
                showHome = true
            }
        }
    }
}

#Preview {
    ContentView()
}
