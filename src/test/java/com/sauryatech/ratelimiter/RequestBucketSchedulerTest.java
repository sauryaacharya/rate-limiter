package com.sauryatech.ratelimiter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RequestBucketSchedulerTest {
	
	@Test
	public void testScheduleLeak() throws InterruptedException
	{
		RequestBucket requestBucket = new RequestBucket("abc", 10, 60);
		RequestBucketScheduler requestBucketScheduler = new RequestBucketScheduler(requestBucket);
		
		requestBucket.fill();
		requestBucketScheduler.scheduleLeak();
		
		assertEquals(false, requestBucketScheduler.getScheduler().isShutdown());
		assertEquals(1, requestBucket.getCapacityUsed());
		assertEquals(9, requestBucket.getCapacityLeft());
		
		// wait to clear the request queue in a certain interval of time set by the scheduler
		Thread.sleep(requestBucket.getLeakIntervalToMilliSecond() + 50);
		assertEquals(0, requestBucket.getCapacityUsed());
		assertEquals(10, requestBucket.getCapacityLeft());
	}

}
