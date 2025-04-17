package com.flowiseai.getscheme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    @GetMapping("/login")
    public ModelAndView showLoginPage() {
        // Sử dụng ModelAndView để chỉ rõ view name với đường dẫn đầy đủ
        return new ModelAndView("login");
    }

    @GetMapping("/")
    public ModelAndView home() {
        return new ModelAndView("home");
    }

    @GetMapping("/home")
    public ModelAndView homePage() {
        return new ModelAndView("home");
    }
}