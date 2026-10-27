package pe.edu.upc.cardio.api.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.exception.BadRequestException;
import pe.edu.upc.cardio.api.exception.ResourceNotFoundException;
import pe.edu.upc.cardio.api.model.AlertaClinica;
import pe.edu.upc.cardio.api.model.Paciente;
import pe.edu.upc.cardio.api.model.TelemetriaSenal;
import pe.edu.upc.cardio.api.repository.AlertaClinicaRepository;
import pe.edu.upc.cardio.api.repository.PacienteRepository;
import pe.edu.upc.cardio.api.repository.TelemetriaSenalRepository;
import pe.edu.upc.cardio.api.repository.UsuarioRepository;
import pe.edu.upc.cardio.api.service.ReporteClinicoService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteClinicoServiceImpl implements ReporteClinicoService {

    private final PacienteRepository pacienteRepository;
    private final TelemetriaSenalRepository telemetriaRepository;
    private final AlertaClinicaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReportePdf(Long idPaciente, LocalDate fechaInicio, LocalDate fechaFin) {
        return generarReportePdf(idPaciente, fechaInicio, fechaFin, null);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReportePdf(Long idPaciente, LocalDate fechaInicio, LocalDate fechaFin, String medico) {
        String nombreDoctor = (medico != null && !medico.trim().isEmpty()) ? medico.trim() : obtenerNombreDoctorSesion();

        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente));

        // HU017 Escenario 2: Paciente sin registros clínicos suficientes
        long totalLecturas = telemetriaRepository.countByPacienteIdPaciente(idPaciente);
        if (totalLecturas == 0) {
            throw new BadRequestException("El paciente aún no cuenta con lecturas registradas");
        }

        // HU017 Escenario 3: Selección de período para el informe
        LocalDate fin = (fechaFin != null) ? fechaFin : LocalDate.now();
        LocalDate inicio = (fechaInicio != null) ? fechaInicio : fin.minusDays(30);

        OffsetDateTime start = inicio.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = fin.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<TelemetriaSenal> lecturas = telemetriaRepository.findByPacienteIdPacienteAndFechaHoraBetweenOrderByFechaHoraAsc(
                idPaciente, start, end);

        if (lecturas.isEmpty()) {
            throw new BadRequestException("No existen lecturas registradas para el rango de fechas seleccionado");
        }

        List<AlertaClinica> alertasAtendidas = alertaRepository.findByPacienteIdPacienteAndAtendidaTrueAndFechaHoraBetweenOrderByFechaHoraDesc(
                idPaciente, start, end);

        // HU017 Escenario 1: Generación del documento clínico consolidado
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Estilos de fuentes alineados con la identidad visual web (#E11D48 y Slate)
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, new Color(225, 29, 72));
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(15, 23, 42));
            Font encabezadoTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font celdaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(30, 41, 59));
            Font pieFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(100, 116, 139));

            // Título y Plataforma (sin nombre de hospital)
            Paragraph titulo = new Paragraph("INFORME CLÍNICO CONSOLIDADO DE TELEMETRÍA CARDIOVASCULAR", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(4);
            document.add(titulo);

            Paragraph plataforma = new Paragraph("PonteCorazonPe · Plataforma de Monitoreo y Telemetría Médica", pieFont);
            plataforma.setAlignment(Element.ALIGN_CENTER);
            plataforma.setSpacingAfter(15);
            document.add(plataforma);

            // Sección 1: Datos del Paciente
            Paragraph sec1 = new Paragraph("1. Información General del Paciente", subtituloFont);
            sec1.setSpacingAfter(8);
            document.add(sec1);

            int edad = (paciente.getEdad() != null) ? paciente.getEdad() :
                    ((paciente.getFechaNacimiento() != null) ? Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears() : 0);

            PdfPTable tablaPaciente = new PdfPTable(2);
            tablaPaciente.setWidthPercentage(100);
            tablaPaciente.setSpacingAfter(15);

            agregarFilaInfo(tablaPaciente, "Nombres y Apellidos:", paciente.getNombres() + " " + paciente.getApellidos(), celdaFont);
            agregarFilaInfo(tablaPaciente, "Documento de Identidad (DNI):", paciente.getDni(), celdaFont);
            agregarFilaInfo(tablaPaciente, "Edad / Género:", edad + " años / " + (paciente.getGenero() != null ? paciente.getGenero() : "-"), celdaFont);
            agregarFilaInfo(tablaPaciente, "Perfil Antropométrico:",
                    String.format("Peso: %.1f kg | Talla: %d cm | IMC: %.1f kg/m²",
                            paciente.getPesoKg() != null ? paciente.getPesoKg() : BigDecimal.ZERO,
                            paciente.getTallaCm() != null ? paciente.getTallaCm() : 0,
                            paciente.getImc() != null ? paciente.getImc() : BigDecimal.ZERO), celdaFont);
            agregarFilaInfo(tablaPaciente, "Dispositivo de Telemetría:", "HUAWEI Band 10 / 11 (Sensor Óptico PPG)", celdaFont);
            agregarFilaInfo(tablaPaciente, "Período Analizado:", inicio + " al " + fin, celdaFont);
            agregarFilaInfo(tablaPaciente, "Médico Evaluador:", nombreDoctor, celdaFont);
            agregarFilaInfo(tablaPaciente, "Umbrales Basales de Pulso:",
                    String.format("Mínimo: %d ppm | Máximo: %d ppm",
                            paciente.getFrecuenciaMin(), paciente.getFrecuenciaMax()), celdaFont);

            document.add(tablaPaciente);

            // Sección 2: Resumen Estadístico y Hemodinámico
            Paragraph sec2 = new Paragraph("2. Métricas y Promedios de Pulso Registrados", subtituloFont);
            sec2.setSpacingAfter(8);
            document.add(sec2);

            double promPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).average().orElse(0.0);
            int maxPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).max().orElse(0);
            int minPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).min().orElse(0);

            PdfPTable tablaMetricas = new PdfPTable(3);
            tablaMetricas.setWidthPercentage(100);
            tablaMetricas.setSpacingAfter(15);

            agregarCeldaHeader(tablaMetricas, "Parámetro Fisiológico", encabezadoTabla, new Color(225, 29, 72));
            agregarCeldaHeader(tablaMetricas, "Valor / Promedio Calculado", encabezadoTabla, new Color(225, 29, 72));
            agregarCeldaHeader(tablaMetricas, "Evaluación Clínica", encabezadoTabla, new Color(225, 29, 72));

            agregarCelda(tablaMetricas, "Frecuencia Cardíaca Promedio (Pulso)", celdaFont);
            agregarCelda(tablaMetricas, String.format("%.1f ppm", promPulso), celdaFont);
            agregarCelda(tablaMetricas, (promPulso <= paciente.getFrecuenciaMax() && promPulso >= paciente.getFrecuenciaMin()) ? "En Rango Basal Estable" : "Desviación de Umbral Detectada", celdaFont);

            agregarCelda(tablaMetricas, "Pulso Máximo Registrado", celdaFont);
            agregarCelda(tablaMetricas, String.format("%d ppm", maxPulso), celdaFont);
            agregarCelda(tablaMetricas, (maxPulso > paciente.getFrecuenciaMax()) ? "Episodio de Riesgo detectado" : "Dentro de lo Esperado", celdaFont);

            agregarCelda(tablaMetricas, "Pulso Mínimo Registrado", celdaFont);
            agregarCelda(tablaMetricas, String.format("%d ppm", minPulso), celdaFont);
            agregarCelda(tablaMetricas, (minPulso < paciente.getFrecuenciaMin()) ? "Episodio de Bradicardia" : "Dentro de lo Esperado", celdaFont);

            document.add(tablaMetricas);

            // Sección 3: Historial de Alertas Clínicas Atendidas
            Paragraph sec3 = new Paragraph("3. Registro de Alertas Clínicas Atendidas en el Período", subtituloFont);
            sec3.setSpacingAfter(8);
            document.add(sec3);

            if (alertasAtendidas.isEmpty()) {
                Paragraph sinAlertas = new Paragraph("El paciente no registra alertas críticas atendidas durante este período.", celdaFont);
                sinAlertas.setSpacingAfter(15);
                document.add(sinAlertas);
            } else {
                PdfPTable tablaAlertas = new PdfPTable(4);
                tablaAlertas.setWidthPercentage(100);
                tablaAlertas.setSpacingAfter(15);

                agregarCeldaHeader(tablaAlertas, "Fecha y Hora", encabezadoTabla, new Color(225, 29, 72));
                agregarCeldaHeader(tablaAlertas, "Tipo de Alerta", encabezadoTabla, new Color(225, 29, 72));
                agregarCeldaHeader(tablaAlertas, "Valor Registrado", encabezadoTabla, new Color(225, 29, 72));
                agregarCeldaHeader(tablaAlertas, "Observación Médica", encabezadoTabla, new Color(225, 29, 72));

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                for (AlertaClinica a : alertasAtendidas) {
                    agregarCelda(tablaAlertas, a.getFechaHora().format(dtf), celdaFont);
                    agregarCelda(tablaAlertas, "Riesgo detectado", celdaFont);
                    agregarCelda(tablaAlertas, a.getValorRegistrado(), celdaFont);
                    agregarCelda(tablaAlertas, a.getObservacionMedica() != null ? a.getObservacionMedica() : "Revisado por cardiólogo", celdaFont);
                }

                document.add(tablaAlertas);
            }

            // Firma y Conformidad del Médico que Genera el Reporte
            PdfPTable firmaTable = new PdfPTable(1);
            firmaTable.setWidthPercentage(45);
            firmaTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            firmaTable.setSpacingBefore(18);
            firmaTable.setSpacingAfter(10);

            PdfPCell celdaFirma = new PdfPCell();
            celdaFirma.setBorder(Rectangle.NO_BORDER);
            celdaFirma.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph linea = new Paragraph("____________________________________", celdaFont);
            linea.setAlignment(Element.ALIGN_CENTER);
            celdaFirma.addElement(linea);

            Paragraph docNombre = new Paragraph(nombreDoctor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(15, 23, 42)));
            docNombre.setAlignment(Element.ALIGN_CENTER);
            celdaFirma.addElement(docNombre);

            Paragraph docCargo = new Paragraph("Médico Cardiólogo · Reg. C.M.P.", pieFont);
            docCargo.setAlignment(Element.ALIGN_CENTER);
            celdaFirma.addElement(docCargo);

            firmaTable.addCell(celdaFirma);
            document.add(firmaTable);

            // Pie de página y conformidad legal
            Paragraph legal = new Paragraph("Documento generado automáticamente bajo la Ley N.° 29733 de Protección de Datos Personales en el Perú y estándar IEEE 1708-2014.", pieFont);
            legal.setAlignment(Element.ALIGN_CENTER);
            document.add(legal);

            document.close();
            return out.toByteArray();

        } catch (Exception ex) {
            throw new BadRequestException("Error al compilar y generar el reporte clínico en PDF: " + ex.getMessage());
        }
    }

    private String obtenerNombreDoctorSesion() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                String email = auth.getName();
                if ("agustinalex14@gmail.com".equalsIgnoreCase(email)) {
                    return "Dr. Agustín Aguilar";
                }
                return usuarioRepository.findByCorreo(email)
                        .map(u -> "Dr. " + (u.getCorreo().startsWith("pablo") ? "Pablo Rojas" : u.getCorreo()))
                        .orElse("Dr. Pablo Rojas");
            }
        } catch (Exception ignored) {}
        return "Dr. Pablo Rojas";
    }

    private void agregarFilaInfo(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3);
        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(3);
        table.addCell(c1);
        table.addCell(c2);
    }

    private void agregarCeldaHeader(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void agregarCelda(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
