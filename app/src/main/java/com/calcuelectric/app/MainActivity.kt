package com.calcuelectric.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calcuelectric.app.ui.theme.CalcuelectricTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalcuelectricTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CalcuelectricApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CalcuelectricApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calcuelectric") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    if (state.user != null) {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                state.isLoading -> LoadingScreen()
                state.user == null -> AuthScreen(state, viewModel)
                else -> MainScreen(state, viewModel, focusManager)
            }
            if (state.toastMessage.isNotBlank()) {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                    Text(state.toastMessage)
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun AuthScreen(state: UiState, viewModel: MainViewModel) {
    val isLogin = state.authMode == AuthMode.Login
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a Calcuelectric", style = MaterialTheme.typography.h5, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.authEmail,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (!isLogin) {
            OutlinedTextField(
                value = state.authName,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = state.authPassword,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.authenticate(asProfessor = false) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (isLogin) "Iniciar sesión Alumno" else "Registrar Alumno")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.authenticate(asProfessor = true) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (isLogin) "Iniciar sesión Profesor" else "Registrar Profesor")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { viewModel.toggleAuthMode() }) {
            Text(if (isLogin) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Inicia sesión")
        }
        if (state.toastMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(state.toastMessage, color = Color.Red, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MainScreen(state: UiState, viewModel: MainViewModel, focusManager: FocusManager) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Hola, ${state.user?.name ?: "Usuario"}", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (state.user?.role == "admin") "Modo profesor" else "Modo estudiante",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp, shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Calculadora", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormulaSelector(state.selectedFormula, viewModel::onFormulaSelected)
                    Spacer(modifier = Modifier.height(12.dp))
                    FormulaInputs(state, viewModel)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        focusManager.clearFocus()
                        viewModel.calculateResult()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Calcular")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ResultCard(state)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.user?.role == "admin") {
            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp, shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Estudiantes", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                            if (state.selectedStudentId != null) {
                                TextButton(onClick = { viewModel.selectStudent(null) }) {
                                    Text("Ver Todos")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        UserSummary(state.users, state.selectedStudentId) { user ->
                            viewModel.selectStudent(user.id)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp, shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val historyTitle = if (state.user?.role == "admin") {
                            val selectedName = state.users.find { it.id == state.selectedStudentId }?.name
                            if (selectedName != null) "Historial de $selectedName" else "Historial Global"
                        } else {
                            "Mi Historial"
                        }
                        Text(historyTitle, style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar historial")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OperationList(state, viewModel)
                }
            }
        }
    }
}

@Composable
fun FormulaSelector(selectedFormula: String, onFormulaSelected: (String) -> Unit) {
    val options = listOf("ohm" to "Ley de Ohm", "power" to "Potencia eléctrica", "series" to "Resistencias en serie")
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Selecciona una operación", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(options.find { it.first == selectedFormula }?.second ?: "")
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, label) ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onFormulaSelected(id)
                    }) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
fun FormulaInputs(state: UiState, viewModel: MainViewModel) {
    val fields = when (state.selectedFormula) {
        "ohm" -> listOf("Voltaje (V)" to "v", "Corriente (A)" to "i", "Resistencia (Ω)" to "r")
        "power" -> listOf("Potencia (W)" to "p", "Voltaje (V)" to "v", "Corriente (A)" to "i")
        else -> listOf("Resistencia 1 (Ω)" to "r1", "Resistencia 2 (Ω)" to "r2", "Resistencia 3 (Ω)" to "r3")
    }
    Column {
        fields.forEach { (label, key) ->
            OutlinedTextField(
                value = state.inputs[key] ?: "",
                onValueChange = { viewModel.onInputChanged(key, it) },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun ResultCard(state: UiState) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.surface, RoundedCornerShape(10.dp)).padding(12.dp)) {
        Text("Resultado", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(state.resultText, style = MaterialTheme.typography.h6)
        if (state.formulaMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(state.formulaMessage, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun OperationList(state: UiState, viewModel: MainViewModel) {
    if (state.operations.isEmpty()) {
        Text("No hay operaciones para mostrar.", color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
        return
    }

    Column {
        state.operations.take(15).forEach { operation ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp, shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(operation.label, style = MaterialTheme.typography.subtitle1)
                            Text(operation.result, style = MaterialTheme.typography.body2)
                        }
                        IconButton(onClick = { viewModel.startEditing(operation) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                        IconButton(onClick = { viewModel.deleteOperation(operation.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                    }
                    if (operation.studentName != null) {
                        Text("Alumno: ${operation.studentName}", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.primary)
                    }
                    Text(operation.createdAt ?: "", style = MaterialTheme.typography.caption)
                }
            }
        }
    }

    if (state.editingOperationId != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Editar operación", style = MaterialTheme.typography.subtitle1)
        OutlinedTextField(
            value = state.editLabel,
            onValueChange = viewModel::onEditLabelChanged,
            label = { Text("Etiqueta") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = state.editResult,
            onValueChange = viewModel::onEditResultChanged,
            label = { Text("Resultado") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { viewModel.saveEditedOperation() }) { Text("Guardar") }
            OutlinedButton(onClick = { viewModel.cancelEditing() }) { Text("Cancelar") }
        }
    }
}

@Composable
fun UserSummary(users: List<User>, selectedStudentId: Int?, onUserClick: (User) -> Unit) {
    val students = users.filter { it.role != "admin" }
    if (students.isEmpty()) {
        Text("No hay estudiantes registrados.", color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
        return
    }
    Column {
        students.forEach { user ->
            val isSelected = user.id == selectedStudentId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onUserClick(user) },
                elevation = if (isSelected) 4.dp else 1.dp,
                backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface,
                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colors.primary) else null,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(user.name, style = MaterialTheme.typography.subtitle2)
                        Text(user.email, style = MaterialTheme.typography.caption)
                    }
                    Text("${user.operationsCount ?: 0} ops", style = MaterialTheme.typography.body2, fontWeight = if (isSelected) FontWeight.Bold else null)
                }
            }
        }
    }
}
