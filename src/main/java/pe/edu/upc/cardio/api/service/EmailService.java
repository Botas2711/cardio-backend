package pe.edu.upc.cardio.api.service;

public interface EmailService {

    void enviarCorreoRecuperacion(String destinatario, String nombre, String nuevaContrasena);

    void enviarCredencialesPaciente(String destinatario, String nombre, String correo, String contrasena);
}
