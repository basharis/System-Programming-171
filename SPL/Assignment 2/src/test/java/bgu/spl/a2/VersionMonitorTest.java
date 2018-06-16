package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionMonitorTest {
	
	VersionMonitor testedVersionMonitor;

	@Before
	public void setUp() throws Exception {
		testedVersionMonitor = new VersionMonitor();
	}

	@After
	public void tearDown() throws Exception {
		testedVersionMonitor = null;
	}

	@Test
	/**
	 * Testing getVersion():
	 * The test makes sure the function returns the current valid version number.
	 * After initialization, the version number is 0. 
	 */
	public void testGetVersion() {
		assertEquals(0, testedVersionMonitor.getVersion());
	}

	@Test
	/**
	 * Testing inc():
	 * The test is first making sure the version number is i.
	 * then calls inc() to increment it by 1 and finally,
	 * checks the version number again to see that it changed to i+1
	 */
	public void testInc() {
		for (int i = 0 ; i<5 ; i++){
			assertEquals(i, testedVersionMonitor.getVersion());
			testedVersionMonitor.inc();
			assertEquals(i+1, testedVersionMonitor.getVersion());
		}
	}
	
	@Test
	/**
	 * Testing await():
	 * The test starts a thread that calls the testedVersionMonitor's await function, 
	 * making sure the thread is WAITING afterwards.
	 * Then it uses inc() to increment the version by 1, making sure the thread was notified and not WAITING any longer.
	 */
	public void testAwait() {
		// Thread that waits for testVM version to change
		Thread t1 = new Thread(() -> { 
			try{testedVersionMonitor.await(0);}
			catch (InterruptedException e){} 
		});
		
		t1.start(); // Start the waiting process
		
		try {Thread.sleep(50);} catch (InterruptedException e1) {}
 		assertTrue(t1.getState() == Thread.State.WAITING);
		
		testedVersionMonitor.inc();
		
		try {Thread.sleep(50);} catch (InterruptedException e1) {}
		assertTrue(t1.getState() != Thread.State.WAITING);
	}

}
