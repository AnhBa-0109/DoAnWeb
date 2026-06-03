package khanh.ntu.BF.controllers;

import khanh.ntu.BF.Repository.TravelGroupRepository;
import khanh.ntu.BF.models.SettleUpDto;
import khanh.ntu.BF.models.User;
import khanh.ntu.BF.services.BeFairService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupApiController {

    @Autowired
    private BeFairService beFairService;
    
    @Autowired
    private TravelGroupRepository groupRepository;

    @GetMapping("/{groupId}/settle-up")
    public ResponseEntity<List<SettleUpDto>> getSettleUpInstructions(@PathVariable Long groupId) {
        List<SettleUpDto> instructions = beFairService.getSettleUpInstructions(groupId);
        if (instructions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(instructions);
    }

    
    @GetMapping("/search-users")
    public ResponseEntity<List<Map<String, String>>> searchUsers(@RequestParam String keyword) {
        List<User> users = beFairService.searchUsersByKeyword(keyword);
        
        List<Map<String, String>> result = users.stream().map(u -> {
            Map<String, String> map = new HashMap<>();
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    
    @GetMapping("/{groupId}/bank-info")
    public ResponseEntity<Map<String, String>> getBankInfo(@PathVariable Long groupId,
                                                           @RequestParam String memberName) {
        Map<String, String> info = beFairService.getBankInfoByMemberName(groupId, memberName);

        if (info == null || info.get("bankAccount") == null || info.get("bankCode") == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(info);
    }
}