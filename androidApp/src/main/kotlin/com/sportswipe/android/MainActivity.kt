package com.sportswipe.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportswipe.shared.SportSwipeSdk
import com.sportswipe.shared.data.local.DriverFactory
import com.sportswipe.shared.domain.model.DiscoverItem
import com.sportswipe.shared.domain.model.Objective
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sdk = SportSwipeSdk(DriverFactory(this))
        MainScope().launch { sdk.init() }
        val discoverVm = sdk.discoverViewModel()
        val likesVm = sdk.likesViewModel()
        val chatsVm = sdk.chatsViewModel()
        val exploreVm = sdk.exploreViewModel()
        val profileVm = sdk.profileViewModel()

        setContent {
            MaterialTheme {
                var tab by remember { mutableStateOf(0) }
                Scaffold(bottomBar = {
                    NavigationBar {
                        listOf("Descubrir", "Me gusta", "Chats", "Explorar", "Perfil").forEachIndexed { i, t ->
                            NavigationBarItem(selected = tab == i, onClick = { tab = i }, label = { Text(t) }, icon = {})
                        }
                    }
                }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        when (tab) {
                            0 -> DiscoverScreen(discoverVm.state.value.items.getOrNull(discoverVm.state.value.currentIndex), onLike = { discoverVm.likeCurrent() }, onNope = { discoverVm.nopeCurrent() })
                            1 -> LikesScreen(likesVm.state.value.isPremium, likesVm.state.value.profiles.map { it.name })
                            2 -> ChatsScreen(chatsVm.state.value.matches.map { "Match #${it.id}" })
                            3 -> ExploreScreen(exploreVm.state.value.recommended.map { it.name }) { exploreVm.openGroup(Objective.TRAIN_TODAY) }
                            else -> ProfileScreen(profileVm.state.value.settings.isPremium, onPremium = { profileVm.togglePremium(it) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverScreen(item: DiscoverItem?, onLike: () -> Unit, onNope: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (item) {
                is DiscoverItem.User -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().size(300.dp).background(
                            Brush.linearGradient(listOf(Color(0xFF87CEEB), Color(0xFF6A5ACD)))
                        ).padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Text("${item.profile.name}, ${item.profile.age}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(item.profile.bio, color = Color.White)
                        Text("Etiquetas: ${item.profile.activityTags.joinToString()} ", color = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onNope) { Text("Nope") }
                        Button(onClick = onLike) { Text("Like") }
                        Button(onClick = {}) { Text("Rewind") }
                    }
                }
                is DiscoverItem.Ad -> {
                    Text("AD: ${item.ad.title}")
                    Text(item.ad.description)
                    Button(onClick = {}) { Text(item.ad.cta) }
                }
                null -> Text("Sin perfiles")
            }
        }
    }
}

@Composable
fun LikesScreen(isPremium: Boolean, likes: List<String>) {
    Column(Modifier.padding(16.dp)) {
        Text("Modo Premium: $isPremium")
        likes.forEach { name -> Text(if (isPremium) name else "Perfil oculto") }
    }
}

@Composable
fun ChatsScreen(matches: List<String>) { Column(Modifier.padding(16.dp)) { matches.forEach { Text(it) } } }

@Composable
fun ExploreScreen(recommended: List<String>, onGroup: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text("Recomendado")
        recommended.forEach { Text(it) }
        Button(onClick = onGroup) { Text("Entrenar hoy") }
    }
}

@Composable
fun ProfileScreen(isPremium: Boolean, onPremium: (Boolean) -> Unit) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Premium")
        Switch(checked = isPremium, onCheckedChange = onPremium)
    }
}
