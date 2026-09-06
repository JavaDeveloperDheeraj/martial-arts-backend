package com.martialarts.backend.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.martialarts.backend.entity.Student;
import com.martialarts.backend.service.StudentService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private StudentService service;

    @GetMapping("/students/pending")
    public List<Student> getPending() {
        return service.getPendingStudents();
    }

    @GetMapping("/students/approved")
    public List<Student> getApproved() {
        return service.getApprovedStudents();
    }

    @GetMapping("/students/rejected")
    public List<Student> getRejected() {
        return service.getRejectedStudents();
    }

    @PostMapping("/students/approve/{id}")
    public String approve(@PathVariable Long id) {
        return service.approveStudent(id);
    }

//    @PostMapping("/students/reject/{id}")
//    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
//        System.out.println("Rejecting student ID: " + id + " for Reason: " + reason);
//        return service.rejectStudent(id);
//    }
    
    @PostMapping("/students/reject/{id}")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return service.rejectStudent(id, reason != null ? reason : "No reason specified");
    }
    
    @GetMapping("/students/discontinued")
    public List<Student> getDiscontinued() {
        return service.getDiscontinuedStudents();
    }

    @PostMapping("/students/discontinue/{id}")
    public String discontinue(@PathVariable Long id, @RequestParam String reason) {
        return service.discontinueStudent(id, reason);
    }

    @PostMapping("/students/rejoin/{id}")
    public String rejoin(@PathVariable Long id) {
        return service.rejoinStudent(id);
    }
}