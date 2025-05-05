package com.example.flashcardapp.data.entities

import kotlinx.serialization.Serializable

/**
 * Enum para tipos de flashcards
 */
@Serializable
enum class FlashcardType {
    BASIC, // Frente e verso simples
    QUIZ, // Múltipla escolha
    CLOZE, // Omissão de palavras
    INPUT // Digite a resposta
}