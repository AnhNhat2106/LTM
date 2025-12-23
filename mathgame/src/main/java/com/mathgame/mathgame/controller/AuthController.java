package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.dto.RegisterRequest;
import com.mathgame.mathgame.service.EmailOtpService;
import com.mathgame.mathgame.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@Controller
public class AuthController {

    private final UserService userService;
    private final EmailOtpService otpService;

    public AuthController(UserService userService, EmailOtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("req", new RegisterRequest());
        return "register";
    }

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

    @PostMapping("/otp/send")
    @ResponseBody
    public ResponseEntity<Map<String, String>> sendOtp(@RequestParam("email") String email,
                                                       @RequestParam("purpose") String purpose) {
        try {
            String p = purpose == null ? "" : purpose.trim().toUpperCase(Locale.ROOT);
            if (!EmailOtpService.PURPOSE_REGISTER.equals(p) && !EmailOtpService.PURPOSE_RESET.equals(p)) {
                throw new RuntimeException("Invalid purpose");
            }
            otpService.sendOtp(email, p);
            return ResponseEntity.ok(Map.of("ok", "true", "message", "OTP sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("ok", "false", "message", e.getMessage()));
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String doForgotPassword(@RequestParam("email") String email,
                                   @RequestParam("otpCode") String otpCode,
                                   @RequestParam("newPassword") String newPassword,
                                   @RequestParam("confirmPassword") String confirmPassword,
                                   Model model) {
        try {
            userService.resetPasswordWithOtp(email, otpCode, newPassword, confirmPassword);
            model.addAttribute("success", "Password updated. Please login.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forgot_password";
    }
}
