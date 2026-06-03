package khanh.ntu.BF.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import khanh.ntu.BF.Repository.UserRepository;
import khanh.ntu.BF.models.User;
import khanh.ntu.BF.services.BeFairService;

@Controller
public class UserController {

    @Autowired
    private BeFairService bfService;

    @Autowired
    private UserRepository userRepository;

    //hồ sơ cá nhân
    @GetMapping("/profile")
    public String showProfile(ModelMap model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", "profile");
        return "profile";
    }

    //cập nhật thông tin cá nhân
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phoneNumber,
                                @RequestParam(required = false) String bankAccount,
                                @RequestParam(required = false) String bankCode,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            bfService.updateUserProfile(principal.getName(), fullName, email, phoneNumber, bankAccount, bankCode);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }
}
