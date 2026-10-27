package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.dto.auth.MessageResponse;
import pe.edu.upc.cardio.api.dto.contacto.ContactoAuxilioRequest;
import pe.edu.upc.cardio.api.dto.contacto.ContactoAuxilioResponseDTO;
import pe.edu.upc.cardio.api.service.ContactoAuxilioService;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes/{idPaciente}/contactos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Contactos de Auxilio", description = "Gestión de contactos de emergencia familiar (HU018)")
public class ContactoAuxilioController {

    private final ContactoAuxilioService contactoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CARDIOLOGO', 'PACIENTE')")
    @Operation(summary = "Listar contactos de auxilio asignados al paciente")
    public ResponseEntity<List<ContactoAuxilioResponseDTO>> listar(@PathVariable("idPaciente") Long idPaciente) {
        return ResponseEntity.ok(contactoService.listarPorPaciente(idPaciente));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CARDIOLOGO', 'PACIENTE')")
    @Operation(summary = "Registrar nuevo contacto familiar de auxilio (9 dígitos)", description = "HU018")
    public ResponseEntity<ContactoAuxilioResponseDTO> registrar(
            @PathVariable("idPaciente") Long idPaciente,
            @Valid @RequestBody ContactoAuxilioRequest request) {
        ContactoAuxilioResponseDTO nuevo = contactoService.registrarContacto(idPaciente, request);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PutMapping("/{idContacto}")
    @PreAuthorize("hasAnyRole('CARDIOLOGO', 'PACIENTE')")
    @Operation(summary = "Actualizar contacto de auxilio existente", description = "HU018")
    public ResponseEntity<ContactoAuxilioResponseDTO> actualizar(
            @PathVariable("idPaciente") Long idPaciente,
            @PathVariable("idContacto") Long idContacto,
            @Valid @RequestBody ContactoAuxilioRequest request) {
        return ResponseEntity.ok(contactoService.actualizarContacto(idPaciente, idContacto, request));
    }

    @DeleteMapping("/{idContacto}")
    @PreAuthorize("hasAnyRole('CARDIOLOGO', 'PACIENTE')")
    @Operation(summary = "Eliminar contacto de auxilio")
    public ResponseEntity<MessageResponse> eliminar(
            @PathVariable("idPaciente") Long idPaciente,
            @PathVariable("idContacto") Long idContacto) {
        contactoService.eliminarContacto(idPaciente, idContacto);
        return ResponseEntity.ok(MessageResponse.builder()
                .exitoso(true)
                .mensaje("Contacto de auxilio eliminado correctamente")
                .build());
    }
}
