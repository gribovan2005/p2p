package com.p2p.repository;

import com.p2p.model.Transfer;
import java.util.List;

public interface TransferRepository {
    void save(Transfer transfer);
    List<Transfer> findByAccountId(Integer accountId);
} 