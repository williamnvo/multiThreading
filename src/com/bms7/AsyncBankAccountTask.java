package com.bms7;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class AsyncBankAccountTask implements Callable<BigDecimal> {
	private String accountNumber;
	private ClwDao dao;
	private int threadNumber;
	private TransactionTemplate transactionTemplate;
	
	public AsyncBankAccountTask(int threadNumber, ClwDao dao, String accountNumber, PlatformTransactionManager transactionManager){
		this.dao = dao;
		this.accountNumber = accountNumber;
		this.threadNumber = threadNumber;
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Override
	public BigDecimal call() throws Exception {

		return transactionTemplate.execute(new TransactionCallback<BigDecimal>() {

			@Override
			public BigDecimal doInTransaction(TransactionStatus arg0) {
				System.out.println(String.format("Thread #%s starts", threadNumber));
				Lock lock = LockFactory.getInstance().getLock(accountNumber);				
				lock.lock();
				System.out.println(String.format("Thread #%s acquires lock", threadNumber));

				try {
					List<BankAccount> accounts = dao.retrieve(accountNumber);
					sleep();

					BankAccount account = accounts.get(0);
					BigDecimal deposit = BigDecimal.ONE;
					BigDecimal balance = account.getBalance().add(deposit);
					account.setBalance(balance);

					dao.update(account);					

					return balance;
				} finally {
					System.out.println(String.format("******************* Thread #%s is done", threadNumber));
					lock.unlock();
				}
			}

			private void sleep() {
				try {
					Thread.sleep(1000 );
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public ClwDao getDao() {
		return dao;
	}

	public void setDao(ClwDao dao) {
		this.dao = dao;
	}
}
