package khanh.ntu.BF.controllers; // Bạn đổi lại package cho đúng cấu trúc dự án nhé


import khanh.ntu.BF.models.MemberDebtDto;
import khanh.ntu.BF.models.SettleUpDto;
import khanh.ntu.BF.services.BeFairService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/groups")
public class GroupApiController {

    @Autowired
    private BeFairService beFairService;

    @GetMapping("/{groupId}/settle-up")
    public ResponseEntity<List<SettleUpDto>> getSettleUpInstructions(@PathVariable Long groupId) {
        List<SettleUpDto> instructions = beFairService.getSettleUpInstructions(groupId);
        if (instructions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(instructions);
    }

    @GetMapping("/{groupId}/member/{memberId}/debts")
    public ResponseEntity<List<MemberDebtDto>> getMemberDebts(@PathVariable Long groupId, @PathVariable Long memberId) {
        List<MemberDebtDto> debts = beFairService.getMemberDebts(groupId, memberId);
        if (debts == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(debts);
    }
}