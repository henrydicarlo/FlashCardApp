package com.example.flashcardapp.data.entities

/**
 * Enum para tipos de flashcards
 */
enum class FlashcardType {
    BASIC, // Frente e verso simples
    QUIZ, // Múltipla escolha
    CLOZE, // Omissão de palavras
    INPUT // Digite a resposta
}