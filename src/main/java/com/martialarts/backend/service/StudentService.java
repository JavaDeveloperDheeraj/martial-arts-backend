package com.martialarts.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.martialarts.backend.entity.AuthorizedUser;
import com.martialarts.backend.entity.ParentStudentMapping;
import com.martialarts.backend.entity.Student;
import com.martialarts.backend.repository.AuthorizedUserRepository;
import com.martialarts.backend.repository.ParentStudentMappingRepository;
import com.martialarts.backend.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repo;
    
    @Autowired
    private AuthorizedUserRepository authUserRepo;
    
    @Autowired
    private ParentStudentMappingRepository mappingRepo;

    // LOGIN for existing students (mobile check)
    public Student login(String mobile) {

        Student student = repo.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"APPROVED".equals(student.getStatus())) {
            throw new RuntimeException("Not approved yet");
        }

        return student;
    }
    
    // PARENT LOGIN (checks authorized_user table)
    public AuthorizedUser parentLogin(String mobile) {
        return authUserRepo.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("Mobile number not registered. Please contact admin."));
    }

    // FILTER APIs
    public List<Student> getPendingStudents() {
        return repo.findByStatus("PENDING");
    }

    public List<Student> getApprovedStudents() {
        return repo.findByStatus("APPROVED");
    }

    public List<Student> getRejectedStudents() {
        return repo.findByStatus("REJECTED");
    }

    // APPROVE with Parent Authorization
    @Transactional
    public String approveStudent(Long id) {

        Student s = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if ("APPROVED".equals(s.getStatus())) {
            return "Already Approved";
        }

        s.setStatus("APPROVED");
        s.setUpdatedAt(LocalDateTime.now());
        repo.save(s);
        
        // 🔥 Add parent to authorized users
        addParentToAuthorizedUsers(s);
        
        return "Student Approved Successfully";
    }
    
    // Add parent to authorized users table
    private void addParentToAuthorizedUsers(Student student) {
        String parentMobile = student.getMobile(); // Student's mobile is parent's mobile
        
        // Check if parent already exists
        if (!authUserRepo.existsByMobile(parentMobile)) {
            AuthorizedUser parent = new AuthorizedUser();
            parent.setMobile(parentMobile);
            parent.setName(student.getFatherName()); // Use father's name as parent name
            parent.setRole("PARENT");
            parent.setActive(true);
            authUserRepo.save(parent);
        }
        
        // Create mapping
        if (!mappingRepo.existsByParentMobileAndStudentId(parentMobile, student.getId())) {
            ParentStudentMapping mapping = new ParentStudentMapping();
            mapping.setParentMobile(parentMobile);
            mapping.setStudentId(student.getId());
            mapping.setStudentName(student.getName());
            mapping.setStudentStatus("APPROVED");
            mappingRepo.save(mapping);
        }
    }

    // REJECT
    public String rejectStudent(Long id) {

        Student s = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if ("REJECTED".equals(s.getStatus())) {
            return "Already Rejected";
        }

        s.setStatus("REJECTED");
        s.setUpdatedAt(LocalDateTime.now());

        repo.save(s);

        return "Student Rejected Successfully";
    }
    
    // Get all students for a parent
    public List<ParentStudentMapping> getStudentsByParentMobile(String mobile) {
        return mappingRepo.findByParentMobile(mobile);
    }
    
    
}