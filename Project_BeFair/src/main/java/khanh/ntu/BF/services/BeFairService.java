package khanh.ntu.BF.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import khanh.ntu.BF.Repository.ExpenseRepository;
import khanh.ntu.BF.Repository.MemberRepository;
import khanh.ntu.BF.Repository.TravelGroupRepository;
import khanh.ntu.BF.models.Expense;
import khanh.ntu.BF.models.Member;
import khanh.ntu.BF.models.TravelGroup;

@Service
public class BeFairService {
	@Autowired
	private TravelGroupRepository groupRepository;
	@Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExpenseRepository expenseRepository;
    
    public void addNewGroup(TravelGroup group) {
    	groupRepository.save(group);
    }
    public void addNewMember(Long groupId, String name) {
    	TravelGroup group = groupRepository.findById(groupId).get();
        Member member = new Member();
        member.setName(name);
        member.setGroup(group);
        memberRepository.save(member);
    }
    public Map<String, Double> calculateBalances(Long groupId) {
        List<Member> members = memberRepository.findByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        Map<String, Double> balances = new HashMap<>();
        for (Member m : members) {
            balances.put(m.getName(), 0.0);
        }

        if (members.isEmpty()) return balances;

        for (Expense exp : expenses) {
            double amount = exp.getAmount();
            double perPerson = amount / members.size();
            String payerName = exp.getPayer().getName();

            for (Member m : members) {
                String name = m.getName();
                if (name.equals(payerName)) {
                    balances.put(name, balances.get(name) + (amount - perPerson));
                } else {
                    balances.put(name, balances.get(name) - perPerson);
                }
            }
        }
        return balances;
    }
}
