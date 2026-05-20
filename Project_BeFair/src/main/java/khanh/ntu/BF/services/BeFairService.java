package khanh.ntu.BF.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import khanh.ntu.BF.models.MemberDebtDto;
import khanh.ntu.BF.models.SettleUpDto;
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
    
    
    private List<Long> parseSharerIds(String sharerIdsStr) {
        List<Long> ids = new ArrayList<>();
        if (sharerIdsStr == null || sharerIdsStr.trim().isEmpty()) {
            return ids;
        }
        String[] split = sharerIdsStr.split(",");
        for (String s : split) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                try {
                    ids.add(Long.parseLong(trimmed));
                } catch (NumberFormatException e) {
                }
            }
        }
        return ids;
    }

    public List<SettleUpDto> getSettleUpInstructions(Long groupId) {
        Optional<TravelGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) return null;
        TravelGroup group = groupOpt.get();

        Map<Long, Double> balances = new HashMap<>();
        Map<Long, String> memberIdToName = new HashMap<>();

        for (Member m : group.getMembers()) {
            balances.put(m.getId(), 0.0);
            memberIdToName.put(m.getId(), m.getName());
        }

        for (Expense e : group.getExpenses()) {
            if (e.getPayer() == null || e.getAmount() == null) continue;

            Long payerId = e.getPayer().getId();
            Double amount = e.getAmount();
            
            List<Long> sharerIds = parseSharerIds(e.getSharerIds());
            if (sharerIds.isEmpty()) continue;

            balances.put(payerId, balances.getOrDefault(payerId, 0.0) + amount);

            Double share = amount / sharerIds.size();
            for (Long sharerId : sharerIds) {
                if (balances.containsKey(sharerId)) {
                    balances.put(sharerId, balances.get(sharerId) - share);
                }
            }
        }

        List<Map.Entry<Long, Double>> debtors = new ArrayList<>();
        List<Map.Entry<Long, Double>> creditors = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : balances.entrySet()) {
            if (entry.getValue() < -0.01) {
                debtors.add(entry);
            } else if (entry.getValue() > 0.01) {
                creditors.add(entry);
            }
        }

        List<SettleUpDto> instructions = new ArrayList<>();
        int d = 0, c = 0;

        while (d < debtors.size() && c < creditors.size()) {
            Map.Entry<Long, Double> debtor = debtors.get(d);
            Map.Entry<Long, Double> creditor = creditors.get(c);

            Double owedAmount = -debtor.getValue();
            Double creditAmount = creditor.getValue();
            Double minAmount = Math.min(owedAmount, creditAmount);

            instructions.add(new SettleUpDto(
                memberIdToName.get(debtor.getKey()),
                memberIdToName.get(creditor.getKey()),
                minAmount
            ));

            debtor.setValue(debtor.getValue() + minAmount);
            creditor.setValue(creditor.getValue() - minAmount);

            if (Math.abs(debtor.getValue()) < 0.01) d++;
            if (Math.abs(creditor.getValue()) < 0.01) c++;
        }

        return instructions;
    }

    public List<MemberDebtDto> getMemberDebts(Long groupId, Long memberId) {
        Optional<TravelGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) return null;
        TravelGroup group = groupOpt.get();

        List<MemberDebtDto> debtList = new ArrayList<>();

        for (Expense e : group.getExpenses()) {
            if (e.getAmount() == null) continue;

            List<Long> sharerIds = parseSharerIds(e.getSharerIds());
            if (sharerIds.contains(memberId)) {
                Double amountOwed = e.getAmount() / sharerIds.size();
                String payerName = (e.getPayer() != null) ? e.getPayer().getName() : "Không rõ";

                debtList.add(new MemberDebtDto(
                    e.getDescription(),
                    amountOwed,
                    payerName
                ));
            }
        }
        return debtList;
    }
}
