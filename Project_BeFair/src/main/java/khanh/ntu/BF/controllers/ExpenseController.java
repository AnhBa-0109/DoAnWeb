package khanh.ntu.BF.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import khanh.ntu.BF.Repository.ExpenseRepository;
import khanh.ntu.BF.Repository.MemberRepository;
import khanh.ntu.BF.Repository.TravelGroupRepository;
import khanh.ntu.BF.models.Expense;

@Controller
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TravelGroupRepository groupRepository;

    @PostMapping("/group/{groupId}/add-expense")
    public String addExpense(@PathVariable Long groupId,
                             @RequestParam String description,
                             @RequestParam Double amount,
                             @RequestParam Long payerId) {
        
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        
        expense.setGroup(groupRepository.findById(groupId).get());
        expense.setPayer(memberRepository.findById(payerId).get());
        
        expenseRepository.save(expense);
        
        return "redirect:/group/" + groupId;
    }
}
