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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import jakarta.transaction.Transactional;
import khanh.ntu.BF.Repository.ExpenseRepository;
import khanh.ntu.BF.Repository.MemberRepository;
import khanh.ntu.BF.Repository.TravelGroupRepository;
import khanh.ntu.BF.Repository.UserRepository;
import khanh.ntu.BF.models.Expense;
import khanh.ntu.BF.models.Member;
import khanh.ntu.BF.models.TravelGroup;
import khanh.ntu.BF.models.User;

@Service
public class BeFairService {
	@Autowired
	private TravelGroupRepository groupRepository;
	@Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    
    //hàm thêm nhóm mới
    public void addNewGroup(TravelGroup group) {
    	groupRepository.save(group);
    }
    
    //hàm lấy thông tin nhóm theo id
    public TravelGroup getGroupById(Long id) {
    	return groupRepository.getReferenceById(id);
    }
    
    
    //hàm thêm thành viên mới của từng nhóm
    public void addNewMember(Long groupId, String name) {
    	TravelGroup group = groupRepository.findById(groupId).get();
        Member member = new Member();
        member.setName(name);
        member.setGroup(group);
        memberRepository.save(member);
    }
    
    //hàm sửa tên thành viên
    public void editMember(Long memberId, String newName) {
    	Member member = memberRepository.findById(memberId).get();
        member.setName(newName);
        memberRepository.save(member);
    }
    
    //hàm xóa thành viên khỏi nhóm
    public void deleteMember(Long memberId) {
        Member m = memberRepository.findById(memberId).orElse(null);
        if (m != null) {
            m.setActive(false);
            m.setLeftAt(LocalDateTime.now());
            memberRepository.save(m);
        }
    }
    
    //hàm xóa hóa đơn
    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }
    
    
    //hàm xóa nhóm
    @Transactional
    public void deleteGroup(Long groupId) {
        if (groupRepository.existsById(groupId)) {
            groupRepository.deleteById(groupId);
        }
    }
    
    
    //hàm thêm hóa đơn
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
    
    
    //hàm tính toán nợ cho từng thành viên trong nhóm
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
    
    
    //hàm đăng kí tài khoản
    public void registerUser(String username, String password, String fullName) throws Exception {
        if (userRepository.findByUsername(username) != null) {
            throw new Exception("Tên đăng nhập này đã có người dùng rồi!");
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        userRepository.save(user);
    }
}
