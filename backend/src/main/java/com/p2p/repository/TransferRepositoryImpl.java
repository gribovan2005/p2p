package com.p2p.repository;

import com.p2p.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TransferRepositoryImpl implements TransferRepository {
    private final JdbcTemplate jdbcTemplate;

    public TransferRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transfer> transferRowMapper = new RowMapper<Transfer>() {
        @Override
        public Transfer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Transfer t = new Transfer();
            t.setId(rs.getInt("id"));
            t.setFromAccountId(rs.getInt("from_account_id"));
            t.setToAccountId(rs.getInt("to_account_id"));
            t.setAmount(rs.getInt("amount"));
            t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return t;
        }
    };

    @Override
    public void save(Transfer transfer) {
        jdbcTemplate.update(
            "INSERT INTO transfers (from_account_id, to_account_id, amount) VALUES (?, ?, ?)",
            transfer.getFromAccountId(),
            transfer.getToAccountId(),
            transfer.getAmount()
        );
    }

    @Override
    public List<Transfer> findByAccountId(Integer accountId) {
        return jdbcTemplate.query(
            "SELECT * FROM transfers WHERE from_account_id = ? OR to_account_id = ? ORDER BY created_at DESC",
            transferRowMapper,
            accountId, accountId
        );
    }
} 