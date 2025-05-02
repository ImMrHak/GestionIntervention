package com.gestion.intervention.adapter.web;

import com.gestion.intervention.application.person.record.PersonDTO;
import com.gestion.intervention.application.person.record.request.LoginRequestDTO;
import com.gestion.intervention.application.person.record.response.LoginResponseDTO;
import com.gestion.intervention.application.person.service.PersonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication") // Add Tag for grouping
public class AuthController {

    private final PersonService personService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user with provided credentials (email and password) and returns access/refresh JWT tokens upon successful login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., missing fields)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication failed (invalid credentials or user not found)",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error during authentication",
                    content = @Content)
    })
    public ResponseEntity<LoginResponseDTO> login(
            @Parameter(description = "User credentials for login.", // Updated description
                    required = true,
                    // Use LoginRequestDTO for the schema
                    schema = @Schema(implementation = LoginRequestDTO.class))
            @RequestBody @Valid LoginRequestDTO loginRequestDTO) { // Add @Valid if LoginRequestDTO has validation
        LoginResponseDTO response = personService.login(loginRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // If you add more endpoints like registration, add Swagger annotations to them as well.
    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Registers a new user in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonDTO.class))), // Assuming registration returns the created user
        @ApiResponse(responseCode = "400", description = "Invalid input data (validation errors, username/email already exists)", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error during registration", content = @Content)
    })
    public ResponseEntity<LoginResponseDTO> register(
        @Parameter(description = "Details of the user to register.", required = true, schema = @Schema(implementation = PersonDTO.class)) @RequestBody PersonDTO dto) {
            LoginResponseDTO createdPerson = personService.register(dto);
            return new ResponseEntity<>(createdPerson, HttpStatus.CREATED);
    }
}