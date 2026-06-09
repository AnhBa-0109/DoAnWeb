package khanh.ntu.BF.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import khanh.ntu.BF.Repository.UserRepository;
import khanh.ntu.BF.models.ExpenseDTO;
import khanh.ntu.BF.models.Member;
import khanh.ntu.BF.models.TravelGroup;
import khanh.ntu.BF.models.User;
import khanh.ntu.BF.services.BeFairService;

@Controller
public class TravelGroupController {
	@Autowired
    private TravelGroupRepository groupRepository;
	
	@Autowired
	private UserRepository userRepository;
    
    @Autowired
    private BeFairService bfService;

    //Trang danh sách nhóm
    @GetMapping("/home")
    public String index(ModelMap model, Principal principal) {
    	String username = principal.getName();
        
    	User currentUser = userRepository.findByUsername(username);
    	
        List<TravelGroup> userGroups = groupRepository.findByOwnerOrMember(currentUser.getId());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("groups", userGroups);
        model.addAttribute("activeTab", "home");
        return "index";
    }

    //Trang chi tiết nhóm
    @GetMapping("/group/{id}")
    public String detailGroup(@PathVariable Long id, ModelMap model, Principal principal) {
    	String username = principal.getName();
        TravelGroup group = bfService.getGroupById(id);
        User currentUser = userRepository.findByUsername(username);
        List<TravelGroup> userGroups = groupRepository.findByOwnerOrMember(currentUser.getId());
        List<Member> currentMembers = group.getMembers().stream()
                                           .filter(Member::isActive)
                                           .collect(Collectors.toList());
        List<ExpenseDTO> expenseDTOs = bfService.getGroupExpensesForView(id);
        
        model.addAttribute("activeTab", "home");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("group", group);
        model.addAttribute("members", currentMembers); 
        model.addAttribute("expenses", group.getExpenses());
        model.addAttribute("balances", bfService.calculateBalances(id));
        model.addAttribute("expenses", expenseDTOs);
        model.addAttribute("groups", userGroups);
        
        return "detailGroup";
    }
    
    //Xử lý thêm nhóm mới
    @PostMapping("/add-group")
    public String addGroup(@ModelAttribute TravelGroup group, Principal principal) {
    	String username = principal.getName();
        User currentUser = userRepository.findByUsername(username);
        
        group.setOwner(currentUser);
        bfService.addNewGroup(group);
        return "redirect:/home";
    }
    
    //Xử lý sửa tên nhóm
    @PostMapping("/group/edit/{id}")
    public String editGroup(@PathVariable Long id, 
                            @RequestParam String groupName,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        
        if (!bfService.isGroupOwner(id, principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải chủ nhóm!");
            return "redirect:/home";
        }
        
        bfService.editGroup(id, groupName);
        redirectAttributes.addFlashAttribute("successMessage", "Sửa tên nhóm thành công!");
        return "redirect:/home";
    }
    
    //Xóa nhóm
    @PostMapping("/group/delete/{id}")
    public String deleteGroup(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bfService.deleteGroup(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nhóm thành công!");
        return "redirect:/home";
    }
    
    //Thêm thành viên
    @PostMapping("/group/{id}/add-member")
    public String addMember(@PathVariable Long id, @RequestParam String memberName, RedirectAttributes redirectAttributes) {
        if (memberName == null || memberName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên không được để trống!");
            return "redirect:/group/" + id;
        }
        try {
            bfService.addNewMember(id, memberName);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thành viên thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/group/" + id;
    }
    
    //Sửa tên thành viên
    @PostMapping("/group/{id}/edit-member")
    public String editMember(@PathVariable Long id, @RequestParam Long memberId, @RequestParam String newName, 
    							Principal principal,
    							RedirectAttributes redirectAttributes) {
    	
    	if (!bfService.isGroupOwner(id, principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải chủ nhóm!");
            return "redirect:/group/" + id;
        }
        if (newName != null && !newName.trim().isEmpty()) {
            try {
                bfService.editMember(memberId, newName);
                redirectAttributes.addFlashAttribute("successMessage", "Sửa tên thành viên thành công!");
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        }
        return "redirect:/group/" + id;
    }
    
    //Xóa thành viên
    @PostMapping("/group/{groupId}/delete-member/{memberId}")
    public String deleteMember(@PathVariable Long groupId, @PathVariable Long memberId, RedirectAttributes redirectAttributes,
					    		Principal principal) {
    	
    	if (!bfService.isGroupOwner(groupId, principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải chủ nhóm, không có quyền sửa tên thành viên!");
            return "redirect:/group/" + groupId;
        }
    	
    	try {
    		bfService.deleteMember(memberId);
    		redirectAttributes.addFlashAttribute("successMessage", "Xóa thành công!");
    	}
    	catch(IllegalArgumentException e)
    	{
    		redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    	}
    	
    	return "redirect:/group/" + groupId;
    }
    
    //link user với member
    @PostMapping("/group/{groupId}/link-member")
    public String linkMemberToUser(@PathVariable Long groupId, 
                                   @RequestParam Long memberId, 
                                   @RequestParam String username, 
                                   RedirectAttributes redirectAttributes) {
        try {
            bfService.linkMemberToUser(memberId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Liên kết người dùng thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/group/" + groupId;
    }
    
    //hủy liên kết user khỏi member
    @PostMapping("/group/{groupId}/unlink-member")
    public String unlinkMemberFromUser(@PathVariable Long groupId,
                                       @RequestParam Long memberId,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        if (!bfService.isGroupOwner(groupId, principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải chủ nhóm, không có quyền hủy liên kết!");
            return "redirect:/group/" + groupId;
        }
        try {
            bfService.unlinkMemberFromUser(memberId);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy liên kết thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/group/" + groupId;
    }
}
