package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeferredTest {
	
	Deferred<Integer> testedDeferred;

	@Before
	public void setUp() throws Exception {
		testedDeferred = new Deferred<Integer>();
	}

	@After
	public void tearDown() throws Exception {
		testedDeferred = null;
	}

	@Test
	public void testGet() {
		testedDeferred.resolve(new Integer(10));
		try{
			Integer returnValueInteger = testedDeferred.get();
			assertEquals(10, returnValueInteger.intValue());
		}
		catch(IllegalStateException e){
			fail("Object should have been resolved and deferred value available");
		}
	}

	@Test
	public void testIsResolved() {
		assertFalse(testedDeferred.isResolved());
		testedDeferred.resolve(new Integer(10));
		assertTrue(testedDeferred.isResolved());
	}

	@Test
	public void testResolve() {
		testedDeferred.whenResolved(() -> {});
		assertEquals(1, testedDeferred.getNumberOfQueuedCallbacks());
		testedDeferred.resolve(new Integer(10));
		assertEquals(0, testedDeferred.getNumberOfQueuedCallbacks());
		testedDeferred.whenResolved(() -> {});
		assertEquals(0, testedDeferred.getNumberOfQueuedCallbacks());
	}

	@Test
	public void testWhenResolved() {
		assertEquals(0, testedDeferred.getNumberOfQueuedCallbacks());
		testedDeferred.whenResolved(() -> {});
		assertEquals(1, testedDeferred.getNumberOfQueuedCallbacks());
		testedDeferred.whenResolved(() -> {});
		assertEquals(2, testedDeferred.getNumberOfQueuedCallbacks());
	}

}
