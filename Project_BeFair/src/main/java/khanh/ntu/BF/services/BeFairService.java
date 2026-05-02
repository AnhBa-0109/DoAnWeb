package khanh.ntu.BF.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

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
    
    public void editMember(Long memberId, String newName) {
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
    
    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }
    
    public void addExpense(Long groupId, String description, Double amount, Long payerId, List<Long> sharerIds, MultipartFile file) {
        TravelGroup group = groupRepository.findById(groupId).orElseThrow();
        Member payer = memberRepository.findById(payerId).orElseThrow();
        
        Expense exp = new Expense();
        exp.setGroup(group);
        exp.setPayer(payer);
        exp.setDescription(description);
        exp.setAmount(amount);
        exp.setCreateAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/" + fileName);
                Files.createDirectories(path.getParent());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                exp.setInvoiceImage(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (sharerIds == null || sharerIds.isEmpty()) {
            List<Long> allActiveIds = group.getMembers().stream()
                .filter(Member::isActive)
                .map(Member::getId)
                .collect(Collectors.toList());
            exp.setSharerIds(StringUtils.join(allActiveIds, ","));
        } else {
            exp.setSharerIds(StringUtils.join(sharerIds, ","));
        }

        expenseRepository.save(exp);
    }
    
    
    public Map<String, Double> calculateBalances(Long groupId) {
        TravelGroup group = groupRepository.findById(groupId).get();
        Map<String, Double> balances = new HashMap<>();
        
        group.getMembers().forEach(m -> balances.put(m.getName(), 0.0));

        for (Expense exp : group.getExpenses()) {
            double amount = exp.getAmount();
            
            String sharerIdsStr = exp.getSharerIds();
            if (sharerIdsStr == null || sharerIdsStr.isEmpty()) {
                continue; 
            }

            String[] ids = sharerIdsStr.split(",");
            int numberOfSharers = ids.length;
            if (numberOfSharers == 0) continue;

            double shareAmount = amount / numberOfSharers;

            String payerName = exp.getPayer().getName();
            balances.put(payerName, balances.get(payerName) + amount);

            for (String idStr : ids) {
                try {
                    Long sId = Long.parseLong(idStr.trim());
                    memberRepository.findById(sId).ifPresent(m -> {
                        balances.put(m.getName(), balances.get(m.getName()) - shareAmount);
                    });
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return balances;
    }
}
