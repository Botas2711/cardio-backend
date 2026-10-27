package pe.edu.upc.cardio.api.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import pe.edu.upc.cardio.api.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final String REMITENTE_NOMBRE = "PonteCorazonPe";
    private static final String ASUNTO_RECUPERACION = "Restablecimiento de Contraseña - PonteCorazonPe";
    private static final String BRAND_COLOR_PRIMARY = "#E11D48";
    private static final String BRAND_COLOR_LIGHT = "#FFE4E6";
    private static final String BRAND_COLOR_BACKGROUND = "#FFF1F2";

    private final JavaMailSender mailSender;

    @Override
    public void enviarCorreoRecuperacion(String destinatario, String nombre, String nuevaContrasena) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(ASUNTO_RECUPERACION);
            helper.setFrom("no-reply@pontecorazonpe.com", REMITENTE_NOMBRE);

            String cuerpoHtml = construirPlantillaHtml(nombre, nuevaContrasena);
            helper.setText(cuerpoHtml, true);

            mailSender.send(mensaje);
            logger.info("Correo de recuperación enviado exitosamente a: {}", destinatario);

        } catch (MessagingException ex) {
            logger.error("Error al construir el correo de recuperación para {}: {}", destinatario, ex.getMessage());
        } catch (Exception ex) {
            logger.warn("No se pudo conectar al servidor SMTP para enviar el correo a {} (Verifique credenciales en application.properties): {}",
                    destinatario, ex.getMessage());
            logger.info("[Simulación Local] La nueva contraseña generada para {} es: {}", destinatario, nuevaContrasena);
        }
    }

    private String construirPlantillaHtml(String nombre, String nuevaContrasena) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Restablecimiento de Contraseña</title>
              <style>
                body {
                  margin: 0;
                  padding: 24px;
                  background-color: #F8FAFC;
                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                  color: #0F172A;
                }
                .email-container {
                  max-width: 560px;
                  margin: 0 auto;
                  background-color: #FFFFFF;
                  border-radius: 16px;
                  overflow: hidden;
                  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
                  border: 1px solid #E2E8F0;
                }
                .email-header {
                  background-color: %s;
                  padding: 36px 24px;
                  text-align: center;
                  color: #FFFFFF;
                }
                .brand-title {
                  font-size: 26px;
                  font-weight: 800;
                  letter-spacing: -0.5px;
                  margin: 0;
                }
                .brand-slogan {
                  font-size: 14px;
                  font-weight: 500;
                  margin-top: 6px;
                  opacity: 0.95;
                }
                .email-body {
                  padding: 36px 32px;
                  line-height: 1.65;
                  font-size: 15px;
                }
                .greeting {
                  font-size: 18px;
                  font-weight: 700;
                  color: #0F172A;
                  margin-top: 0;
                  margin-bottom: 16px;
                }
                .password-card {
                  background-color: %s;
                  border: 2px dashed %s;
                  border-radius: 12px;
                  padding: 20px;
                  text-align: center;
                  margin: 28px 0;
                }
                .password-label {
                  font-size: 12px;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 1px;
                  color: %s;
                  margin-bottom: 8px;
                }
                .password-value {
                  font-size: 24px;
                  font-weight: 800;
                  color: %s;
                  letter-spacing: 2px;
                  font-family: 'Courier New', Courier, monospace;
                  user-select: all;
                }
                .security-note {
                  font-size: 13px;
                  color: #64748B;
                  background-color: #F1F5F9;
                  border-left: 4px solid %s;
                  padding: 12px 16px;
                  border-radius: 0 8px 8px 0;
                  margin-top: 24px;
                }
                .email-footer {
                  background-color: #F8FAFC;
                  padding: 24px 32px;
                  text-align: center;
                  font-size: 12px;
                  color: #94A3B8;
                  border-top: 1px solid #E2E8F0;
                }
              </style>
            </head>
            <body>
              <div class="email-container">
                <!-- Cabecera de Marca Oficial -->
                <div class="email-header">
                  <h1 class="brand-title">PonteCorazonPe</h1>
                  <p class="brand-slogan">“Cuidando tu corazón a cada latido”</p>
                </div>

                <!-- Contenido Principal -->
                <div class="email-body">
                  <p class="greeting">Estimado/a %s,</p>
                  <p>
                    Hemos recibido una solicitud para restablecer el acceso a su cuenta en el
                    <strong>Sistema de monitoreo</strong>.
                  </p>
                  <p>
                    A continuación, le proporcionamos su nueva contraseña provisional para acceder al sistema:
                  </p>

                  <!-- Recuadro Destacado con la Nueva Contraseña -->
                  <div class="password-card">
                    <div class="password-label">Nueva Contraseña de Acceso</div>
                    <div class="password-value">%s</div>
                  </div>

                  <div class="security-note">
                    <strong>Recomendación de seguridad:</strong> Por motivos de confidencialidad y protección de datos médicos, le sugerimos ingresar al sistema y actualizar esta contraseña provisional.
                  </div>
                </div>

                <!-- Pie de Página Institucional -->
                <div class="email-footer">
                  <p style="margin: 0 0 6px 0;">Este es un mensaje automático de seguridad generado por la plataforma PonteCorazonPe.</p>
                  <p style="margin: 0;">© 2026 PonteCorazonPe • Todos los derechos reservados.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_BACKGROUND,
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_PRIMARY,
                nombre,
                nuevaContrasena
        );
    }

    @Override
    public void enviarCredencialesPaciente(String destinatario, String nombre, String correo, String contrasena) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Bienvenido a PonteCorazonPe - Tus credenciales para la App Móvil");
            helper.setFrom("no-reply@pontecorazonpe.com", REMITENTE_NOMBRE);

            String cuerpoHtml = construirPlantillaBienvenidaHtml(nombre, correo, contrasena);
            helper.setText(cuerpoHtml, true);

            mailSender.send(mensaje);
            logger.info("Correo de credenciales de paciente enviado exitosamente a: {}", destinatario);

        } catch (MessagingException ex) {
            logger.error("Error al construir el correo de credenciales para {}: {}", destinatario, ex.getMessage());
        } catch (Exception ex) {
            logger.warn("No se pudo conectar al servidor SMTP para enviar credenciales a {}: {}", destinatario, ex.getMessage());
            logger.info("[Simulación Local] Credenciales para {}: Correo: {} / Contraseña: {}", destinatario, correo, contrasena);
        }
    }

    private String construirPlantillaBienvenidaHtml(String nombre, String correo, String contrasena) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Bienvenido a PonteCorazonPe</title>
              <style>
                body {
                  margin: 0;
                  padding: 24px;
                  background-color: #F8FAFC;
                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                  color: #0F172A;
                }
                .email-container {
                  max-width: 560px;
                  margin: 0 auto;
                  background-color: #FFFFFF;
                  border-radius: 16px;
                  overflow: hidden;
                  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
                  border: 1px solid #E2E8F0;
                }
                .email-header {
                  background-color: %s;
                  padding: 36px 24px;
                  text-align: center;
                  color: #FFFFFF;
                }
                .brand-title {
                  font-size: 26px;
                  font-weight: 800;
                  letter-spacing: -0.5px;
                  margin: 0;
                }
                .brand-slogan {
                  font-size: 14px;
                  font-weight: 500;
                  margin-top: 6px;
                  opacity: 0.95;
                }
                .email-body {
                  padding: 36px 32px;
                  line-height: 1.65;
                  font-size: 15px;
                }
                .greeting {
                  font-size: 18px;
                  font-weight: 700;
                  color: #0F172A;
                  margin-top: 0;
                  margin-bottom: 16px;
                }
                .credentials-card {
                  background-color: %s;
                  border: 2px dashed %s;
                  border-radius: 12px;
                  padding: 20px;
                  text-align: left;
                  margin: 24px 0;
                }
                .cred-item {
                  margin-bottom: 12px;
                }
                .cred-label {
                  font-size: 11px;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 1px;
                  color: %s;
                  margin-bottom: 4px;
                }
                .cred-value {
                  font-size: 20px;
                  font-weight: 800;
                  color: %s;
                  letter-spacing: 1px;
                  font-family: 'Courier New', Courier, monospace;
                  user-select: all;
                }
                .instructions-box {
                  background-color: #F8FAFC;
                  border: 1px solid #E2E8F0;
                  border-radius: 10px;
                  padding: 16px;
                  margin-top: 20px;
                }
                .step {
                  display: flex;
                  align-items: flex-start;
                  margin-bottom: 10px;
                  font-size: 14px;
                }
                .step-num {
                  background-color: %s;
                  color: white;
                  font-weight: bold;
                  border-radius: 50%%;
                  width: 22px;
                  height: 22px;
                  display: inline-flex;
                  align-items: center;
                  justify-content: center;
                  font-size: 11px;
                  margin-right: 10px;
                  flex-shrink: 0;
                }
                .email-footer {
                  background-color: #F8FAFC;
                  padding: 24px 32px;
                  text-align: center;
                  font-size: 12px;
                  color: #94A3B8;
                  border-top: 1px solid #E2E8F0;
                }
              </style>
            </head>
            <body>
              <div class="email-container">
                <div class="email-header">
                  <h1 class="brand-title">PonteCorazonPe</h1>
                  <p class="brand-slogan">“Cuidando tu corazón a cada latido”</p>
                </div>

                <div class="email-body">
                  <p class="greeting">Estimado/a %s,</p>
                  <p>
                    ¡Te damos la bienvenida a <strong>PonteCorazonPe</strong>! Tu médico especialista te ha registrado en la plataforma de telemonitoreo cardíaco para cuidar tu salud hemodinámica mediante tu smartwatch.
                  </p>
                  <p>
                    A continuación, te proporcionamos tus credenciales de acceso a la <strong>Aplicación Móvil</strong>:
                  </p>

                  <div class="credentials-card">
                    <div class="cred-item">
                      <div class="cred-label">Correo Electrónico</div>
                      <div class="cred-value" style="font-size: 16px; word-break: break-all;">%s</div>
                    </div>
                    <div class="cred-item" style="margin-bottom: 0;">
                      <div class="cred-label">Contraseña</div>
                      <div class="cred-value">%s</div>
                    </div>
                  </div>

                  <div class="instructions-box">
                    <p style="margin: 0 0 10px 0; font-weight: bold; color: #1E293B;">Primeros pasos para conectarte:</p>
                    <div class="step">
                      <span class="step-num">1</span>
                      <span>Abre la aplicación móvil <strong>PonteCorazonPe</strong> en tu teléfono inteligente.</span>
                    </div>
                    <div class="step">
                      <span class="step-num">2</span>
                      <span>Inicia sesión ingresando tu correo electrónico y la contraseña indicada arriba.</span>
                    </div>
                    <div class="step" style="margin-bottom: 0;">
                      <span class="step-num">3</span>
                      <span>Enlaza tu smartwatch vía Bluetooth para comenzar el monitoreo continuo en tiempo real.</span>
                    </div>
                  </div>
                </div>

                <div class="email-footer">
                  <p style="margin: 0 0 6px 0;">Este mensaje es confidencial y contiene datos médicos privados.</p>
                  <p style="margin: 0;">© 2026 PonteCorazonPe • Todos los derechos reservados.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_BACKGROUND,
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_PRIMARY,
                BRAND_COLOR_PRIMARY,
                nombre,
                correo,
                contrasena
        );
    }
}

