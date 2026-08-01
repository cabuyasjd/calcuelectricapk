package com.calcuelectric.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calcuelectric.app.network.NetworkModule
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.appDao()
    private val gson = Gson()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var operationsJob: Job? = null
    private var currentUserJob: Job? = null

    init {
        // 1. Observar todos los usuarios para la lista del profesor (Historial persistente en DB)
        viewModelScope.launch {
            dao.getAllUsers().collectLatest { entities ->
                val users = entities.map { 
                    User(it.id, it.name, it.email, it.role, it.operationsCount) 
                }
                _uiState.value = _uiState.value.copy(users = users)
            }
        }

        // 2. Observar al usuario actual para reaccionar a cambios en la base de datos (como cambio de rol manual)
        viewModelScope.launch {
            _uiState
                .map { it.user?.id }
                .distinctUntilChanged()
                .collectLatest { userId ->
                    if (userId != null) {
                        observeCurrentUserFromDb(userId)
                    } else {
                        currentUserJob?.cancel()
                    }
                }
        }

        refresh()
    }

    private fun observeCurrentUserFromDb(userId: Int) {
        currentUserJob?.cancel()
        currentUserJob = viewModelScope.launch {
            dao.getUserById(userId).collectLatest { entity ->
                if (entity != null) {
                    val updatedUser = User(entity.id, entity.name, entity.email, entity.role, entity.operationsCount)
                    val currentState = _uiState.value
                    
                    if (currentState.user != updatedUser) {
                        val roleChanged = currentState.user?.role != updatedUser.role
                        _uiState.value = currentState.copy(user = updatedUser)
                        
                        if (roleChanged) {
                            // Si cambió a admin, cargamos datos de profesor
                            if (updatedUser.role == "admin") {
                                refreshAdminData()
                            }
                            // Re-observar operaciones con la nueva lógica de rol
                            observeOperations(if (updatedUser.role == "admin") _uiState.value.selectedStudentId else updatedUser.id)
                        }
                    }
                }
            }
        }
    }

    private fun refreshAdminData() {
        viewModelScope.launch {
            try {
                // Descargar todos los usuarios (estudiantes)
                val usersResponse = NetworkModule.apiService.getUsers()
                val remoteUsers = usersResponse.body()?.users ?: emptyList()
                if (remoteUsers.isNotEmpty()) {
                    dao.insertUsers(remoteUsers.map {
                        UserEntity(it.id, it.name, it.email, it.role, it.operationsCount ?: 0)
                    })
                }
                
                // Cargar todas las operaciones para el profesor
                val opsResponse = NetworkModule.apiService.getOperations()
                val ops = opsResponse.body()?.operations ?: emptyList()
                if (ops.isNotEmpty()) {
                    dao.insertOperations(ops.map { 
                        OperationEntity(id = it.id, userId = it.userId, label = it.label, result = it.result, formulaLabel = it.formulaLabel, studentName = it.studentName, createdAt = it.createdAt) 
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeOperations(userId: Int?) {
        operationsJob?.cancel()
        operationsJob = viewModelScope.launch {
            val flow = if (userId == null) {
                dao.getAllOperations()
            } else {
                dao.getOperationsByUser(userId)
            }
            
            flow.collectLatest { entities ->
                val operations = entities.map { 
                    Operation(it.id, it.userId, it.label, it.result, it.formulaLabel, it.studentName, it.createdAt) 
                }
                _uiState.value = _uiState.value.copy(operations = operations)
            }
        }
    }

    fun selectStudent(studentId: Int?) {
        _uiState.value = _uiState.value.copy(selectedStudentId = studentId)
        val user = _uiState.value.user
        if (user != null) {
            observeOperations(if (user.role == "admin") studentId else user.id)
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, toastMessage = "")
        viewModelScope.launch {
            try {
                val sessionResponse = NetworkModule.apiService.getSession()
                val user = sessionResponse.body()?.user
                if (sessionResponse.isSuccessful && user != null) {
                    loadData(user)
                } else {
                    tryLocalLogin()
                }
            } catch (e: Exception) {
                tryLocalLogin("Modo Local: Base de Datos Activa")
            }
        }
    }

    private suspend fun tryLocalLogin(message: String = "") {
        val localUsers = dao.getAllUsers().first()
        if (localUsers.isNotEmpty()) {
            val entity = localUsers.first()
            val user = User(entity.id, entity.name, entity.email, entity.role, entity.operationsCount)
            _uiState.value = _uiState.value.copy(isLoading = false, user = user, toastMessage = message)
            observeOperations(if (user.role == "admin") _uiState.value.selectedStudentId else user.id)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            authMode = if (_uiState.value.authMode == AuthMode.Login) AuthMode.Register else AuthMode.Login,
            toastMessage = ""
        )
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(authEmail = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(authPassword = value)
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(authName = value)
    }

    fun onFormulaSelected(formula: String) {
        _uiState.value = _uiState.value.copy(selectedFormula = formula)
    }

    fun onInputChanged(key: String, value: String) {
        val newInputs = _uiState.value.inputs.toMutableMap()
        newInputs[key] = value
        _uiState.value = _uiState.value.copy(inputs = newInputs)
    }

    fun authenticate(asProfessor: Boolean = false) {
        val state = _uiState.value
        val payload = mutableMapOf(
            "email" to state.authEmail,
            "password" to state.authPassword,
            "role" to if (asProfessor) "admin" else "student"
        )
        
        if (state.authMode == AuthMode.Register) {
            payload["action"] = "register"
            payload["name"] = state.authName
        } else {
            payload["action"] = "login"
        }

        _uiState.value = state.copy(isLoading = true, toastMessage = "")
        viewModelScope.launch {
            try {
                val response = NetworkModule.apiService.login(payload)
                if (response.isSuccessful && response.body()?.user != null) {
                    val userFromServer = response.body()!!.user!!
                    // Si el usuario eligió entrar como profesor, forzamos el rol admin localmente
                    val userToLoad = if (asProfessor) userFromServer.copy(role = "admin") else userFromServer
                    loadData(userToLoad)
                } else {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = gson.fromJson(errorBody, com.calcuelectric.app.network.AuthResponse::class.java)
                        errorResponse.message ?: "Error de autenticación"
                    } catch (e: Exception) {
                        "Error en la respuesta del servidor"
                    }
                    _uiState.value = state.copy(isLoading = false, toastMessage = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(isLoading = false, toastMessage = "Error de conexión")
            }
        }
    }

    fun calculateResult() {
        val state = _uiState.value
        val result = when (state.selectedFormula) {
            "ohm" -> calculateOhm(state.inputs)
            "power" -> calculatePower(state.inputs)
            "series" -> calculateSeries(state.inputs)
            else -> "Completa los campos"
        }

        val formulaMessage = when (state.selectedFormula) {
            "ohm" -> "Ley de Ohm: V = I × R"
            "power" -> "Potencia: P = V × I"
            else -> "Resistencias en serie"
        }

        _uiState.value = state.copy(resultText = result, formulaMessage = formulaMessage)
        if (result.any { it.isDigit() } && !result.startsWith("Completa")) {
            saveOperation(result, formulaMessage)
        }
    }

    private suspend fun loadData(user: User) {
        try {
            // Verificar si el usuario ya es admin en la base de datos local para no sobrescribir su rol
            val localUser = dao.getUserById(user.id).first()
            val finalRole = if (localUser?.role == "admin") "admin" else user.role
            val userToSave = user.copy(role = finalRole)

            // Guardar usuario en la base de datos local
            dao.insertUser(UserEntity(userToSave.id, userToSave.name, userToSave.email, userToSave.role, userToSave.operationsCount ?: 0))

            if (userToSave.role == "admin") {
                refreshAdminData()
            } else {
                // Cargar operaciones del estudiante
                val opsResponse = NetworkModule.apiService.getOperations()
                val ops = opsResponse.body()?.operations ?: emptyList()
                dao.insertOperations(ops.map { 
                    OperationEntity(id = it.id, userId = it.userId, label = it.label, result = it.result, formulaLabel = it.formulaLabel, studentName = it.studentName, createdAt = it.createdAt) 
                })
            }

            _uiState.value = _uiState.value.copy(isLoading = false, user = userToSave)
            observeOperations(if (userToSave.role == "admin") _uiState.value.selectedStudentId else userToSave.id)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, user = user, toastMessage = "Sincronizado localmente")
            observeOperations(if (user.role == "admin") _uiState.value.selectedStudentId else user.id)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { 
                NetworkModule.apiService.login(mapOf("action" to "logout")) 
            } catch (_: Exception) {}
            
            // Mantenemos los usuarios en DB, solo reseteamos el estado actual
            _uiState.value = UiState(isLoading = false, user = null, toastMessage = "Sesión cerrada")
            operationsJob?.cancel()
            currentUserJob?.cancel()
        }
    }

    fun startEditing(operation: Operation) {
        _uiState.value = _uiState.value.copy(editingOperationId = operation.id, editLabel = operation.label, editResult = operation.result)
    }

    fun deleteOperation(id: Int) {
        viewModelScope.launch {
            try {
                NetworkModule.apiService.deleteOperation(mapOf("id" to id))
                dao.deleteOperationById(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(toastMessage = "Error al eliminar")
            }
        }
    }

    fun onEditLabelChanged(value: String) {
        _uiState.value = _uiState.value.copy(editLabel = value)
    }

    fun onEditResultChanged(value: String) {
        _uiState.value = _uiState.value.copy(editResult = value)
    }

    fun saveEditedOperation() {
        val state = _uiState.value
        val id = state.editingOperationId ?: return
        viewModelScope.launch {
            try {
                val res = NetworkModule.apiService.updateOperation(mapOf("id" to id.toString(), "label" to state.editLabel, "result" to state.editResult))
                val updated = res.body()?.operation
                if (updated != null) {
                    dao.insertOperation(OperationEntity(id = updated.id, userId = updated.userId, label = updated.label, result = updated.result, formulaLabel = updated.formulaLabel, studentName = updated.studentName, createdAt = updated.createdAt))
                    _uiState.value = state.copy(editingOperationId = null, toastMessage = "Actualizado")
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(toastMessage = "Error al actualizar")
            }
        }
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(editingOperationId = null)
    }

    private fun calculateOhm(inputs: Map<String, String>): String {
        val v = inputs["v"]?.toDoubleOrNull()
        val i = inputs["i"]?.toDoubleOrNull()
        val r = inputs["r"]?.toDoubleOrNull()
        return when {
            v != null && i != null -> String.format(Locale.US, "%.2f Ω", v / i)
            v != null && r != null -> String.format(Locale.US, "%.2f A", v / r)
            i != null && r != null -> String.format(Locale.US, "%.2f V", i * r)
            else -> "Completa 2 campos"
        }
    }

    private fun calculatePower(inputs: Map<String, String>): String {
        val p = inputs["p"]?.toDoubleOrNull()
        val v = inputs["v"]?.toDoubleOrNull()
        val i = inputs["i"]?.toDoubleOrNull()
        return when {
            p != null && v != null -> String.format(Locale.US, "%.2f A", p / v)
            p != null && i != null -> String.format(Locale.US, "%.2f V", p / i)
            v != null && i != null -> String.format(Locale.US, "%.2f W", v * i)
            else -> "Completa 2 campos"
        }
    }

    private fun calculateSeries(inputs: Map<String, String>): String {
        val sum = (inputs["r1"]?.toDoubleOrNull() ?: 0.0) + (inputs["r2"]?.toDoubleOrNull() ?: 0.0) + (inputs["r3"]?.toDoubleOrNull() ?: 0.0)
        return if (sum > 0) String.format(Locale.US, "%.2f Ω", sum) else "Ingresa valores"
    }

    private fun saveOperation(result: String, formula: String) {
        val state = _uiState.value
        val currentUser = state.user ?: return
        val label = when (state.selectedFormula) {
            "ohm" -> "Ley de Ohm"
            "power" -> "Potencia"
            else -> "Serie"
        }
        viewModelScope.launch {
            try {
                val res = NetworkModule.apiService.createOperation(mapOf("label" to label, "result" to result, "formulaLabel" to formula))
                res.body()?.operation?.let { op ->
                    dao.insertOperation(OperationEntity(id = op.id, userId = op.userId, label = op.label, result = op.result, formulaLabel = op.formulaLabel, studentName = op.studentName, createdAt = op.createdAt))
                }
            } catch (e: Exception) {
                val tempId = -(System.currentTimeMillis() % 1000000).toInt()
                dao.insertOperation(OperationEntity(id = tempId, userId = currentUser.id, label = label, result = result, formulaLabel = formula, createdAt = "Local"))
            }
        }
    }
}
