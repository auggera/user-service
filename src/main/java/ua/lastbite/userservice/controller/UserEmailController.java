package ua.lastbite.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.lastbite.userservice.dto.email.UserEmailResponseDto;
import ua.lastbite.userservice.service.UserEmailService;

@RestController
@RequestMapping("/api/email")
public class UserEmailController {

    private final UserEmailService userEmailService;

    @Autowired
    public UserEmailController(UserEmailService userEmailService) {
        this.userEmailService = userEmailService;
    }

    @GetMapping("/{userId}/info")
    public ResponseEntity<UserEmailResponseDto> getUserEmailInfo(@PathVariable Integer userId) {
        UserEmailResponseDto userEmailResponseDto = userEmailService.getUserEmailInfo(userId);
        return ResponseEntity.ok(userEmailResponseDto);
    }
}
