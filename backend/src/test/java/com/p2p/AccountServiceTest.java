package com.p2p;

import com.p2p.model.Account;
import com.p2p.repository.AccountRepository;
import com.p2p.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    private AccountRepository accountRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountService = new AccountService(accountRepository);
    }

    @Test
    void getAccountsByUserId_returnsAccounts() {
        Account acc1 = new Account(); acc1.setUserId(1);
        Account acc2 = new Account(); acc2.setUserId(1);
        when(accountRepository.findByUserId(1)).thenReturn(Arrays.asList(acc1, acc2));
        List<Account> result = accountService.getAccountsByUserId(1);
        assertEquals(2, result.size());
    }

    @Test
    void getAccountById_found() {
        Account acc = new Account(); acc.setId(1);
        when(accountRepository.findById(1)).thenReturn(Optional.of(acc));
        Optional<Account> result = accountService.getAccountById(1);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void getAccountById_notFound() {
        when(accountRepository.findById(2)).thenReturn(Optional.empty());
        Optional<Account> result = accountService.getAccountById(2);
        assertFalse(result.isPresent());
    }

    @Test
    void createAccount_savesAccount() {
        Account acc = new Account();
        accountService.createAccount(acc);
        verify(accountRepository).save(acc);
    }

    @Test
    void closeAccount_found_updatesClosed() {
        Account acc = new Account(); acc.setId(1); acc.setClosed(false);
        when(accountRepository.findById(1)).thenReturn(Optional.of(acc));
        accountService.closeAccount(1);
        assertTrue(acc.getClosed());
        verify(accountRepository).update(acc);
    }

    @Test
    void closeAccount_notFound_doesNothing() {
        when(accountRepository.findById(2)).thenReturn(Optional.empty());
        accountService.closeAccount(2);
        verify(accountRepository, never()).update(any());
    }
} 