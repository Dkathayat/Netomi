package com.kathayat.netomi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.kathayat.netomi.presentation.navigation.NavigationGraph
import com.kathayat.netomi.presentation.vm.ChatListViewModel
import com.kathayat.netomi.ui.theme.NetomiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val viewModel: ChatListViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetomiTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    NavigationGraph(innerPadding )
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        // NOT clear on rotation only if activity is actually finishing
        if (isChangingConfigurations) return
        if (isFinishing) {
            viewModel.clearAllChatsOnClose()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Some devices never call isFinishing correctly;
        if (!isChangingConfigurations) {
            viewModel.clearAllChatsOnClose()
        }
    }
}


