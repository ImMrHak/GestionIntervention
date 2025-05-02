package com.gestion.intervention.adapter.web;

import com.gestion.intervention.application.disponibile.record.DisponibiliteDTO;
import com.gestion.intervention.application.disponibile.service.DisponibiliteService;
import com.gestion.intervention.application.intervention.record.InterventionDTO;
import com.gestion.intervention.application.intervention.service.InterventionService;
import com.gestion.intervention.application.technicianinfo.record.TechnicianInfoDTO; // Not directly used but relevant context
import com.gestion.intervention.application.technicianinfo.service.TechnicianInfoService;
// Import PanneService if HelpDesk needs to view/update Pannes
// import com.gestion.intervention.application.panne.service.PanneService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/helpdesk")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('HELPDESK')")
@Tag(name = "Help Desk Operations", description = "Endpoints for Help Desk staff to manage interventions (launch, assign technicians, update), manage technician availability, and search relevant information. Requires HELPDESK role.")
public class HelpDeskController {

    private final InterventionService interventionService;
    private final TechnicianInfoService technicianInfoService; // Needed for context on assignment
    private final DisponibiliteService disponibiliteService; // Needed for managing availability
    // private final PanneService panneService; // If needed

    // == Launch Intervention (Lancer une intervention) ==
    @PostMapping("/interventions")
    @Operation(summary = "HelpDesk: Launch a new intervention",
            description = "Creates a new intervention record, typically linked to an existing breakdown (Panne). The Help Desk agent fills in the initial details. Requires HELPDESK role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Intervention launched successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InterventionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., validation errors, missing Panne ID)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Related Panne not found", content = @Content), // If Panne ID validation is done here
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<InterventionDTO> launchIntervention(
            @Parameter(description = "Details of the intervention to launch. Must include a valid `panneId`.", required = true, schema = @Schema(implementation = InterventionDTO.class))
            @Valid @RequestBody InterventionDTO dto) {
        // DTO should contain Panne ID. HelpDesk selects/confirms details.
        InterventionDTO createdIntervention = interventionService.createIntervention(dto);
        return new ResponseEntity<>(createdIntervention, HttpStatus.CREATED);
    }

    // == Assign Technician (Assigner technicien) ==
    @PutMapping("/interventions/{interventionId}/assign")
    @Operation(summary = "HelpDesk: Assign a technician to an intervention",
            description = "Assigns a specific technician to an existing intervention. The service layer should ideally verify technician availability before assignment. Requires HELPDESK role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Technician assigned successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InterventionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., technician unavailable, invalid IDs)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Intervention or Technician not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<InterventionDTO> assignTechnician(
            @Parameter(description = "Unique ID of the intervention to update", required = true, example = "623e4567-e89b-12d3-a456-426614174005")
            @PathVariable UUID interventionId,
            @Parameter(description = "Unique ID of the technician to assign", required = true, example = "723e4567-e89b-12d3-a456-426614174006")
            @RequestParam UUID technicianId) {
        // Logic as described in original code: Fetch, create updated DTO, call update service
        InterventionDTO intervention = interventionService.getInterventionById(interventionId);
        // Basic update DTO creation - service should handle merge/validation logic
        InterventionDTO updatedDto = new InterventionDTO(
                intervention.id(),
                intervention.dateDebut(),
                intervention.dateFin(),
                intervention.duree(),
                technicianId, // Assigning the new technician
                intervention.panneId()
        );
        InterventionDTO assignedIntervention = interventionService.updateIntervention(interventionId, updatedDto); // Service should handle availability check
        return ResponseEntity.ok(assignedIntervention);
    }

    // General Intervention Update
    @PutMapping("/interventions/{id}")
    @Operation(summary = "HelpDesk: Update intervention details (e.g., assign technician, status, dates)",
            description = "Allows updating various fields of an existing intervention, including assigning a technician (alternative to the dedicated assign endpoint). Requires HELPDESK role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intervention updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InterventionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., validation errors, inconsistent dates)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Intervention not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<InterventionDTO> updateIntervention(
            @Parameter(description = "Unique ID of the intervention to update", required = true, example = "623e4567-e89b-12d3-a456-426614174005")
            @PathVariable UUID id,
            @Parameter(description = "Updated details of the intervention", required = true, schema = @Schema(implementation = InterventionDTO.class))
            @Valid @RequestBody InterventionDTO dto) {
        InterventionDTO updatedIntervention = interventionService.updateIntervention(id, dto);
        return ResponseEntity.ok(updatedIntervention);
    }

    // == View Interventions ==
    @GetMapping("/interventions")
    @Operation(summary = "HelpDesk: Get all interventions (potentially filtered)",
            description = "Retrieves a list of all interventions. Can be optionally filtered by status (e.g., 'PENDING', 'IN_PROGRESS', 'COMPLETED'). Requires HELPDESK role. **Filtering logic needs implementation.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of interventions retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = InterventionDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<InterventionDTO>> getAllInterventions(
            @Parameter(description = "Optional: Filter interventions by status", required = false, example = "PENDING")
            @RequestParam(required = false) String status) {
        // TODO: Implement filtering logic in InterventionService based on status
        List<InterventionDTO> interventions = interventionService.getAllInterventions(); // Currently unfiltered
        return ResponseEntity.ok(interventions);
    }

    @GetMapping("/interventions/{id}")
    @Operation(summary = "HelpDesk: Get a specific intervention",
            description = "Retrieves details of a specific intervention by its unique ID. Requires HELPDESK role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intervention details found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InterventionDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Intervention not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<InterventionDTO> getInterventionById(
            @Parameter(description = "Unique ID of the intervention to retrieve", required = true, example = "623e4567-e89b-12d3-a456-426614174005")
            @PathVariable UUID id) {
        return ResponseEntity.ok(interventionService.getInterventionById(id));
    }


    // == Search ==
    @GetMapping("/search")
    @Operation(summary = "HelpDesk: Search across various relevant entities",
            description = "Performs a search based on a query string across specified entity types relevant to Help Desk (e.g., 'technician', 'employee', 'machine', 'panne', 'intervention'). Requires HELPDESK role. **NOTE: Currently returns a placeholder.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results (currently placeholder text)",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters (e.g., missing query or type)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "501", description = "Search endpoint not yet implemented",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Object> search(
            @Parameter(description = "The search term or query string", required = true, example = "Technician Smith")
            @RequestParam String query,
            @Parameter(description = "The type of entity to search within (e.g., 'technician', 'panne', 'intervention')", required = true, example = "technician")
            @RequestParam String type) {
        // Implement broader search capabilities for HelpDesk
        // Object results = searchService.search(query, type, UserRole.HELPDESK);
        // Placeholder:
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("HelpDesk Search for type '" + type + "' with query '" + query + "' not implemented.");
    }

    // == Manage Technician Availability ==
    @GetMapping("/technicians/availability")
    @Operation(summary = "HelpDesk: View technician availability slots",
            description = "Retrieves a list of availability slots for technicians. Can be optionally filtered by a specific technician's ID. Requires HELPDESK role. **Filtering logic needs implementation.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of availability slots retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DisponibiliteDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<DisponibiliteDTO>> getTechnicianAvailability(
            @Parameter(description = "Optional: Filter availability by a specific technician's ID", required = false, example = "723e4567-e89b-12d3-a456-426614174006")
            @RequestParam(required = false) UUID technicianId) {
        // TODO: Implement filtering logic in DisponibiliteService based on technicianId
        List<DisponibiliteDTO> dispos = disponibiliteService.getAllDisponibilites(); // Currently unfiltered
        return ResponseEntity.ok(dispos);
    }

    @PostMapping("/technicians/availability")
    @Operation(summary = "HelpDesk: Create an availability slot for a technician",
            description = "Allows Help Desk to add a new availability time slot for a specific technician. Requires HELPDESK role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Availability slot created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DisponibiliteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., validation errors, overlapping times)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Technician specified in DTO not found", content = @Content), // If technician ID validation happens here
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<DisponibiliteDTO> createAvailability(
            @Parameter(description = "Details of the availability slot to create. Must include `technicienId`.", required = true, schema = @Schema(implementation = DisponibiliteDTO.class))
            @Valid @RequestBody DisponibiliteDTO dto) {
        return new ResponseEntity<>(disponibiliteService.createDisponibilite(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/technicians/availability/{id}")
    @Operation(summary = "HelpDesk: Delete an availability slot",
            description = "Deletes a specific availability slot by its unique ID. Requires HELPDESK role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Availability slot deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have HELPDESK role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Availability slot not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteAvailability(
            @Parameter(description = "Unique ID of the availability slot to delete", required = true, example = "823e4567-e89b-12d3-a456-426614174007")
            @PathVariable UUID id) {
        disponibiliteService.deleteDisponibilite(id);
        return ResponseEntity.noContent().build();
    }
}