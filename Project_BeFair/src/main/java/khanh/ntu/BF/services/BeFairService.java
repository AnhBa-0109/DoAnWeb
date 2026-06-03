package khanh.ntu.BF.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
import khanh.ntu.BF.models.ExpenseDTO;
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
    
    //hàm sửa tên nhóm
    public void editGroup(Long groupId, String newName) {
        TravelGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm!"));
        group.setName(newName);
        groupRepository.save(group);
    }
    
    //hàm xóa nhóm
    @Transactional
    public void deleteGroup(Long groupId) {
        if (groupRepository.existsById(groupId)) {
            groupRepository.deleteById(groupId);
        }
    }
    
    //hàm lấy thông tin nhóm theo id
    public TravelGroup getGroupById(Long id) {
    	return groupRepository.getReferenceById(id);
    }
    
    
    //hàm thêm thành viên mới
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
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên!"));

        List<Expense> expenses = expenseRepository.findByGroupId(m.getGroup().getId());

        boolean isInExpense = expenses.stream().anyMatch(e -> {
            boolean isPayer = e.getPayer() != null && e.getPayer().getId().equals(memberId);
            boolean isSharer = e.getSharers().stream().anyMatch(s -> s.getId().equals(memberId));
            return isPayer || isSharer;
        });

        if (isInExpense) {
            m.setActive(false);
            m.setLeftAt(LocalDateTime.now());
            m.setUser(null);
            memberRepository.save(m);
        } else {
            memberRepository.delete(m);
        }
    }
    
    
    //hàm xóa hóa đơn
    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
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
            List<Member> allActiveMembers = group.getMembers().stream()
                .filter(Member::isActive)
                .collect(Collectors.toList());
            exp.setSharers(allActiveMembers);
        } else {
            List<Member> sharers = memberRepository.findAllById(sharerIds);
            exp.setSharers(sharers);
        }
        
        expenseRepository.save(exp);
    }
    
    //hàm sửa hóa đơn
    @Transactional
    public void updateExpense(Long expenseId, String description, Double amount, Long payerId, List<Long> sharerIds, MultipartFile file) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn cần sửa!"));

        Member payer = memberRepository.findById(payerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên thanh toán!"));

        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setPayer(payer);

        if (sharerIds != null && !sharerIds.isEmpty()) {
            List<Member> sharers = memberRepository.findAllById(sharerIds);
            expense.setSharers(sharers);
        } else {
            expense.setSharers(new ArrayList<>());
        }

        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                if (expense.getInvoiceImage() != null) Files.deleteIfExists(uploadPath.resolve(expense.getInvoiceImage()));
                expense.setInvoiceImage(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi hệ thống khi lưu file ảnh hóa đơn mới!", e);
            }
        }

        expenseRepository.save(expense);
    }
    //hàm kiểm tra chủ nhóm
    public boolean isGroupOwner(Long groupId, String currentUsername) {
        Optional<TravelGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return false;
        }
        return groupOpt.get().getOwner().getUsername().equals(currentUsername);
    }
    
    //hàm tính toán nợ cho từng thành viên trong nhóm
    public Map<String, Double> calculateBalances(Long groupId) {
        TravelGroup group = groupRepository.findById(groupId).orElseThrow();
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        Map<String, Double> balances = new HashMap<>();

        for (Member m : group.getMembers()) {
            if (m.isActive()) balances.put(m.getName(), 0.0);
        }

        for (Expense exp : expenses) {
            if (exp.getPayer() == null || exp.getSharers().isEmpty()) continue;

            double amount = exp.getAmount();
            int numberOfSharers = exp.getSharers().size();
            double shareAmount = amount / numberOfSharers;

            String payerName = exp.getPayer().getName();
            balances.put(payerName, balances.getOrDefault(payerName, 0.0) + amount);

            for (Member sharer : exp.getSharers()) {
                String sharerName = sharer.getName();
                balances.put(sharerName, balances.getOrDefault(sharerName, 0.0) - shareAmount);
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
            
            List<Member> sharers = e.getSharers();
            if (sharers.isEmpty()) continue;

            balances.put(payerId, balances.getOrDefault(payerId, 0.0) + amount);

            Double share = amount / sharers.size();
            for (Member sharer : sharers) {
                if (balances.containsKey(sharer.getId())) {
                    balances.put(sharer.getId(), balances.get(sharer.getId()) - share);
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
    
    //hàm link user với member trong group
    @Transactional
    public void linkMemberToUser(Long memberId, String username) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên!"));
                
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Tài khoản người dùng này không tồn tại trên hệ thống!");
        }
        
        TravelGroup group = member.getGroup();
        boolean alreadyLinked = group.getMembers().stream()
                .anyMatch(m -> m.getUser() != null && m.getUser().getId().equals(user.getId()));
                
        if (alreadyLinked) {
            throw new IllegalArgumentException("Người dùng này đã được liên kết với một thành viên khác trong nhóm!");
        }
        
        member.setUser(user);
        memberRepository.save(member);
    }
    
    //hàm hủy liên kết member với user
    @Transactional
    public void unlinkMemberFromUser(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên!"));
        member.setUser(null);
        memberRepository.save(member);
    }
    
    //hàm tìm kiếm người dùng
    public List<User> searchUsersByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.searchUsers(keyword, PageRequest.of(0, 5));
    }
   
    
    public List<ExpenseDTO> getGroupExpensesForView(Long groupId) {
        TravelGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm!"));

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<ExpenseDTO> dtoList = new ArrayList<>();

        for (Expense expense : expenses) {
            ExpenseDTO dto = new ExpenseDTO();
            dto.setId(expense.getId());
            dto.setDescription(expense.getDescription());
            dto.setAmount(expense.getAmount());
            dto.setCreateAt(expense.getCreateAt());
            
            if (expense.getPayer() != null) {
                dto.setPayerId(expense.getPayer().getId());
                dto.setPayerName(expense.getPayer().getName());
            }
            
            // Chuyển List<Member> thành chuỗi id để dùng ở FE nếu cần
            String sharerIdsStr = expense.getSharers().stream()
                    .map(m -> String.valueOf(m.getId()))
                    .collect(Collectors.joining(","));
            dto.setSharerIds(sharerIdsStr);
            dto.setInvoiceImage(expense.getInvoiceImage());

            String displayText = calculateSharersDisplayText(expense.getSharers(), group);
            dto.setSharersDisplayText(displayText);

            dtoList.add(dto);
        }
        return dtoList;
    }

    private String calculateSharersDisplayText(List<Member> sharers, TravelGroup group) {
        if (sharers == null || sharers.isEmpty()) {
            return "Tất cả thành viên";
        }

        List<Member> allMembers = group.getMembers();
        if (allMembers == null || sharers.size() >= allMembers.size()) {
            return "Tất cả thành viên";
        }

        List<String> sharerIds = sharers.stream()
                .map(m -> String.valueOf(m.getId()))
                .collect(Collectors.toList());

        List<String> excludedNames = new ArrayList<>();
        for (Member m : allMembers) {
            if (!sharerIds.contains(String.valueOf(m.getId()))) {
                excludedNames.add(m.getName());
            }
        }

        if (excludedNames.isEmpty()) return "Tất cả thành viên";
        return "Tất cả trừ: " + String.join(", ", excludedNames);
    }
    
    //cập nhật thông tin cá nhân
    public void updateUserProfile(String username, String fullName, String email, String phoneNumber, String bankAccount, String bankCode) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new IllegalArgumentException("Không tìm thấy người dùng!");

        if (fullName != null && !fullName.trim().isEmpty()) user.setFullName(fullName);
        if (email != null && !email.trim().isEmpty()) user.setEmail(email);
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) user.setPhoneNumber(phoneNumber);
        if (bankAccount != null && !bankAccount.trim().isEmpty()) user.setBankAccount(bankAccount);
        if (bankCode != null && !bankCode.trim().isEmpty()) user.setBankCode(bankCode);

        userRepository.save(user);
    }
    
    //lấy thông tin ngân hàng của user để tạo QR
    public Map<String, String> getBankInfoByMemberName(Long groupId, String memberName) {
        TravelGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm!"));

        return group.getMembers().stream()
                .filter(m -> m.getName().equals(memberName) && m.getUser() != null)
                .findFirst()
                .map(m -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("bankAccount", m.getUser().getBankAccount());
                    info.put("bankCode", m.getUser().getBankCode());
                    info.put("fullName", m.getUser().getFullName());
                    return info;
                })
                .orElse(null);
    }
    
    
    
}
