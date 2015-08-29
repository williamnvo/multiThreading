package com.bms7;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class ClwDao {
	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplate;
	
	@PostConstruct
	public void postConstruct(){
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public List<BankAccount> retrieve(String accountNumber){
		String sql = "select * from bank_account where accountNumber = ? for update";
		return jdbcTemplate.query(sql, new Object[]{accountNumber}, new BankAccountMapper());
	}
	
	public void insert(BankAccount account){
		String sql = "insert into bank_account (accountNumber, balance) values(?, ?) ";
		jdbcTemplate.update(sql, new Object[]{account.getAccountNumber(), account.getBalance()});
	}
	
	public void update(BankAccount account){
		String sql = "update bank_account set balance = ? where accountNumber = ?";
		jdbcTemplate.update(sql, new Object[]{account.getBalance(), account.getAccountNumber()});
	}
	
	public void deleteAll(){
		String sql = "delete from bank_account";
		jdbcTemplate.update(sql);
	}
}
