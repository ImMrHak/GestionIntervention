package com.gestion.intervention.adapter.web;

import com.gestion.intervention.application.machine.record.MachineDTO;
import com.gestion.intervention.application.machine.service.MachineService;
import com.gestion.intervention.application.person.record.PersonDTO;
import com.gestion.intervention.application.person.service.PersonService;
import com.gestion.intervention.application.piece.record.PieceDTO;
import com.gestion.intervention.application.piece.service.PieceService;
import com.gestion.intervention.application.stemaintenance.record.STEmaintenanceDTO;
import com.gestion.intervention.application.stemaintenance.service.STEmaintenanceService;
// Import other services if needed for statistics, etc.

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
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('ADMIN')") // Apply authorization at the class level
@Tag(name = "Admin Operations", description = "Endpoints for managing users, machines, pieces, STE maintenance companies, and viewing statistics. Requires ADMIN role.")
public class AdminController {

    private final PersonService personService;
    private final MachineService machineService;
    private final PieceService pieceService;
    private final STEmaintenanceService steMaintenanceService;
    // Inject statistics service if created

    // == User Management (Gérer utilisateur) ==
    @PostMapping("/users")
    @Operation(summary = "Admin: Create a new user (person)", description = "Creates a new user record in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided (e.g., validation errors)",
                    content = @Content),
            // Add 401/403 if security is enabled
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PersonDTO> createUser(
            @Parameter(description = "Details of the user to create", required = true, schema = @Schema(implementation = PersonDTO.class))
            @Valid @RequestBody PersonDTO dto) {
        PersonDTO createdPerson = personService.createPerson(dto);
        return new ResponseEntity<>(createdPerson, HttpStatus.CREATED);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Admin: Get user by ID", description = "Retrieves details of a specific user by their unique ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content),
            // Add 401/403 if security is enabled
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PersonDTO> getUserById(
            @Parameter(description = "Unique ID of the user to retrieve", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    @GetMapping("/users")
    @Operation(summary = "Admin: Get all users", description = "Retrieves a list of all users registered in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PersonDTO.class)))),
            // Add 401/403 if security is enabled
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<PersonDTO>> getAllUsers() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Admin: Update user details", description = "Updates the details of an existing user identified by their ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content),
            // Add 401/403 if security is enabled
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PersonDTO> updateUser(
            @Parameter(description = "Unique ID of the user to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Updated details of the user", required = true, schema = @Schema(implementation = PersonDTO.class))
            @Valid @RequestBody PersonDTO dto) {
        return ResponseEntity.ok(personService.updatePerson(id, dto));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Admin: Delete user", description = "Deletes a user from the system based on their ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID",
                    content = @Content),
            // Add 401/403 if security is enabled
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Unique ID of the user to delete", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    // == Machine Management (Gérer machines) ==
    @PostMapping("/machines")
    @Operation(summary = "Admin: Create a new machine", description = "Creates a new machine record in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Machine created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MachineDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<MachineDTO> createMachine(
            @Parameter(description = "Details of the machine to create", required = true, schema = @Schema(implementation = MachineDTO.class))
            @Valid @RequestBody MachineDTO dto) {
        return new ResponseEntity<>(machineService.createMachine(dto), HttpStatus.CREATED);
    }

    @GetMapping("/machines/{id}")
    @Operation(summary = "Admin: Get machine by ID", description = "Retrieves details of a specific machine by its unique ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Machine found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MachineDTO.class))),
            @ApiResponse(responseCode = "404", description = "Machine not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<MachineDTO> getMachineById(
            @Parameter(description = "Unique ID of the machine to retrieve", required = true, example = "223e4567-e89b-12d3-a456-426614174001")
            @PathVariable UUID id) {
        return ResponseEntity.ok(machineService.getMachineById(id));
    }

    @GetMapping("/machines")
    @Operation(summary = "Admin: Get all machines", description = "Retrieves a list of all machines registered in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of machines retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = MachineDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<MachineDTO>> getAllMachines() {
        return ResponseEntity.ok(machineService.getAllMachines());
    }

    @PutMapping("/machines/{id}")
    @Operation(summary = "Admin: Update machine", description = "Updates the details of an existing machine identified by its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Machine updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MachineDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Machine not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<MachineDTO> updateMachine(
            @Parameter(description = "Unique ID of the machine to update", required = true, example = "223e4567-e89b-12d3-a456-426614174001")
            @PathVariable UUID id,
            @Parameter(description = "Updated details of the machine", required = true, schema = @Schema(implementation = MachineDTO.class))
            @Valid @RequestBody MachineDTO dto) {
        return ResponseEntity.ok(machineService.updateMachine(id, dto));
    }

    @DeleteMapping("/machines/{id}")
    @Operation(summary = "Admin: Delete machine", description = "Deletes a machine from the system based on its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Machine deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Machine not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteMachine(
            @Parameter(description = "Unique ID of the machine to delete", required = true, example = "223e4567-e89b-12d3-a456-426614174001")
            @PathVariable UUID id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    // == Piece Management (Gérer pièces) ==
    @PostMapping("/pieces")
    @Operation(summary = "Admin: Create a new piece", description = "Creates a new piece record in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Piece created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PieceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PieceDTO> createPiece(
            @Parameter(description = "Details of the piece to create", required = true, schema = @Schema(implementation = PieceDTO.class))
            @Valid @RequestBody PieceDTO dto) {
        return new ResponseEntity<>(pieceService.createPiece(dto), HttpStatus.CREATED);
    }

    @GetMapping("/pieces/{id}")
    @Operation(summary = "Admin: Get piece by ID", description = "Retrieves details of a specific piece by its unique ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Piece found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PieceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Piece not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PieceDTO> getPieceById(
            @Parameter(description = "Unique ID of the piece to retrieve", required = true, example = "323e4567-e89b-12d3-a456-426614174002")
            @PathVariable UUID id) {
        return ResponseEntity.ok(pieceService.getPieceById(id));
    }

    @GetMapping("/pieces")
    @Operation(summary = "Admin: Get all pieces", description = "Retrieves a list of all pieces, optionally filtered by machine ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of pieces retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PieceDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<PieceDTO>> getAllPieces(
            @Parameter(description = "Optional: Filter pieces by the ID of the machine they belong to", required = false, example = "223e4567-e89b-12d3-a456-426614174001")
            @RequestParam(required = false) UUID machineId) {
        // TODO: Implement filtering logic in PieceService based on machineId if provided
        return ResponseEntity.ok(pieceService.getAllPieces());
    }

    @PutMapping("/pieces/{id}")
    @Operation(summary = "Admin: Update piece", description = "Updates the details of an existing piece identified by its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Piece updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PieceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Piece not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<PieceDTO> updatePiece(
            @Parameter(description = "Unique ID of the piece to update", required = true, example = "323e4567-e89b-12d3-a456-426614174002")
            @PathVariable UUID id,
            @Parameter(description = "Updated details of the piece", required = true, schema = @Schema(implementation = PieceDTO.class))
            @Valid @RequestBody PieceDTO dto) {
        return ResponseEntity.ok(pieceService.updatePiece(id, dto));
    }

    @DeleteMapping("/pieces/{id}")
    @Operation(summary = "Admin: Delete piece", description = "Deletes a piece from the system based on its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Piece deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Piece not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Void> deletePiece(
            @Parameter(description = "Unique ID of the piece to delete", required = true, example = "323e4567-e89b-12d3-a456-426614174002")
            @PathVariable UUID id) {
        pieceService.deletePiece(id);
        return ResponseEntity.noContent().build();
    }

    // == STE Maintenance Management (Préciser STE maintenance) ==
    @PostMapping("/stemaintenance")
    @Operation(summary = "Admin: Create a new STE Maintenance company", description = "Creates a new STE Maintenance company record. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "STE Maintenance company created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = STEmaintenanceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<STEmaintenanceDTO> createSTE(
            @Parameter(description = "Details of the STE Maintenance company to create", required = true, schema = @Schema(implementation = STEmaintenanceDTO.class))
            @Valid @RequestBody STEmaintenanceDTO dto) {
        return new ResponseEntity<>(steMaintenanceService.createSTEmaintenance(dto), HttpStatus.CREATED);
    }

    @PutMapping("/stemaintenance/{id}")
    @Operation(summary = "Admin: Update STE Maintenance company", description = "Updates the details of an existing STE Maintenance company. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "STE Maintenance company updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = STEmaintenanceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "STE Maintenance company not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<STEmaintenanceDTO> updateSTE(
            @Parameter(description = "Unique ID of the STE Maintenance company to update", required = true, example = "423e4567-e89b-12d3-a456-426614174003")
            @PathVariable UUID id,
            @Parameter(description = "Updated details of the STE Maintenance company", required = true, schema = @Schema(implementation = STEmaintenanceDTO.class))
            @Valid @RequestBody STEmaintenanceDTO dto) {
        return ResponseEntity.ok(steMaintenanceService.updateSTEmaintenance(id, dto));
    }

    @GetMapping("/stemaintenance/{id}")
    @Operation(summary = "Admin: Get STE Maintenance company by ID", description = "Retrieves details of a specific STE Maintenance company by its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "STE Maintenance company found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = STEmaintenanceDTO.class))),
            @ApiResponse(responseCode = "404", description = "STE Maintenance company not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<STEmaintenanceDTO> getSTEById(
            @Parameter(description = "Unique ID of the STE Maintenance company to retrieve", required = true, example = "423e4567-e89b-12d3-a456-426614174003")
            @PathVariable UUID id) {
        return ResponseEntity.ok(steMaintenanceService.getSTEmaintenanceById(id));
    }

    @GetMapping("/stemaintenance")
    @Operation(summary = "Admin: Get all STE Maintenance companies", description = "Retrieves a list of all STE Maintenance companies. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of STE Maintenance companies retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = STEmaintenanceDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<STEmaintenanceDTO>> getAllSTEs() {
        return ResponseEntity.ok(steMaintenanceService.getAllSTEmaintenances());
    }

    @DeleteMapping("/stemaintenance/{id}")
    @Operation(summary = "Admin: Delete STE Maintenance company", description = "Deletes an STE Maintenance company based on its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "STE Maintenance company deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "STE Maintenance company not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteSTE(
            @Parameter(description = "Unique ID of the STE Maintenance company to delete", required = true, example = "423e4567-e89b-12d3-a456-426614174003")
            @PathVariable UUID id) {
        steMaintenanceService.deleteSTEmaintenance(id);
        return ResponseEntity.noContent().build();
    }

    // == Statistics (Calculer les statistiques) ==
    @GetMapping("/statistics")
    @Operation(summary = "Admin: Calculate and retrieve system statistics", description = "Retrieves various system statistics (e.g., number of users, machines, interventions). Requires ADMIN role. **NOTE: Currently returns a placeholder.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved (currently placeholder text)",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "501", description = "Statistics endpoint not yet implemented", // Or keep 200 with placeholder info
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Object> getStatistics() {
        // Requires a dedicated StatisticsService to gather data
        // Object stats = statisticsService.calculateOverallStats();
        // return ResponseEntity.ok(stats);
        // Placeholder:
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Statistics endpoint not yet implemented.");
        // return ResponseEntity.ok("Statistics endpoint not yet implemented."); // Alternative placeholder
    }

    // == Access All Information (Accéder à tout les informations) ==
    // This functionality is largely covered by the individual GET endpoints above.
    // Specific aggregated views could be added if necessary.

    // == Search (Part of 'Accéder à tout les informations' / General Admin Task) ==
    @GetMapping("/search")
    @Operation(summary = "Admin: Generic search across different entities", description = "Performs a search based on a query string across specified entity types (e.g., 'user', 'machine', 'piece'). Requires ADMIN role. **NOTE: Currently returns a placeholder.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results (currently placeholder text)",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters (e.g., missing query or type)",
                    content = @Content),
            @ApiResponse(responseCode = "501", description = "Search endpoint not yet implemented", // Or keep 200 with placeholder info
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Object> search(
            @Parameter(description = "The search term or query string", required = true, example = "John Doe")
            @RequestParam String query,
            @Parameter(description = "The type of entity to search within (e.g., 'user', 'machine', 'piece', 'stemaintenance')", required = true, example = "user")
            @RequestParam String type) {
        // Implement a generic search service or delegate based on 'type'
        // Object results = searchService.search(query, type, UserRole.ADMIN);
        // return ResponseEntity.ok(results);
        // Placeholder:
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Admin Search for type '" + type + "' with query '" + query + "' not yet implemented.");
        // return ResponseEntity.ok("Admin Search for type '" + type + "' with query '" + query + "' not yet implemented."); // Alternative placeholder
    }
}