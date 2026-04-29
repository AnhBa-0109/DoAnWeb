package khanh.ntu.BF.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    
    public TravelGroup getGroupById(Long id) {
    	return groupRepository.getReferenceById(id);
    }
    public void addNewMember(Long groupId, String name) {
    	TravelGroup group = groupRepository.findById(groupId).get();
        Member member = new Member();
        member.setName(name);
        member.setGroup(group);
        memberRepository.save(member);
    }
    
    public void editMember(@RequestParam Long memberId, @RequestParam String newName) {
    	Member member = memberRepository.findById(memberId).get();
        member.setName(newName);
        memberRepository.save(member);
    }
    
    public void deleteMember(Long memberId) {
        Member m = memberRepository.findById(memberId).orElse(null);
        if (m != null) {
            m.setActive(false);
            m.setLeftAt(LocalDateTime.now());
            memberRepository.save(m);
        }
    }
    
    public Map<String, Double> calculateBalances(Long groupId) {
        TravelGroup group = groupRepository.findById(groupId).get();
        Map<String, Double> balances = new HashMap<>();
        
        for (Member m : group.getMembers()) {
            balances.put(m.getName(), 0.0);
        }

        for (Expense exp : group.getExpenses()) {
            double amount = exp.getAmount();
            Member payer = exp.getPayer();
            
            // 1. Tìm danh sách những người phải chia hóa đơn này
            // Điều kiện: Gia nhập trước khi hóa đơn tạo VÀ (Chưa rời nhóm HOẶC rời nhóm sau khi hóa đơn tạo)
            List<Member> sharers = group.getMembers().stream()
            	    .filter(m -> {
            	        // Kiểm tra an toàn: Nếu không có ngày tham gia hoặc ngày tạo hóa đơn, mặc định là không tính
            	        if (m.getJoinAt() == null || exp.getCreateAt() == null) {
            	            return false; 
            	        }

            	        boolean joinedBefore = m.getJoinAt().isBefore(exp.getCreateAt()) 
            	                            || m.getJoinAt().isEqual(exp.getCreateAt());
            	        
            	        boolean stillInGroup = (m.getLeftAt() == null) 
            	                            || m.getLeftAt().isAfter(exp.getCreateAt());
            	        
            	        return joinedBefore && stillInGroup;
            	    })
            	    .collect(Collectors.toList());

            if (sharers.isEmpty()) continue;
            double shareAmount = amount / sharers.size();


            balances.put(payer.getName(), balances.get(payer.getName()) + amount);

            for (Member s : sharers) {
                balances.put(s.getName(), balances.get(s.getName()) - shareAmount);
            }
        }
        return balances;
    }
}
