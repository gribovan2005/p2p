package com.p2p;

import com.p2p.model.Account;
import com.p2p.model.Transfer;
import com.p2p.repository.AccountRepository;
import com.p2p.repository.TransferRepository;
import com.p2p.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {
    private AccountRepository accountRepository;
    private TransferRepository transferRepository;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transferRepository = mock(TransferRepository.class);
        transferService = new TransferService(accountRepository, transferRepository);
    }

    @Test
    void transfer_success() {
        Account from = new Account(); from.setId(1); from.setBalance(1000); from.setClosed(false);
        Account to = new Account(); to.setId(2); to.setBalance(500); to.setClosed(false);
        when(accountRepository.findById(1)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2)).thenReturn(Optional.of(to));
        transferService.transfer(1, 2, 300);
        assertEquals(700, from.getBalance());
        assertEquals(800, to.getBalance());
        verify(accountRepository).update(from);
        verify(accountRepository).update(to);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void transfer_insufficientFunds_throws() {
        Account from = new Account(); from.setId(1); from.setBalance(100); from.setClosed(false);
        Account to = new Account(); to.setId(2); to.setBalance(500); to.setClosed(false);
        when(accountRepository.findById(1)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2)).thenReturn(Optional.of(to));
        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(1, 2, 200));
    }

    @Test
    void transfer_sameAccount_throws() {
        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(1, 1, 100));
    }

    @Test
    void transfer_closedSource_throws() {
        Account from = new Account(); from.setId(1); from.setBalance(1000); from.setClosed(true);
        Account to = new Account(); to.setId(2); to.setBalance(500); to.setClosed(false);
        when(accountRepository.findById(1)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2)).thenReturn(Optional.of(to));
        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(1, 2, 100));
    }

    @Test
    void transfer_closedTarget_throws() {
        Account from = new Account(); from.setId(1); from.setBalance(1000); from.setClosed(false);
        Account to = new Account(); to.setId(2); to.setBalance(500); to.setClosed(true);
        when(accountRepository.findById(1)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2)).thenReturn(Optional.of(to));
        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(1, 2, 100));
    }

    @Test
    void transfer_invalidAmount_throws() {
        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(1, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(1, 2, -10));
        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(1, 2, null));
    }
} 