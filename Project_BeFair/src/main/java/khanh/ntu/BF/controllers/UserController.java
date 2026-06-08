package khanh.ntu.BF.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import khanh.ntu.BF.Repository.TravelGroupRepository;
import khanh.ntu.BF.Repository.UserRepository;
import khanh.ntu.BF.models.TravelGroup;
import khanh.ntu.BF.models.User;
import khanh.ntu.BF.services.BeFairService;

@Controller
public class UserController {

    @Autowired
    private BeFairService bfService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TravelGroupRepository groupRepository;

    //hồ sơ cá nhân
    @GetMapping("/profile")
    public String showProfile(ModelMap model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName());
        List<TravelGroup> userGroups = groupRepository.findByOwnerOrMember(currentUser.getId());
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", "profile");
        model.addAttribute("groups", userGroups);
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
