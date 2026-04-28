package khanh.ntu.BF.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import khanh.ntu.BF.Repository.ExpenseRepository;
import khanh.ntu.BF.Repository.MemberRepository;
import khanh.ntu.BF.Repository.TravelGroupRepository;
import khanh.ntu.BF.models.Member;
import khanh.ntu.BF.models.TravelGroup;
import khanh.ntu.BF.services.BeFairService;

@Controller
public class TravelGroupController {
	@Autowired
    private TravelGroupRepository groupRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private BeFairService bfService;

    //Trang danh sách nhóm
    @GetMapping("/home")
    public String index(ModelMap model) {
        model.addAttribute("groups", groupRepository.findAll());
        return "index";
    }

    //Trang chi tiết nhóm
    @GetMapping("/group/{id}")
    public String groupDetail(@PathVariable Long id, ModelMap model) {
        TravelGroup group = groupRepository.findById(id).orElseThrow();
        
        model.addAttribute("group", group);
        model.addAttribute("members", memberRepository.findByGroupId(id));
        model.addAttribute("expenses", expenseRepository.findByGroupId(id));
        
        Map<String, Double> balances = bfService.calculateBalances(id);
        model.addAttribute("balances", balances);
        
        return "detailGroup";
    }
    
    
    //Xử lý thêm nhóm mới
    @PostMapping("/add-group")
    public String addGroup(@ModelAttribute TravelGroup group) {
    	if(group.getName() == null || group.getName().trim().isEmpty()) return "redirect:/home";
    	bfService.addNewGroup(group);
        return "redirect:/home";
    }
    
    //Thêm thành viên
    @PostMapping("/group/{id}/add-member")
    public String addMember(@PathVariable Long id, @RequestParam String memberName, RedirectAttributes redirectAttributes) {
    	if( memberName== null || memberName.trim().isEmpty()) {
    		redirectAttributes.addFlashAttribute("errorMessage", "Tên không được để trống!");
    		return "redirect:/group/" + id;
    	}
    	bfService.addNewMember(id, memberName);
        return "redirect:/group/" + id;
    }
}
