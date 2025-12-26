package com.mathgame.mathgame.controller;

import com.mathgame.mathgame.dto.BattleSummaryDto;
import com.mathgame.mathgame.dto.PracticeSummaryDto;
import com.mathgame.mathgame.entity.User;
import com.mathgame.mathgame.repository.BattleResultRepository;
import com.mathgame.mathgame.repository.PracticeHistoryRepository;
import com.mathgame.mathgame.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {

    private final UserService userService;
    private final PracticeHistoryRepository practiceHistoryRepository;
    private final BattleResultRepository battleResultRepository;

    public ProfileController(UserService userService,
                             PracticeHistoryRepository practiceHistoryRepository,
                             BattleResultRepository battleResultRepository) {
        this.userService = userService;
        this.practiceHistoryRepository = practiceHistoryRepository;
        this.battleResultRepository = battleResultRepository;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        String username = auth.getName();
        User user = userService.findByUsername(username);
        PracticeSummaryDto practice = practiceHistoryRepository.getSummary(username);
        BattleSummaryDto battle = battleResultRepository.getSummary(username);

        model.addAttribute("user", user);
        model.addAttribute("practice", practice);
        model.addAttribute("battle", battle);
        return "profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(Authentication auth,
                                 @RequestParam("otpCode") String otpCode,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {
        String username = auth.getName();

        try {
            userService.changePasswordWithOtp(username, otpCode, newPassword, confirmPassword);
            model.addAttribute("success", "Đổi mật khẩu thành công");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        User user = userService.findByUsername(username);
        PracticeSummaryDto practice = practiceHistoryRepository.getSummary(username);
        BattleSummaryDto battle = battleResultRepository.getSummary(username);

        model.addAttribute("user", user);
        model.addAttribute("practice", practice);
        model.addAttribute("battle", battle);
        return "profile";
    }

    @PostMapping("/profile/username")
    public String changeUsername(Authentication auth,
                                 @RequestParam("newUsername") String newUsername,
                                 Model model) {
        String currentUsername = auth.getName();
        String effectiveUsername = currentUsername;

        try {
            userService.changeUsername(currentUsername, newUsername);
            String updatedUsername = (newUsername == null || newUsername.trim().isEmpty())
                    ? currentUsername
                    : newUsername.trim();
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUsername,
                    auth.getCredentials(),
                    auth.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            model.addAttribute("success", "Đã cập nhật username");
            effectiveUsername = updatedUsername;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        User user = userService.findByUsername(effectiveUsername);
        PracticeSummaryDto practice = practiceHistoryRepository.getSummary(effectiveUsername);
        BattleSummaryDto battle = battleResultRepository.getSummary(effectiveUsername);

        model.addAttribute("user", user);
        model.addAttribute("practice", practice);
        model.addAttribute("battle", battle);
        return "profile";
    }
}