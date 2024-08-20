package com.kcs.security_sample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1")
public class FormController {

    @GetMapping("/form")
    public String showForm() {
        return "form";
    }

    @PostMapping("/submit")
    public String submitForm(@RequestParam String inputText, Model model) {
        model.addAttribute("submittedText", inputText);
        return "result";
    }
}