package com.bms7;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class BankAccountMapper implements RowMapper<BankAccount> {

	@Override
	public BankAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
		BankAccount account = new BankAccount();
		account.setId(rs.getInt("id"));
		account.setAccountNumber(rs.getString("accountNumber"));
		account.setBalance(rs.getBigDecimal("balance"));
		return account;
	}

}
