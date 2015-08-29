package com.bms7;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/***
 * LockFactory is a singleton that provides locks to all clients.
 * 
 * @author William.Vo
 *
 */
public class LockFactory {
	private static LockFactory factory = new LockFactory();
	private Map<String, Lock> locks = new ConcurrentHashMap<>();
	
	private LockFactory(){
	}
	
	public static LockFactory getInstance(){
		return factory;
	}
	
	/***
	 * Get lock per account.
	 * @param accountNumber
	 * @return
	 */
	public Lock getLock(String accountNumber){
		Lock lock = null;
		if (locks.containsKey(accountNumber)){
			lock = locks.get(accountNumber);
		} else {
			lock = new ReentrantLock();
			locks.put(accountNumber, lock);
		}
		
		return lock;
	}
	
	public void clearAllLocks(){
		locks.clear();
	}
}
