package com.example.flashcardapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel
import kotlin.math.roundToInt


// Definindo as cores da nova paleta
private val GreenPrimary = Color(0xFF4CAF50) // Verde principal
private val GreenDark = Color(0xFF388E3C) // Verde escuro para elementos de destaque
private val GreenLight = Color(0xFFC8E6C9) // Verde claro para fundos secundários
private val BlueDark = Color(0xFF1A237E) // Azul profundo para contraste
private val GreenPale = Color(0xFFE8F5E9) // Verde pálido para fundos
private val AmberAccent = Color(0xFFFFAB00) // Âmbar para elementos interativos
private val GrayLight = Color(0xFFF5F5F5) // Cinza claro para áreas de texto
private val GrayDark = Color(0xFF424242) // Cinza escuro para textos principais
private val RedError = Color(0xFFEF5350) // Vermelho para erros
private val BlueInfo = Color(0xFF42A5F5) // Azul para informações

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    val userStatsState by viewModel.userStatsUiState.collectAsState()

    Scaffold(
        containerColor = GreenPale,
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas de Estudo", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreenPale)
                .padding(paddingValues)
        ) {
            if (userStatsState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GreenDark
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dias consecutivos
                    StatsCard(
                        title = "Dias Consecutivos",
                        value = userStatsState.streakDays.toString(),
                        subtitle = "Melhor sequência: ${userStatsState.maxStreakDays} dias",
                        valueColor = AmberAccent
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Taxa de acerto
                    StatsCard(
                        title = "Taxa de Acertos",
                        value = "${(userStatsState.correctAnswerRate * 100).roundToInt()}%",
                        subtitle = "Continue melhorando!",
                        valueColor = GreenDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total de dias estudados
                    StatsCard(
                        title = "Total de Dias Estudados",
                        value = userStatsState.totalStudyDays.toString(),
                        subtitle = "Sua jornada de aprendizado",
                        valueColor = BlueInfo
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Mantenha sua rotina de estudos para resultados melhores!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = GrayDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (userStatsState.streakDays > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = GreenDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sequência Atual",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${userStatsState.streakDays} dias",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = AmberAccent
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Continue estudando para manter sua sequência!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = GrayDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = valueColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = GrayDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}