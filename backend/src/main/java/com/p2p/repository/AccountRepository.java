package com.p2p.repository;

import com.p2p.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findByUserId(Integer userId);
    Optional<Account> findById(Integer id);
    void save(Account account);
    void update(Account account);
    void delete(Integer id);
    Optional<Account> findByCardNumber(String cardNumber);
} 