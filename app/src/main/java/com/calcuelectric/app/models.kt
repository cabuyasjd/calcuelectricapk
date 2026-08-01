package com.calcuelectric.app

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val operationsCount: Int? = 0
)

data class Operation(
    val id: Int,
    val userId: Int,
    val label: String,
    val result: String,
    val formulaLabel: String,
    val studentName: String? = null,
    val createdAt: String? = null
)

enum class AuthMode { Login, Register }

data class UiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val users: List<User> = emptyList(),
    val operations: List<Operation> = emptyList(),
    val authMode: AuthMode = AuthMode.Login,
    val authName: String = "",
    val authEmail: String = "",
    val authPassword: String = "",
    val selectedFormula: String = "ohm",
    val inputs: Map<String, String> = mapOf(
        "v" to "", "i" to "", "r" to "", "p" to "", "r1" to "", "r2" to "", "r3" to ""
    ),
    val resultText: String = "Ingresa valores para comenzar",
    val formulaMessage: String = "",
    val editingOperationId: Int? = null,
    val editLabel: String = "",
    val editResult: String = "",
    val toastMessage: String = "",
    val selectedStudentId: Int? = null // Para filtrar historial en vista de profesor
)
