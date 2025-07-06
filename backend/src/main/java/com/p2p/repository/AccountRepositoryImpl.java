package com.p2p.repository;

import com.p2p.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepositoryImpl implements AccountRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Account> accountRowMapper = new RowMapper<Account>() {
        @Override
        public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
            Account account = new Account();
            account.setId(rs.getInt("id"));
            account.setUserId(rs.getInt("user_id"));
            account.setBalance(rs.getInt("balance"));
            account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            account.setClosed(rs.getBoolean("closed"));
            account.setCardNumber(rs.getString("card_number"));
            return account;
        }
    };

    @Override
    public List<Account> findByUserId(Integer userId) {
        return jdbcTemplate.query(
            "SELECT * FROM accounts WHERE user_id = ?",
            accountRowMapper,
            userId
        );
    }

    @Override
    public Optional<Account> findById(Integer id) {
        List<Account> list = jdbcTemplate.query(
            "SELECT * FROM accounts WHERE id = ?",
            accountRowMapper,
            id
        );
        return list.stream().findFirst();
    }

    @Override
    public void save(Account account) {
        jdbcTemplate.update(
            "INSERT INTO accounts (user_id, balance, closed, card_number) VALUES (?, ?, ?, ?)",
            account.getUserId(),
            account.getBalance(),
            account.getClosed() != null ? account.getClosed() : false,
            account.getCardNumber()
        );
    }

    @Override
    public void update(Account account) {
        jdbcTemplate.update(
            "UPDATE accounts SET balance = ?, closed = ? WHERE id = ?",
            account.getBalance(),
            account.getClosed(),
            account.getId()
        );
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM accounts WHERE id = ?", id);
    }

    @Override
    public Optional<Account> findByCardNumber(String cardNumber) {
        List<Account> list = jdbcTemplate.query(
            "SELECT * FROM accounts WHERE card_number = ?",
            accountRowMapper,
            cardNumber
        );
        return list.stream().findFirst();
    }
} 