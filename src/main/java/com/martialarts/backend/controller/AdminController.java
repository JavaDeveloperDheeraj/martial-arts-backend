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

    @PostMapping("/students/reject/{id}")
    public String reject(@PathVariable Long id) {
        return service.rejectStudent(id);
    }
}