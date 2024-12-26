package ua.lastbite.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.lastbite.userservice.dto.email.UserEmailInfo;
import ua.lastbite.userservice.dto.user.*;
import ua.lastbite.userservice.service.UserService;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Request received: POST /api/users - Register user");
        UserResponseDto createdUser = userService.register(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        log.info("User registered successfully: {}", createdUser);
        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        log.info("Request received: GET /api/users - Get all users (page: {}, size: {})", page, size);
        Page<UserResponseDto> users = userService.getAllUsers(PageRequest.of(page, size, Sort.by("firstName").ascending()));

        log.debug("Retrieved users: {}", users.getContent());
        return ResponseEntity.ok(users.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer id) {
        log.info("Request received: GET /api/users/{} - Get user by ID", id);
        UserResponseDto user = userService.getUserById(id);

        log.debug("Retrieved user: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/email-info")
    public ResponseEntity<UserEmailInfo> getUserEmailInfo(@PathVariable Integer id) {
        log.info("Request received: GET /api/users/{}/email-info - Get user email info", id);
        UserEmailInfo userEmailInfo = userService.getUserEmailInfo(id);

        log.debug("Retrieved user email info: {}", userEmailInfo);
        return ResponseEntity.ok(userEmailInfo);
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<Void> changeEmail(@PathVariable Integer id,
                                            @Valid @RequestBody ChangeEmailRequest request) {
        log.info("Request received: PUT /api/users/{}/email - Change email", id);
        userService.updateEmailAddress(id, request);

        log.info("Email updated for user with id {}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Request received: PUT /api/users/{}/password - Change password", id);
        userService.updatePassword(id, request);

        log.info("Password updated for user with id {}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/phone")
    public ResponseEntity<Void> changePhoneNumber(@PathVariable Integer id,
                                                  @Valid @RequestBody ChangePhoneNumberRequest request) {
        log.info("Request received: PUT /api/users/{}/phone - Change phone", id);
        userService.updatePhoneNumber(id, request);

        log.info("Phone updated for user with id {}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<Void> changeName(@PathVariable Integer id,
                                           @Valid @RequestBody ChangeNameRequest request) {
        log.info("Request received: PUT /api/users/{}/name - Change name", id);
        userService.updateName(id, request);

        log.info("Name updated for user with id {}", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        log.info("Request received: DELETE /api/users/{} - Delete user", id);
        userService.deleteUser(id);

        log.info("User deleted with id {}", id);
        return ResponseEntity.noContent().build();
    }
}
