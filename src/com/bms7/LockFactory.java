package com.bms7;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockFactory {
	private static LockFactory factory = new LockFactory();
	private Map<String, Lock> locks = new HashMap<>();
	
	private LockFactory(){
		
	}
	
	public static LockFactory getInstance(){
		return factory;
	}
	
	public Lock getLock(String accountNumber){
		synchronized (locks) {
			Lock lock = null;
			if (locks.containsKey(accountNumber)){
				lock = locks.get(accountNumber);
			} else {
				lock = new ReentrantLock();
				locks.put(accountNumber, lock);
			}
			
			return lock;
		}
	}
	
	public void clearAllLocks(){
		synchronized (locks) {
			locks.clear();
		}
	}
}
