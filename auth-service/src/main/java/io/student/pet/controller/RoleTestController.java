package io.student.pet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class RoleTestController {

    @GetMapping("/moderator")
    public ResponseEntity<String> moderatorAccess() {
        return ResponseEntity.ok("Hello, Moderator!");
    }

    @GetMapping("/teacher")
    public ResponseEntity<String> teacherOnly() {
        return ResponseEntity.ok("Hello, Teacher!");
    }

    @GetMapping("/student")
    public ResponseEntity<String> studentOnly() {
        return ResponseEntity.ok("Hello, Student!");
    }
}
