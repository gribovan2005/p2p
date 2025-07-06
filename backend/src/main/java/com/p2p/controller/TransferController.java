package com.p2p.controller;

import com.p2p.model.Transfer;
import com.p2p.service.TransferService;
import com.p2p.repository.TransferRepository;
import com.p2p.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.p2p.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;
    private final TransferRepository transferRepository;
    private final UserService userService;
    private final AccountService accountService;

    public TransferController(TransferService transferService, TransferRepository transferRepository, UserService userService, AccountService accountService) {
        this.transferService = transferService;
        this.transferRepository = transferRepository;
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request, Authentication authentication) {
        Integer userId = getUserId(authentication);
        transferService.transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account/{accountId}")
    public List<Transfer> getTransfers(@PathVariable Integer accountId) {
        return transferRepository.findByAccountId(accountId);
    }

    @PostMapping("/by-card")
    public ResponseEntity<?> transferByCard(@RequestBody TransferByCardRequest request, Authentication authentication) {
        Integer userId = getUserId(authentication);
        Integer fromAccountId = request.getFromAccountId();
        String toCardNumber = request.getToCardNumber();
        Integer amount = request.getAmount();
        var toAccountOpt = accountService.getAccountByCardNumber(toCardNumber);
        if (toAccountOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Карта не найдена");
        }
        Integer toAccountId = toAccountOpt.get().getId();
        transferService.transfer(fromAccountId, toAccountId, amount);
        return ResponseEntity.ok().build();
    }

    private Integer getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username).getId();
    }

    public static class TransferRequest {
        private Integer fromAccountId;
        private Integer toAccountId;
        private Integer amount;
        public Integer getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Integer fromAccountId) { this.fromAccountId = fromAccountId; }
        public Integer getToAccountId() { return toAccountId; }
        public void setToAccountId(Integer toAccountId) { this.toAccountId = toAccountId; }
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
    }

    public static class TransferByCardRequest {
        private Integer fromAccountId;
        private String toCardNumber;
        private Integer amount;
        public Integer getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Integer fromAccountId) { this.fromAccountId = fromAccountId; }
        public String getToCardNumber() { return toCardNumber; }
        public void setToCardNumber(String toCardNumber) { this.toCardNumber = toCardNumber; }
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
    }
} 