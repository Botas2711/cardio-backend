# Backend Transaccional y de Soporte Clínico - CardioPredict

Este módulo es el backend transaccional en **Spring Boot (Java 21)** con **PostgreSQL 16** para el Sistema Inteligente de Soporte a la Decisión Clínica (CDSS) del Hospital II Carlos Alcántara Butterfield.

Toda la documentación técnica completa, diccionario de datos relacional, matriz de cumplimiento de las 28 Historias de Usuario (HU001–HU028), catálogo de endpoints y credenciales de prueba se encuentra en:

📄 **[Manual Técnico y Documentación Completa](../documentacion_backend.md)**

---

## Inicio Rápido

1. Asegúrate de tener una base de datos PostgreSQL llamada `cardio_db` corriendo en `localhost:5432` con usuario `postgres` y contraseña `admin` (o ajusta [application.properties](src/main/resources/application.properties)).

2. Compilar:
   ```bash
   .\gradlew compileJava
   ```

3. Ejecutar:
   ```bash
   .\gradlew bootRun
   ```

4. Documentación interactiva Swagger UI:
   👉 `http://localhost:8080/swagger-ui.html`

### Credenciales de Prueba Presembradas
* **Cardiólogo:** `cardiologo@hospital.gob.pe` / `Cardio123!`
* **Paciente Crítico:** `juan.perez@gmail.com` / `Paciente123!`
* **Paciente Moderado:** `rosa.flores@gmail.com` / `Paciente123!`
* **Paciente Estable:** `luis.mendoza@gmail.com` / `Paciente123!`
