package com.bms7;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/***
 * Main window.
 * 
 * @author William.Vo
 *
 */
public class MainWindow {

	private JFrame frame;
	private ApplicationContext context;
	private JTextField numberOfThreadsField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
										
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 423, 225);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		context = new ClassPathXmlApplicationContext("Beans.xml");
		
		JLabel lblNumberOfThreads = new JLabel("Number of threads");
		frame.getContentPane().add(lblNumberOfThreads, "2, 2, right, default");
		
		numberOfThreadsField = new JTextField();
		numberOfThreadsField.setHorizontalAlignment(SwingConstants.RIGHT);
		numberOfThreadsField.setText("10");
		frame.getContentPane().add(numberOfThreadsField, "4, 2, fill, default");
		numberOfThreadsField.setColumns(2);
		
		JButton btnDelete = new JButton("Delete All Existing Accounts");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteAll();
			}
		});
		frame.getContentPane().add(btnDelete, "4, 4");
		
		JButton btnInsert = new JButton("New accounts and deposit");
		frame.getContentPane().add(btnInsert, "4, 6");
		
		btnInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insert();
			}
		});
	}
	
	/***
	 * Create an account with a random unique account number.
	 * @return
	 */
	private BankAccount createAccount(){
		BankAccount account = new BankAccount();
		String accountNumber = UUID.randomUUID().toString();
		account.setAccountNumber(accountNumber);
		account.setBalance(BigDecimal.ZERO);
		ClwDao dao = (ClwDao) context.getBean("clwDao");
		dao.insert(account);
		return account;
	}
	
	/***
	 * Create 10 tasks to add $1 to 1 account and another task to add $1 to another account.
	 * 
	 * The purpose of this exercise is to prove that 10 threads simultaneously update a record without
	 * any deadlock by using locks.
	 * 
	 * 
	 */
	private void insert(){
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			BankAccount account1 = createAccount();
			BankAccount account2 = createAccount();
			
			ClwDao dao = (ClwDao) context.getBean("clwDao");
			PlatformTransactionManager transactionManager = (PlatformTransactionManager) context.getBean("transactionManager");
			List<FutureTask<BigDecimal>> futureTasks = new ArrayList<>();
			
			int numberOfThreads = Integer.valueOf(numberOfThreadsField.getText());
			
			for (int i = numberOfThreads; i > 0; i --){
				AsyncBankAccountTask task = new AsyncBankAccountTask(i, dao, account1.getAccountNumber(), transactionManager);
				FutureTask<BigDecimal> futureTask = new FutureTask<>(task);
				futureTasks.add(futureTask);
				executor.execute(futureTask);
			}
			
			AsyncBankAccountTask task2 = new AsyncBankAccountTask(++numberOfThreads, dao, account2.getAccountNumber(), transactionManager);
			FutureTask<BigDecimal> futureTask2 = new FutureTask<>(task2);
			futureTasks.add(futureTask2);
			executor.execute(futureTask2);
			
			
			//wait for completion
			for (FutureTask<BigDecimal> futureTask : futureTasks) {
				try {
					futureTask.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
		} finally{
			if (! executor.isShutdown()){
				executor.shutdown();
				LockFactory.getInstance().clearAllLocks();
			}
		}
	}
	
	private void deleteAll(){
		ClwDao dao = (ClwDao) context.getBean("clwDao");
		dao.deleteAll();
	}
}
