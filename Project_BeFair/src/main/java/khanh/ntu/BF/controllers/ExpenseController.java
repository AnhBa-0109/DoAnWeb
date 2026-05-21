package khanh.ntu.BF.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import khanh.ntu.BF.services.BeFairService;

@Controller
public class ExpenseController {

    @Autowired
    private BeFairService bfService;

    //Thêm hóa đơn
    @PostMapping("/group/{id}/add-expense")
    public String addExpense(@PathVariable Long id, 
                             @RequestParam String description, 
                             @RequestParam Double amount, 
                             @RequestParam Long payerId,
                             @RequestParam(required = false) List<Long> sharerIds,
                             @RequestParam("imageFile") MultipartFile file) {

        bfService.addExpense(id, description, amount, payerId, sharerIds, file);
        return "redirect:/group/" + id;
    }
    
    //Xóa hóa đơn
    @PostMapping("/group/{groupId}/delete-expense/{expenseId}")
    public String deleteExpense(@PathVariable Long groupId, @PathVariable Long expenseId, Principal principal, RedirectAttributes redirectAttributes) {
    	
    	if (!bfService.isGroupOwner(groupId, principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải chủ nhóm, không có quyền xóa hóa đơn!");
            return "redirect:/group/" + groupId;
        }
    	
        bfService.deleteExpense(expenseId);
        return "redirect:/group/" + groupId;
    }
}
