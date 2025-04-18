package com.example.flashcardapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5D2316))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp) //
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FlashcardsApp",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Default.Place,
                        contentDescription = "Local",
                        tint = Color.Red
                    )
                    Text(
                        text = "sala",
                        color = Color.White
                    )
                    Icon(
                        imageVector = Default.KeyboardArrowRight,
                        contentDescription = "Ir",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatusCard(
                icon = Default.FlashOn,
                iconColor = Color.Yellow,
                value = "0%",
                label = "Taxa de acertos"
            )
            Spacer(modifier = Modifier.height(8.dp))

            StatusCard(
                icon = Default.Whatshot,
                iconColor = Color.Red,
                value = "0",
                label = "Dias seguidos de estudo"
            )
            Spacer(modifier = Modifier.height(8.dp))

            StatusCard(
                icon = Default.EmojiEvents,
                iconColor = Color(0xFFFFD700),
                value = "0",
                label = "Record de dias"
            )
        }

        // FAB
        FloatingActionButton(
            onClick = { /* ação ao clicar */ },
            containerColor = Color(0xFF2E3B00),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Default.Add, contentDescription = "Adicionar")
        }
    }
}

@Composable
fun StatusCard(icon: ImageVector, iconColor: Color, value: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7A2F1B)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow,
                    fontSize = 16.sp
                )
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

