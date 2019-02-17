package com.sauryatech.ratelimiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class RateLimiterTest {
	
	private static final int CAPACITY = 10;

	// interval is expected in second
	private static final int INTERVAL = 60;
	
	@Test
	public void testCreateRateLimiter()
	{
		RateLimiter rateLimiter = RateLimiter.create("ab123", CAPACITY, INTERVAL);
		assertNotNull(rateLimiter);
		
		RequestBucket requestBucket = rateLimiter.getRequestBucket("ab123");
		assertNotNull(requestBucket);
		assertEquals("ab123", requestBucket.getKey());
		assertEquals(10, requestBucket.getCapacity());
		assertEquals(60, requestBucket.getInterval());
		assertEquals(6.0, requestBucket.getLeakInterval(), 0.0);
		assertNotNull(requestBucket.getRequestBucketScheduler());
		assertNotNull(requestBucket.getRequestQueue());
		assertEquals(0, requestBucket.getRequestQueue().size());
		assertEquals(false, requestBucket.isFull());
		assertEquals(false, requestBucket.getIsOverflow());
		assertEquals(6000, requestBucket.getLeakIntervalToMilliSecond());
		assertEquals(0, requestBucket.getCapacityUsed());
		assertEquals(10, requestBucket.getCapacityLeft());
		
		RequestBucketScheduler requestBucketScheduler = requestBucket.getRequestBucketScheduler();
		assertEquals(requestBucket, requestBucketScheduler.getRequestBucket());
		
		// dont create scheduler until we fill the request
		assertNull(requestBucketScheduler.getScheduler());
	}
	
	@Test
	public void testFill() throws InterruptedException
	{
		RateLimiter rateLimiter = RateLimiter.create("ab134", CAPACITY, INTERVAL);
		rateLimiter.fill("ab134");
		RequestBucket requestBucket = rateLimiter.getRequestBucket("ab134");
		
		assertEquals(1, requestBucket.getRequestQueue().size());
		assertEquals(false, requestBucket.isFull());
		assertEquals(false, requestBucket.getIsOverflow());
		assertEquals(1, requestBucket.getCapacityUsed());
		assertEquals(9, requestBucket.getCapacityLeft());
		
		// wait to clear the request queue in a certain interval of time
		Thread.sleep(requestBucket.getLeakIntervalToMilliSecond() + 50);
		assertEquals(0, requestBucket.getCapacityUsed());
		assertEquals(10, requestBucket.getCapacityLeft());
		
		// fill all the request bucket beyond its capacity
		for (int i = 0; i < 11; i++)
		{
			rateLimiter.fill("ab134");
		}
		
		assertEquals(10, requestBucket.getRequestQueue().size());
		assertEquals(true, requestBucket.isFull());
		assertEquals(true, requestBucket.getIsOverflow());
		assertEquals(10, requestBucket.getCapacityUsed());
		assertEquals(0, requestBucket.getCapacityLeft());
	}
	
	@Test
	public void testGetWaitingTime()
	{
		RateLimiter rateLimiter = RateLimiter.create("cab23", CAPACITY, INTERVAL);
		for (int i = 0; i < 11; i++)
		{
			rateLimiter.fill("cab23");
		}
		assertEquals(6.0, rateLimiter.getWaitingTime("cab23"), 0.0);
	}

}
