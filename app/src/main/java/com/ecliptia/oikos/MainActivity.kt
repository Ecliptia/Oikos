package com.ecliptia.oikos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ecliptia.oikos.ui.theme.OikosTheme
import com.ecliptia.oikos.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val financialStatus by themeViewModel.financialStatus.collectAsStateWithLifecycle()

            OikosTheme(financialStatus = financialStatus) {
                AppNavigation()
            }
        }
    }
}