package com.martialarts.backend.service;

import com.martialarts.backend.entity.Student;
import com.martialarts.backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepo;

    public List<Student> getPendingStudents() {
        return studentRepo.findByStatus("PENDING");
    }

    public List<Student> getApprovedStudents() {
        return studentRepo.findByStatus("APPROVED");
    }

    public List<Student> getRejectedStudents() {
        return studentRepo.findByStatus("REJECTED");
    }

    // 🎯 APPROVE & RE-APPROVE LOGIC
    public String approveStudent(Long id) {
        Student s = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));

        String prevStatus = s.getStatus();
        s.setStatus("APPROVED");
        s.setUpdatedAt(LocalDateTime.now());

        String log = "[" + LocalDateTime.now() + "] Status updated from " + prevStatus + " to APPROVED.";
        s.setActionHistory(s.getActionHistory() == null ? log : s.getActionHistory() + "\n" + log);

        s.setRejectionReason(null);
        studentRepo.save(s);
        return "Student Approved Successfully";
    }

    public String rejectStudent(Long id, String reason) {
        Student s = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));

        String prevStatus = s.getStatus();
        s.setStatus("REJECTED");
        s.setRejectionReason(reason);
        s.setUpdatedAt(LocalDateTime.now());

        String log = "[" + LocalDateTime.now() + "] Status updated from " + prevStatus + " to REJECTED. Reason: " + reason;
        s.setActionHistory(s.getActionHistory() == null ? log : s.getActionHistory() + "\n" + log);

        studentRepo.save(s);
        return "Student Rejected";
    }
    
    
 // 🎯 1. Discontinue Student
    public String discontinueStudent(Long id, String reason) {
        Student s = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        String prevStatus = s.getStatus();
        s.setStatus("DISCONTINUED");
        s.setDiscontinuedReason(reason);
        s.setDiscontinuedDate(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());

        String log = "[" + LocalDateTime.now() + "] Discontinued from " + prevStatus + ". Reason: " + reason;
        s.setActionHistory(s.getActionHistory() == null ? log : s.getActionHistory() + "\n" + log);

        studentRepo.save(s);
        return "Student marked as Discontinued";
    }

    // 🎯 2. Re-Join Discontinued Student
    public String rejoinStudent(Long id) {
        Student s = studentRepo.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        s.setStatus("APPROVED");
        s.setUpdatedAt(LocalDateTime.now());

        String log = "[" + LocalDateTime.now() + "] Re-joined academy. Reactivated to APPROVED status.";
        s.setActionHistory(s.getActionHistory() == null ? log : s.getActionHistory() + "\n" + log);

        studentRepo.save(s);
        return "Student Re-joined and Reactivated Successfully";
    }

    // 🎯 3. Get Discontinued Students
    public List<Student> getDiscontinuedStudents() {
        return studentRepo.findByStatus("DISCONTINUED");
    }
}