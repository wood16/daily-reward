package com.example.demo.controller;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.service.CheckInHistoryService;
import com.example.demo.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    CheckInHistoryService checkInHistoryService;

    @PostMapping
    public ResponseEntity<?> postUser(@RequestHeader(value = "Accept-Language", defaultValue = "vi") String language,
                                      @RequestBody CreateUserRequest createUserRequest) {

        Locale locale = new Locale(language);

        return userService.createUser(createUserRequest, locale);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@RequestHeader(value = "Accept-Language", defaultValue = "vi") String language,
                                            @PathVariable Long id) {

        Locale locale = new Locale(language);

        return userService.getUserById(id, locale);
    }

    @PostMapping("/points/subtract")
    public ResponseEntity<String> subtract(@RequestHeader(value = "Accept-Language", defaultValue = "vi") String language,
                                           @RequestParam Long userId,
                                           @RequestParam int point) {

        Locale locale = new Locale(language);

        try {

            return ResponseEntity.ok(checkInHistoryService.subtractPoint(userId, point, locale));
        } catch (Exception exception) {

            return ResponseEntity.badRequest().body(exception.getMessage());
        }

    }
}
