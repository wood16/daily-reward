package com.example.demo.controller;

import com.example.demo.dto.response.CheckInDayResponse;
import com.example.demo.dto.response.CheckInStatusResponse;
import com.example.demo.service.CheckInHistoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("check-in")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckInHistoryController {

    CheckInHistoryService checkInHistoryService;

    @PostMapping
    public ResponseEntity<String> checkIn(@RequestHeader(value = "Accept-Language", defaultValue = "vi") String language,
                                          @RequestParam Long userId) {

        Locale locale = Locale.forLanguageTag(language);

        try {

            return ResponseEntity.ok(checkInHistoryService.checkIn(userId, locale));
        } catch (ResponseStatusException exception) {

            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        } catch (Exception exception) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<CheckInStatusResponse>> getCheckInStatus(@RequestParam Long userId,
                                                                        @RequestParam LocalDate startDate,
                                                                        @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(checkInHistoryService.getCheckInStatus(userId, startDate, endDate));
    }

    @GetMapping("/histories")
    public ResponseEntity<Page<CheckInDayResponse>> getCheckInStatus(@RequestParam Long userId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "8") int pageSize) {

        return ResponseEntity.ok(checkInHistoryService.getCheckInHistory(userId, page, pageSize));
    }


}
