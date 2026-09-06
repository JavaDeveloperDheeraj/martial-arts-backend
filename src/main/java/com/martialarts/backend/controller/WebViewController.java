package com.martialarts.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {


    @GetMapping("/")
    public String homePage() {
        return "index"; 
    }

    @GetMapping("/register-student")
    public String registerPage() {
        return "student-registration"; 
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; 
    }
    
    @GetMapping({"/admin", "/admin/dashboard"})
    public String adminPage() {
        return "admin-dashboard"; 
    }

//    @GetMapping({"/parent", "/parent/dashboard"})
//    public String parentPage() {
//        return "parent-dashboard";
//    }
    
//    @GetMapping({"/parent/dashboard", "/student/dashboard"})
//    public String studentParentPortal() {
//        return "student-parent-dashboard"; 
//    }
    
    @GetMapping({"/parent", "/parent/dashboard", "/student", "/student/dashboard"})
    public String studentParentPortal() {
        return "student-parent-dashboard"; 
    }
}