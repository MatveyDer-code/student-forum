package io.student.pet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
public class RoleTestController {

    @GetMapping("/moderator")
    public ResponseEntity<String> moderatorAccess() {
        log.info("Accessed moderator-only endpoint");
        return ResponseEntity.ok("Hello, Moderator!");
    }

    @GetMapping("/teacher")
    public ResponseEntity<String> teacherOnly() {
        log.info("Accessed teacher-only endpoint");
        return ResponseEntity.ok("Hello, Teacher!");
    }

    @GetMapping("/student")
    public ResponseEntity<String> studentOnly() {
        log.info("Accessed student-only endpoint");
        return ResponseEntity.ok("Hello, Student!");
    }
}
