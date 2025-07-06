package com.p2p.service;

import com.p2p.model.Account;
import com.p2p.model.Transfer;
import com.p2p.repository.AccountRepository;
import com.p2p.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public TransferService(AccountRepository accountRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public void transfer(Integer fromAccountId, Integer toAccountId, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        Account from = accountRepository.findById(fromAccountId).orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account to = accountRepository.findById(toAccountId).orElseThrow(() -> new IllegalArgumentException("Target account not found"));
        if (from.getClosed() != null && from.getClosed()) {
            throw new IllegalArgumentException("Source account is closed");
        }
        if (to.getClosed() != null && to.getClosed()) {
            throw new IllegalArgumentException("Target account is closed");
        }
        if (from.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        accountRepository.update(from);
        accountRepository.update(to);
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setAmount(amount);
        transferRepository.save(transfer);
    }
} 