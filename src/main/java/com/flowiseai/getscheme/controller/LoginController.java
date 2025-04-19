package com.flowiseai.getscheme.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/login")
    public ModelAndView showLoginPage() {
        logger.info("Displaying login page");
        return new ModelAndView("login");
    }

    @GetMapping("/")
    public ModelAndView home() {
        logger.info("Displaying home page");
        return new ModelAndView("home");
    }

    @GetMapping("/home")
    public ModelAndView homePage() {
        logger.info("Displaying home page");
        return new ModelAndView("home");
    }
}