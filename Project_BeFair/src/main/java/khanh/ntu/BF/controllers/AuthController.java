package khanh.ntu.BF.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import khanh.ntu.BF.services.BeFairService;

@Controller
public class AuthController {
	
	@Autowired
    private BeFairService bfService;
	
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@RequestParam String username, 
                               @RequestParam String password, 
                               @RequestParam String fullName, 
                               ModelMap model) {
        try {
            bfService.registerUser(username, password, fullName);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
