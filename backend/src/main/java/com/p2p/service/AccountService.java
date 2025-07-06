package com.p2p.service;

import com.p2p.model.Account;
import com.p2p.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<Account> getAccountById(Integer id) {
        return accountRepository.findById(id);
    }

    public void createAccount(Account account) {
        account.setCardNumber(generateUniqueCardNumber());
        accountRepository.save(account);
    }

    public void closeAccount(Integer id) {
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            acc.setClosed(true);
            accountRepository.update(acc);
        }
    }

    public Optional<Account> getAccountByCardNumber(String cardNumber) {
        return accountRepository.findByCardNumber(cardNumber);
    }

    private String generateUniqueCardNumber() {
        Random random = new Random();
        String cardNumber;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            cardNumber = sb.toString();
        } while (accountRepository.findByCardNumber(cardNumber).isPresent());
        return cardNumber;
    }
} 