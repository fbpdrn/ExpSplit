import SwiftUI

private enum Screen {
    case intro
    case login
    case register
    case home
}

struct ContentView: View {
    @State private var screen: Screen = .intro

    var body: some View {
        switch screen {
        case .intro:
            IntroView {
                screen = .login
            }
        case .login:
            LoginView(
                onLogin: { screen = .home },
                onSwitchToRegister: { screen = .register }
            )
        case .register:
            RegisterView(
                onRegister: { screen = .login },
                onSwitchToLogin: { screen = .login }
            )
        case .home:
            HomeView(onLogout: { screen = .login })
        }
    }
}

#Preview {
    ContentView()
}
