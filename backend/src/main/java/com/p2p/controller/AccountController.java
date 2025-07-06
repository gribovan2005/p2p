package com.p2p.controller;

import com.p2p.model.Account;
import com.p2p.service.AccountService;
import com.p2p.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public List<Account> getAccounts(Authentication authentication) {
        Integer userId = getUserId(authentication);
        return accountService.getAccountsByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request, Authentication authentication) {
        Integer userId = getUserId(authentication);
        Account acc = new Account();
        acc.setUserId(userId);
        acc.setBalance(request.getInitialBalance());
        acc.setClosed(false);
        accountService.createAccount(acc);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> closeAccount(@PathVariable Integer id, Authentication authentication) {
        Integer userId = getUserId(authentication);
        Account acc = accountService.getAccountById(id).orElse(null);
        if (acc == null || !acc.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        accountService.closeAccount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Integer id, Authentication authentication) {
        Integer userId = getUserId(authentication);
        Account acc = accountService.getAccountById(id).orElse(null);
        if (acc == null || !acc.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(acc.getBalance());
    }

    private Integer getUserId(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username).getId();
    }

    public static class CreateAccountRequest {
        private Integer initialBalance;
        public Integer getInitialBalance() { return initialBalance; }
        public void setInitialBalance(Integer initialBalance) { this.initialBalance = initialBalance; }
    }
} 