package com.example.flashcardapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5D2316))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
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
                        imageVector = Icons.Default.Place,
                        contentDescription = "Local",
                        tint = Color.Red
                    )
                    Text(
                        text = "sala",
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Ir",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatusCard(
                icon = Icons.Default.FlashOn,
                iconColor = Color.Yellow,
                value = "0%",
                label = "Taxa de acertos"
            )
            Spacer(modifier = Modifier.height(8.dp))

            StatusCard(
                icon = Icons.Default.Whatshot,
                iconColor = Color.Red,
                value = "0",
                label = "Dias seguidos de estudo"
            )
            Spacer(modifier = Modifier.height(8.dp))

            StatusCard(
                icon = Icons.Default.EmojiEvents,
                iconColor = Color(0xFFFFD700),
                value = "0",
                label = "Recorde de dias"
            )
        }

        FloatingMenuButton(navController)
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

@Composable
fun FloatingMenuButton(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(visible = expanded) {
                ExtendedFloatingActionButton(
                    onClick = {
                        expanded = false
                        navController.navigate("add_flashcard")
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Adicionar FlashCard") },
                    text = { Text("Adicionar FlashCard") },
                    containerColor = Color(0xFF2E3B00),
                    contentColor = Color.White
                )
            }

            AnimatedVisibility(visible = expanded) {
                ExtendedFloatingActionButton(
                    onClick = {
                        expanded = false
                        navController.navigate("add_deck")
                    },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Novo Deck") },
                    text = { Text("Novo Deck") },
                    containerColor = Color(0xFF2E3B00),
                    contentColor = Color.White
                )
            }

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = Color(0xFF2E3B00),
                contentColor = Color.White,
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Menu"
                )
            }
        }
    }
}


