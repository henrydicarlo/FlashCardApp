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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel
import kotlin.math.roundToInt

private val BluePrimary = Color(0xFF2962FF) // Azul vibrante primário
private val BlueDark = Color(0xFF0039CB) // Azul escuro para elementos de destaque
private val BlueLight = Color(0xFFE3F2FD) // Azul claro para fundos secundários
private val MagentaSecondary = Color(0xFFE91E63) // Rosa/magenta para elementos complementares
private val MagentaLight = Color(0xFFFCE4EC) // Rosa claro para fundos sutis
private val PurpleTransition = Color(0xFF9C27B0) // Roxo para transições em gradientes
private val NeutralLight = Color(0xFFFAFAFA) // Neutro claro para fundos
private val NeutralDark = Color(0xFF333333) // Neutro escuro para textos principais
private val ErrorRed = Color(0xFFE53935) // Vermelho para erros
private val InfoBlue = Color(0xFF29B6F6) // Azul para informações
private val AmberAccent = Color(0xFFFFAB00) // Âmbar para elementos que precisam de destaque especial

// Gradiente principal para elementos destacados
private val GradientPrimary = Brush.horizontalGradient(
    colors = listOf(MagentaSecondary, PurpleTransition, BluePrimary)
)

// Gradiente suave para fundos e elementos decorativos
private val GradientBackground = Brush.verticalGradient(
    colors = listOf(BlueLight.copy(alpha = 0.8f), MagentaLight.copy(alpha = 0.3f))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: FlashcardAppViewModel
) {
    val userStatsState by viewModel.userStatsUiState.collectAsState()

    Scaffold(
        containerColor = NeutralLight,
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas de Estudo", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
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
                .background(GradientBackground) // Aplicando o gradiente de fundo
                .padding(paddingValues)
        ) {
            if (userStatsState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BluePrimary
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
                    StatsCardModern(
                        title = "Sequência de Dias",
                        value = userStatsState.streakDays.toString(),
                        valueColor = MagentaSecondary, // Usando uma cor vibrante
                        unit = " dias",
                        description = "Sua sequência atual de estudo",
                        icon = Icons.Filled.CalendarMonth,
                        backgroundColor = Color.White, // Fundo completamente branco agora
                        textColor = NeutralDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Taxa de acerto
                    StatsCardModern(
                        title = "Taxa de Acertos",
                        value = "${(userStatsState.correctAnswerRate * 100).roundToInt()}",
                        valueColor = BluePrimary, // Outra cor vibrante
                        unit = "%",
                        description = "Seu desempenho geral",
                        icon = Icons.Filled.CheckCircle,
                        backgroundColor = Color.White,
                        textColor = NeutralDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total de dias estudados
                    StatsCardModern(
                        title = "Total de Dias Estudados",
                        value = userStatsState.totalStudyDays.toString(),
                        valueColor = PurpleTransition, // Mais uma cor da paleta
                        unit = " dias",
                        description = "Seu tempo total de aprendizado",
                        icon = Icons.Filled.Timeline,
                        backgroundColor = Color.White,
                        textColor = NeutralDark
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Continue avançando em sua jornada de aprendizado!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = NeutralDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (userStatsState.maxStreakDays > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MagentaSecondary.copy(alpha = 1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Melhor Sequência",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeutralLight
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${userStatsState.maxStreakDays} dia(s)",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = NeutralLight
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
fun StatsCardModern(
    title: String,
    value: String,
    valueColor: Color,
    unit: String = "",
    description: String,
    icon: ImageVector? = null,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = valueColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$value$unit",
                style = MaterialTheme.typography.headlineLarge,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}