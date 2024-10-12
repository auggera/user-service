package ua.lastbite.userservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.lastbite.userservice.dto.user.*;
import ua.lastbite.userservice.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponseDto createdUser = userService.register(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<UserResponseDto> users = userService.getAllUsers(PageRequest.of(page, size, Sort.by("firstName").ascending()));
        return ResponseEntity.ok(users.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<Void> changeEmail(@PathVariable Integer id,
                                            @Valid @RequestBody ChangeEmailRequest request) {
        userService.updateEmailAddress(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.updatePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/phone")
    public ResponseEntity<Void> changePhoneNumber(@PathVariable Integer id,
                                                  @Valid @RequestBody ChangePhoneNumberRequest request) {
        userService.updatePhoneNumber(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<Void> changeName(@PathVariable Integer id, @Valid @RequestBody ChangeNameRequest request) {
        userService.updateName(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
