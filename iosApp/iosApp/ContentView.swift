import SwiftUI

struct ContentView: View {
    @State private var tab: Int = 0

    var body: some View {
        TabView(selection: $tab) {
            Text("Descubrir").tabItem { Text("Descubrir") }.tag(0)
            Text("Me gusta").tabItem { Text("Likes") }.tag(1)
            Text("Chats").tabItem { Text("Chats") }.tag(2)
            Text("Explorar").tabItem { Text("Explorar") }.tag(3)
            Text("Perfil").tabItem { Text("Perfil") }.tag(4)
        }
    }
}
