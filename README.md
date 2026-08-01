# Calcuelectric

**Calcuelectric** es una aplicación Android diseñada para estudiantes y profesores de electricidad, que facilita el cálculo de fórmulas eléctricas fundamentales y el seguimiento del progreso académico.

## 🚀 Características principales

*   **Calculadora Eléctrica:** Herramienta intuitiva para realizar cálculos de:
    *   **Ley de Ohm:** Calcula Voltaje (V), Corriente (I) o Resistencia (R).
    *   **Potencia Eléctrica:** Calcula Potencia (P), Voltaje (V) o Corriente (I).
    *   **Resistencias en Serie:** Suma de hasta 3 resistencias.
*   **Gestión de Usuarios:**
    *   **Modo Estudiante:** Permite a los alumnos realizar cálculos y guardar un historial personal de sus operaciones.
    *   **Modo Profesor (Admin):** Permite a los docentes supervisar el progreso de todos los estudiantes, ver la lista de alumnos registrados y consultar el historial global de operaciones.
*   **Persistencia de Datos:** 
    *   **Base de Datos Local (Room):** Los datos se guardan de forma permanente en el dispositivo, permitiendo el uso offline y el registro histórico de usuarios.
    *   **Sincronización Remota (Retrofit/API PHP):** Sincroniza las operaciones y perfiles con un servidor central para acceso desde múltiples dispositivos.
*   **Interfaz Moderna:** Desarrollada íntegramente con **Jetpack Compose**, ofreciendo una experiencia de usuario fluida y reactiva.

## 🛠️ Tecnologías utilizadas

*   **Kotlin:** Lenguaje de programación principal.
*   **Jetpack Compose:** Para la interfaz de usuario moderna y declarativa.
*   **Room Database:** Persistencia de datos local.
*   **Retrofit & OkHttp:** Comunicación con la API REST.
*   **Coroutines & Flow:** Manejo de asincronía y flujo de datos reactivo.
*   **ViewModel & StateFlow:** Arquitectura MVVM para una gestión de estado robusta.

## 📦 Instalación y Configuración

1.  Clona este repositorio:
    ```bash
    git clone https://github.com/cabuyasjd/calcuelectricapk.git
    ```
2.  Abre el proyecto en **Android Studio**.
3.  Asegúrate de tener configurado el backend (API PHP) y actualiza la `BASE_URL` en `NetworkModule.kt` si es necesario.
4.  Compila y ejecuta la aplicación en un emulador o dispositivo físico.

---
Desarrollado para facilitar el aprendizaje y la enseñanza de la electrónica básica.
