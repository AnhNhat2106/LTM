package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.dto.RegisterRequest;
import com.mathgame.mathgame.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // âœ… má»Ÿ form Ä‘Äƒng kÃ½
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("req", new RegisterRequest());
        return "register";
    }

    // âœ… xá»­ lÃ½ Ä‘Äƒng kÃ½
    @PostMapping("/register")
    public String doRegister(@ModelAttribute("req") RegisterRequest req, Model model) {
        try {
            userService.register(req);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // âœ… má»Ÿ form login (Spring Security xá»­ lÃ½ POST /login)
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String doForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            String tempPassword = userService.resetPasswordByEmail(email);
            model.addAttribute("success", "Mật khẩu tạm thời của bạn: " + tempPassword);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forgot_password";
    }}

