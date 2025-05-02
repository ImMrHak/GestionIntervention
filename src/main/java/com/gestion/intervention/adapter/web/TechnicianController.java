package com.gestion.intervention.adapter.web;

import com.gestion.intervention.application.intervention.record.InterventionDTO;
import com.gestion.intervention.application.intervention.service.InterventionService;
import com.gestion.intervention.application.panne.record.PanneDTO;
import com.gestion.intervention.application.panne.service.PanneService;
// Import DisponibiliteService if technicians manage their own availability
// import com.gestion.intervention.application.disponibile.record.DisponibiliteDTO;
// import com.gestion.intervention.application.disponibile.service.DisponibiliteService;

// Import security utils to get current user ID
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
// Import Duration if using that type
// import java.time.Duration;

@RestController
@RequestMapping("/api/v1/technician")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('TECHNICIAN')")
@Tag(name = "Technician Operations", description = "Endpoints for technicians to manage assigned interventions and breakdowns, update details, and potentially manage availability. Requires TECHNICIAN role.")
public class TechnicianController {

    private final PanneService panneService;
    private final InterventionService interventionService;
    // private final DisponibiliteService disponibiliteService; // If needed

    // == Fill Panne Info (Remplir les infos de la panne) ==
    @PutMapping("/pannes/{id}")
    @Operation(summary = "Technician: Update breakdown details (e.g., diagnostics, status)",
            description = "Allows an assigned technician to update details of a breakdown (Panne), potentially adding diagnostic information or changing its status. **Authorization Required:** The system must ensure the technician is authorized (e.g., via an assigned intervention) to modify this specific panne. Requires TECHNICIAN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Breakdown details updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PanneDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., validation errors)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Technician not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Technician not authorized to update this panne or does not have TECHNICIAN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Breakdown (Panne) not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PanneDTO> updatePanneInfo(
            @Parameter(description = "Unique ID of the breakdown (Panne) to update", required = true, example = "523e4567-e89b-12d3-a456-426614174004")
            @PathVariable UUID id,
            @Parameter(description = "Updated details of the breakdown", required = true, schema = @Schema(implementation = PanneDTO.class))
            @Valid @RequestBody PanneDTO dto) {
        // Ensure technician is authorized to update this specific panne (e.g., assigned via intervention)
        // performAuthorizationCheckForPanne(id);
        PanneDTO updatedPanne = panneService.updatePanne(id, dto);
        return ResponseEntity.ok(updatedPanne);
    }

    // == Estimate Intervention Duration (Estimer la durée de l'intervention) ==
    // Commented out as per original code, but showing how it *would* be annotated.
    /*
    @PutMapping("/interventions/{id}/estimate")
    @Operation(summary = "Technician: Estimate or update the duration of an assigned intervention",
               description = "Allows the assigned technician to provide or update an estimated duration for the intervention. **Authorization Required:** Technician must be assigned to the intervention. Requires TECHNICIAN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Intervention duration updated successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InterventionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input (e.g., invalid duration format)", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not assigned to this intervention or not TECHNICIAN role", content = @Content),
        @ApiResponse(responseCode = "404", description = "Intervention not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<InterventionDTO> estimateInterventionDuration(
            @Parameter(description = "Unique ID of the intervention", required = true, example = "623e4567-e89b-12d3-a456-426614174005")
            @PathVariable UUID id,
            @Parameter(description = "Estimated duration (e.g., 'PT2H30M' for ISO-8601, or simple string like '2.5 hours' - define format)", required = true, schema = @Schema(type="string", example="PT2H"))
            @RequestBody String estimatedDuration) {
        // ... implementation ...
        InterventionDTO intervention = interventionService.getInterventionById(id);
        // Example update DTO creation
        InterventionDTO updatedDto = new InterventionDTO(
                intervention.id(), intervention.dateDebut(), intervention.dateFin(),
                estimatedDuration, // Assuming String storage for simplicity here
                intervention.technicianId(), intervention.panneId()
        );
        return ResponseEntity.ok(interventionService.updateIntervention(id, updatedDto));
    }
    */

    // General Intervention Update
    @PutMapping("/interventions/{id}")
    @Operation(summary = "Technician: Update assigned intervention details (status, notes, dates, duration)",
            description = "Allows the assigned technician to update details of their intervention, such as changing the status (e.g., 'IN_PROGRESS', 'COMPLETED'), adding notes, setting start/end dates, or providing/updating the duration. **Authorization Required:** Technician must be assigned to this intervention. Requires TECHNICIAN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intervention updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InterventionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., validation errors, invalid status transition)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Technician not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not assigned to this intervention or does not have TECHNICIAN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Intervention not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<InterventionDTO> updateIntervention(
            @Parameter(description = "Unique ID of the intervention to update", required = true, example = "623e4567-e89b-12d3-a456-426614174005")
            @PathVariable UUID id,
            @Parameter(description = "Updated details of the intervention. The technician should only modify fields they are responsible for (e.g., status, dates, duration, notes).", required = true, schema = @Schema(implementation = InterventionDTO.class))
            @Valid @RequestBody InterventionDTO dto) {
        // Ensure technician is assigned to this intervention and authorized for changes
        // performAuthorizationCheckForIntervention(id);
        // Ensure the DTO doesn't try to change fields the technician shouldn't (like panneId or assignment) - ideally handled in service layer.
        InterventionDTO updatedIntervention = interventionService.updateIntervention(id, dto);
        return ResponseEntity.ok(updatedIntervention);
    }

    // == View Assigned Interventions ==
    @GetMapping("/interventions/assigned")
    @Operation(summary = "Technician: Get interventions assigned to the current technician",
            description = "Retrieves a list of interventions currently assigned to the authenticated technician. **Filtering needed:** Requires mapping the logged-in user to their technician profile and filtering interventions accordingly. Requires TECHNICIAN role. **NOTE: Currently returns all interventions.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of assigned interventions retrieved (currently unfiltered)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = InterventionDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Technician not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have TECHNICIAN role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error (e.g., error mapping user to technician)",
                    content = @Content)
    })
    public ResponseEntity<List<InterventionDTO>> getMyAssignedInterventions() {
        // UUID technicianUserId = getCurrentUserId(); // Get current technician's *user* ID
        // Find TechnicianInfo ID based on user ID
        // UUID technicianInfoId = technicianInfoService.findInfoIdByUserId(technicianUserId); // Requires service method
        // List<InterventionDTO> interventions = interventionService.getInterventionsByTechnician(technicianInfoId); // Requires service method

        // Placeholder: returning all for now, needs filtering
        List<InterventionDTO> interventions = interventionService.getAllInterventions();
        return ResponseEntity.ok(interventions);
    }

    @GetMapping("/interventions/{id}")
    @Operation(summary = "Technician: Get details of a specific assigned intervention",
            description = "Retrieves details of a specific intervention by its ID. **Authorization Required:** The system must verify that the intervention is assigned to the currently authenticated technician. Requires TECHNICIAN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Intervention details found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InterventionDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Technician not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Intervention not assigned to this technician or user does not have TECHNICIAN role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Intervention not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<InterventionDTO> getMyInterventionById(
            @Parameter(description = "Unique ID of the intervention to retrieve", required = true, example = "623e4567-e89b-12d3-a456-426614174005")
            @PathVariable UUID id) {
        // Ensure technician is assigned to this intervention
        // performAuthorizationCheckForIntervention(id);
        return ResponseEntity.ok(interventionService.getInterventionById(id));
    }

    // == Search ==
    @GetMapping("/search")
    @Operation(summary = "Technician: Search relevant items (assigned work, parts, etc.)",
            description = "Performs a search based on a query string across specified entity types relevant to a technician (e.g., 'intervention', 'panne', 'piece'). **Scope restriction needed:** Search results should be limited to items the technician has permission to view (e.g., assigned interventions, pannes they worked on, pieces for machines they service). Requires TECHNICIAN role. **NOTE: Currently returns a placeholder.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results (currently placeholder text)",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters (e.g., missing query or type)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Technician not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have TECHNICIAN role", content = @Content),
            @ApiResponse(responseCode = "501", description = "Search endpoint not yet implemented",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Object> search(
            @Parameter(description = "The search term or query string", required = true, example = "Bolt M6")
            @RequestParam String query,
            @Parameter(description = "The type of entity to search within (e.g., 'intervention', 'piece', 'panne')", required = true, example = "piece")
            @RequestParam String type) {
        // Implement search logic scoped to technician's context
        // Object results = searchService.search(query, type, UserRole.TECHNICIAN, getCurrentUserId());
        // Placeholder:
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Technician Search for type '" + type + "' with query '" + query + "' not implemented.");
    }

    // == Manage Own Availability (If technicians do this themselves) ==
     /*
     // --- If technicians manage their own availability, these endpoints would need annotations similar to below ---

     @GetMapping("/availability")
     @Operation(summary = "Technician: View own availability slots",
                description = "Retrieves the availability slots registered for the currently authenticated technician. Requires TECHNICIAN role.")
     @ApiResponses(...) // Add responses: 200 OK, 401, 403, 500
     public ResponseEntity<List<DisponibiliteDTO>> getMyAvailability() {
         // ... implementation ...
     }

     @PostMapping("/availability")
     @Operation(summary = "Technician: Create a new availability slot for self",
                description = "Allows the technician to add a new time slot where they are available. Requires TECHNICIAN role.")
     @ApiResponses(...) // Add responses: 201 Created, 400 Bad Request (overlap/invalid), 401, 403, 500
     public ResponseEntity<DisponibiliteDTO> addMyAvailability(
         @Parameter(description = "Details of the availability slot. Technician ID might be auto-set.", required = true, schema = @Schema(implementation = DisponibiliteDTO.class))
         @Valid @RequestBody DisponibiliteDTO dto) {
         // ... implementation ...
     }

     @DeleteMapping("/availability/{id}")
     @Operation(summary = "Technician: Delete own availability slot",
                description = "Allows the technician to delete one of their own availability slots. **Authorization Required:** Ensures the slot belongs to the technician. Requires TECHNICIAN role.")
     @ApiResponses(...) // Add responses: 204 No Content, 401, 403 (not owner), 404 Not Found, 500
     public ResponseEntity<Void> deleteMyAvailability(
         @Parameter(description = "Unique ID of the availability slot to delete", required = true, example = "823e4567-e89b-12d3-a456-426614174007")
         @PathVariable UUID id) {
         // ... implementation ...
     }
     */

    // Placeholder helpers - Implement using Spring Security and potentially TechnicianInfoService
     /*
     private void performAuthorizationCheckForPanne(UUID panneId) { }
     private void performAuthorizationCheckForIntervention(UUID interventionId) { }
     private void performAuthorizationCheckForAvailability(UUID disponibiliteId) { }
     private UUID getCurrentUserId() { return UUID.randomUUID(); } // Implement properly
     private UUID getCurrentTechnicianInfoId() { return UUID.randomUUID(); } // Implement properly (map user ID to technician info ID)
     */
}