package com.kathayat.netomi.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kathayat.netomi.presentation.ui.ChatListScreen
import com.kathayat.netomi.presentation.ui.ChatRoomScreen

@Composable
fun NavigationGraph(
    innerPadding: PaddingValues,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "chat_list"
    ) {
        composable("chat_list") {
            ChatListScreen(
                innerPaddingValues = innerPadding,
                onChatOpen = { chatId ->
                    navController.navigate("chat_room/$chatId")
                }
            )
        }

        composable(
            route = "chat_room/{chatId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.IntType }
            )
        ) { entry ->

            val chatId = entry.arguments?.getInt("chatId") ?: 0

            ChatRoomScreen(
                innerPaddingValues = innerPadding,
                chatId = chatId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
