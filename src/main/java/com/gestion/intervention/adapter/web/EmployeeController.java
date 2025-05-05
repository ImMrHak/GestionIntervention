package com.gestion.intervention.adapter.web;

import com.gestion.intervention.adapter.wrapper.ResponseWrapper;
import com.gestion.intervention.application.intervention.record.InterventionDTO;
import com.gestion.intervention.application.intervention.service.InterventionService;
import com.gestion.intervention.application.panne.record.PanneDTO;
import com.gestion.intervention.application.panne.service.PanneService;
// Import security utils to get current user
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
import com.gestion.intervention.domain.panne.enumeration.PanneStatus;
import com.gestion.intervention.kernel.security.jwt.userPrincipal.UserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('EMPLOYEE')")
@Tag(name = "Employee Operations", description = "Endpoints for employees to report and track breakdowns (pannes), view relevant intervention history, and perform limited searches. Requires EMPLOYEE role.")
public class EmployeeController {

    private final PanneService panneService;
    private final InterventionService interventionService;
    // Inject MachineService/PieceService if needed for searching

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboardData(@AuthenticationPrincipal UserPrincipal userPrincipal){
        Integer totalPendingPannes = panneService.getTotalPannesByStatus(PanneStatus.PENDING, userPrincipal.id());
        Integer totalResolvedPannes = panneService.getTotalPannesByStatus(PanneStatus.RESOLVED, userPrincipal.id());
        Integer totalPannes = panneService.getTotalPannesByReporterId(userPrincipal.id());

        Map<String, Integer> response = new HashMap<>();
        response.put("totalPending", totalPendingPannes);
        response.put("totalResolved", totalResolvedPannes);
        response.put("totalPannes", totalPannes);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // == Report Panne (Réclamer Panne) ==
    @PostMapping("/pannes")
    @Operation(summary = "Employee: Report a new breakdown (panne)",
            description = "Allows an authenticated employee to report a new breakdown. Ideally, the system should automatically associate the report with the logged-in employee. Requires EMPLOYEE role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Breakdown reported successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PanneDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., validation errors)",
                    content = @Content),
            // Add 401/403 if security is enabled
            @ApiResponse(responseCode = "401", description = "Unauthorized - Employee not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have EMPLOYEE role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PanneDTO> reportPanne(
            @Parameter(description = "Details of the breakdown to report. The reporter ID might be ignored if set automatically from security context.", required = true, schema = @Schema(implementation = PanneDTO.class))
            @Valid @RequestBody PanneDTO dto) {
        // Ideally, set the reporterId from the authenticated user context
        // UUID reporterId = getCurrentUserId(); // Implement this helper
        // PanneDTO dtoWithReporter = new PanneDTO(dto.id(), dto.typePanne(), dto.machineId(), reporterId);
        // PanneDTO createdPanne = panneService.createPanne(dtoWithReporter);

        // Using provided DTO directly for now (as per original code):
        // **Note:** This assumes the DTO might contain the reporter ID or the service handles it.
        // The Swagger doc assumes the service might auto-set the reporter.
        PanneDTO createdPanne = panneService.createPanne(dto);
        return new ResponseEntity<>(createdPanne, HttpStatus.CREATED);
    }

    // == Follow Panne (Suivre Panne) ==
    @GetMapping("/pannes/{id}")
    @Operation(summary = "Employee: Get details of a specific breakdown",
            description = "Retrieves details of a specific breakdown by its ID. **Access control needed:** Should typically only allow access to pannes reported by the employee or related to their work area. Requires EMPLOYEE role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Breakdown details found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PanneDTO.class))),
            @ApiResponse(responseCode = "404", description = "Breakdown not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Employee not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Employee does not have permission to view this breakdown", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PanneDTO> getMyPanneById(
            @Parameter(description = "Unique ID of the breakdown to retrieve", required = true, example = "523e4567-e89b-12d3-a456-426614174004")
            @PathVariable UUID id) {
        // Add authorization logic here or in the service to ensure
        // the employee can only view pannes they reported OR pannes on machines they might use.
        PanneDTO panne = panneService.getPanneById(id);
        // performAuthorizationCheck(panne); // Check ownership/permissions
        // Current implementation returns the panne without specific auth check as per original code.
        return ResponseEntity.ok(panne);
    }

    @GetMapping("/pannes")
    @Operation(summary = "Employee: Get list of breakdowns reported by the employee",
            description = "Retrieves a list of breakdowns. **Filtering needed:** This should ideally be filtered to show only pannes reported by the currently authenticated employee. Requires EMPLOYEE role. **NOTE: Currently returns all pannes.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of breakdowns retrieved (currently unfiltered)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PanneDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Employee not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have EMPLOYEE role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<PanneDTO>> getMyPannes() {
        // UUID reporterId = getCurrentUserId(); // Implement this helper
        // List<PanneDTO> pannes = panneService.getPannesByReporter(reporterId); // Requires service method

        // Placeholder: returning all for now, needs filtering
        List<PanneDTO> pannes = panneService.getAllPannes();
        return ResponseEntity.ok(pannes);
    }

    // == Visualize Intervention History (Visualiser historique des interventions) ==
    @GetMapping("/interventions/history")
    @Operation(summary = "Employee: View history of interventions relevant to the employee",
            description = "Retrieves a list of past interventions. **Filtering logic needed:** The definition of 'relevant' needs clarification (e.g., interventions for pannes reported by the employee, interventions on machines in their area). Requires EMPLOYEE role. **NOTE: Currently returns all interventions.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of interventions retrieved (currently unfiltered)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = InterventionDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Employee not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have EMPLOYEE role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<InterventionDTO>> getMyInterventionHistory() {
        // This needs clarification: History related to pannes they reported?
        // Or interventions on machines they use? Requires specific service logic.
        // UUID employeeId = getCurrentUserId();
        // List<InterventionDTO> history = interventionService.getInterventionHistoryForEmployee(employeeId);

        // Placeholder: returning all for now, needs filtering/specific logic
        List<InterventionDTO> history = interventionService.getAllInterventions();
        return ResponseEntity.ok(history);
    }

    // == Search (Chercher - limited scope, e.g., Machine, Panne) ==
    @GetMapping("/search")
    @Operation(summary = "Employee: Search for relevant items (e.g., machines, own pannes)",
            description = "Performs a search based on a query string across specified entity types relevant to an employee (e.g., 'machine', 'panne'). **Scope restriction needed:** Search results should be limited to items the employee has permission to view. Requires EMPLOYEE role. **NOTE: Currently returns a placeholder.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results (currently placeholder text)",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))), // Placeholder returns text
            @ApiResponse(responseCode = "400", description = "Invalid search parameters (e.g., missing query or type)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Employee not logged in", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have EMPLOYEE role", content = @Content),
            @ApiResponse(responseCode = "501", description = "Search endpoint not yet implemented",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Object> search(
            @Parameter(description = "The search term or query string", required = true, example = "Pump 101")
            @RequestParam String query,
            @Parameter(description = "The type of entity to search within (e.g., 'machine', 'panne')", required = true, example = "machine")
            @RequestParam String type) {
        // Implement search logic with employee scope restrictions
        // Object results = searchService.search(query, type, UserRole.EMPLOYEE, getCurrentUserId());

        // Placeholder:
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Employee Search for type '" + type + "' with query '" + query + "' not implemented.");
        // return ResponseEntity.ok("Employee Search for type '" + type + "' with query '" + query + "' not implemented."); // Alternative placeholder
    }

    // Helper method placeholder (implement properly using Spring Security)
    /*
    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
             // Assuming your UserDetails implementation has a method to get the UUID
             // return ((MyUserDetails) principal).getId();
             return UUID.randomUUID(); // Replace with actual logic
        }
        // Handle cases where user is not found or principal is not UserDetails
        throw new IllegalStateException("User not authenticated");
    }
    */
}