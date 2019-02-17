package com.sauryatech.ratelimiter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBucketScheduler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private RequestBucket requestBucket;

	/**
	 * Service to schedule the task to remove the expired request from the queue
	 */
	private ScheduledExecutorService scheduler;

	public RequestBucketScheduler(RequestBucket requestBucket) {
		this.requestBucket = requestBucket;
		this.requestBucket.setRequestBucketScheduler(this);
	}

	public RequestBucket getRequestBucket() {
		return requestBucket;
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	/**
	 * Schedule a task to remove the expired request from a queue with a constant
	 * leak rate. The request bucket has its own constant leak rate to remove the
	 * expired request from queue
	 * 
	 */
	public synchronized void scheduleLeak() {
		
		// time interval to remove one request from a queue
		long leakIntervalForOneRequest = requestBucket.getLeakIntervalToMilliSecond();
		
		if (!requestBucket.getRequestQueue().isEmpty()) {
			scheduler = Executors.newScheduledThreadPool(1);
			logger.info("Scheduled the expired request clearance at the rate of: 1 request per {}s",
					requestBucket.getLeakInterval());
			
			// start the scheduled task for clearing the expired request from queue
			// at a constant rate based on leak interval for one request
			scheduler.scheduleAtFixedRate(() -> {
				requestBucket.remove();
				logger.info("Cleared the expired request for key: {}, Capacity Used: {}, Capacity Left: {}",
						requestBucket.getKey(), requestBucket.getCapacityUsed(), requestBucket.getCapacityLeft());
				
				// shutdown the scheduler if there is no request in a queue
				if (requestBucket.getRequestQueue().isEmpty()) {
					scheduler.shutdown();
				}
			}, leakIntervalForOneRequest, leakIntervalForOneRequest, TimeUnit.MILLISECONDS);
		}
	}

}
