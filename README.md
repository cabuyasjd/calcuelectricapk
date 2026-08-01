# ⚡ Calcuelectric - Sistema Integral de Gestión y Cálculos Eléctricos

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg) ![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg) ![Compose](https://img.swift.io/badge/UI-Jetpack%20Compose-orange.svg)

**Calcuelectric** es una solución móvil avanzada diseñada para el sector educativo y técnico, permitiendo la resolución de cálculos eléctricos fundamentales bajo una arquitectura moderna, reactiva y escalable.

---

## 🚀 Funcionalidades Destacadas

### 🧮 Calculadora de Ingeniería
*   **Ley de Ohm Pro:** Resolución dinámica de Voltaje ($V$), Corriente ($I$) y Resistencia ($R$).
*   **Análisis de Potencia:** Cálculos rápidos de Potencia Activa ($P$) en Watts.
*   **Circuitos en Serie:** Sumatoria resistiva inteligente para configuraciones de múltiples componentes.

### 👥 Gestión por Roles (Dual-Mode)
*   **Vista del Estudiante:**
    *   Cálculos con guardado automático en la nube y local.
    *   Historial personal persistente para revisión de ejercicios.
*   **Vista del Profesor (Administrador):**
    *   **Dashboard de Estudiantes:** Visualización en tiempo real de todos los alumnos registrados.
    *   **Monitorización Académica:** Acceso al historial de operaciones de cualquier alumno para evaluar su progreso.
    *   **Historial Global:** Auditoría completa de todos los cálculos realizados en el sistema.

---

## 🛠 Stack Tecnológico

La aplicación implementa las mejores prácticas de desarrollo recomendadas por Google:

| Componente | Tecnología |
| :--- | :--- |
| **Interfaz de Usuario** | Jetpack Compose (UI 100% Declarativa) |
| **Arquitectura** | MVVM (Model-View-ViewModel) |
| **Base de Datos** | Room Persistence Library (SQLite) |
| **Networking** | Retrofit 2 + OkHttp 4 |
| **Asincronía** | Kotlin Coroutines & Flow |
| **Inyección de Dependencias** | Singleton Pattern (Network & Database) |

---

## 🏗 Estructura del Proyecto

```text
app/src/main/java/com/calcuelectric/app/
├── network/          # Servicios API, interceptores y modelos de red
├── ui/theme/         # Definición de estilos, colores y tipografías Material 3
├── database.kt       # Configuración de Room, DAOs y Entidades persistentes
├── models.kt         # Modelos de dominio y definición del UI State
├── MainViewModel.kt  # Núcleo de lógica de negocio y sincronización
└── MainActivity.kt   # Punto de entrada y orquestación de la UI
```

---

## ⚙️ Configuración e Instalación

### Requisitos
*   Android Studio Ladybug o superior.
*   Java Development Kit (JDK) 17.
*   Acceso a la API backend de Calcuelectric.

### Pasos
1.  **Clonar:** `git clone https://github.com/cabuyasjd/calcuelectricapk.git`
2.  **Importar:** Abre la carpeta del proyecto en Android Studio.
3.  **Configurar:** Actualiza la `BASE_URL` en `com.calcuelectric.app.network.NetworkModule`.
4.  **Ejecutar:** Compila y despliega en un dispositivo o emulador (`Run 'app'`).

---

## 📈 Roadmap
- [ ] Soporte para cálculos en Corriente Alterna (CA).
- [ ] Gráficas de comportamiento eléctrico.
- [ ] Exportación de reportes de progreso en PDF.

---
**Desarrollado por [Daniel]** - *Impulsando la educación técnica mediante software de calidad.*
