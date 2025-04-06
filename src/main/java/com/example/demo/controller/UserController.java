package com.example.demo.controller;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.service.CheckInHistoryService;
import com.example.demo.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

        Locale locale = Locale.forLanguageTag(language);

        return userService.createUser(createUserRequest, locale);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@RequestHeader(value = "Accept-Language", defaultValue = "vi") String language,
                                            @PathVariable Long id) {

        Locale locale = Locale.forLanguageTag(language);

        return userService.getUserById(id, locale);
    }

    @PostMapping("/points/subtract")
    public ResponseEntity<String> subtract(@RequestHeader(value = "Accept-Language", defaultValue = "vi") String language,
                                           @RequestParam Long userId,
                                           @RequestParam int point) {

        Locale locale = Locale.forLanguageTag(language);;

        try {

            return ResponseEntity.ok(checkInHistoryService.subtractPoint(userId, point, locale));
        } catch (ResponseStatusException exception) {

            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        } catch (Exception exception) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }
}
