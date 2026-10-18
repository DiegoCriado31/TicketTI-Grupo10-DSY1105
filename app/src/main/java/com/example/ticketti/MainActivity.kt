package com.example.ticketti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ticketti.navigation.AppNav
import com.example.ticketti.ui.theme.TicketTITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicketTITheme {
                AppNav()
            }
        }
    }
}