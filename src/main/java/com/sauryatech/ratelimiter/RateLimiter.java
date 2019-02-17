package com.sauryatech.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author sauryadhwojacharya
 * 
 * Controller class for rate limiting
 *
 */
public class RateLimiter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static RateLimiter rateLimiter;

	/**
	 * Map to store the key and its bucket for the request
	 */
	private static ConcurrentHashMap<String, RequestBucket> requestMap = new ConcurrentHashMap<String, RequestBucket>();

	private RateLimiter() {
	}

	/**
	 * 
	 * @param key      key used to apply rate limiter that can be apiKey, ip address
	 * @param capacity the number of request
	 * @param interval interval is in second, the period of time that can serve the
	 *                 specified number of request
	 * @return singleton RateLimiter instance
	 */
	public static RateLimiter create(String key, int capacity, int interval) {
		if (rateLimiter == null) {
			rateLimiter = new RateLimiter();
		}

		if (!requestMap.containsKey(key)) {
			RequestBucket requestBucket = new RequestBucket(key, capacity, interval);
			rateLimiter.logger.info("Request Bucket created with Key: {}, capacity of {} request in {}s", key, capacity,
					interval);
			
			requestBucket.setLastUpdated(System.currentTimeMillis());
			requestMap.put(key, requestBucket);
		}
		return rateLimiter;
	}

	/**
	 * 
	 * @param key
	 * @return RequestBucket for specific key
	 */
	public RequestBucket getRequestBucket(String key) {
		return requestMap.get(key);
	}

	/**
	 * fill the request bucket with queue of request
	 * 
	 * @param key
	 */
	public void fill(String key) {
		RequestBucket requestBucket = getRequestBucket(key);
		if (!requestBucket.isFull()) {
			requestBucket.fill();
			logger.info("Added request to the bucket for key: {}, Capacity Used: {}, Capacity Left: {}", key,
					requestBucket.getCapacityUsed(), requestBucket.getCapacityLeft());
			
			requestBucket.setLastUpdated(System.currentTimeMillis());
			requestBucket.setIsOverflow(false);

			ScheduledExecutorService scheduler = requestBucket.getRequestBucketScheduler().getScheduler();
			
			// start the scheduled task for the given request bucket
			// if scheduler is null or in shutdown state
			if (scheduler == null || scheduler.isShutdown()) {
				requestBucket.getRequestBucketScheduler().scheduleLeak();
			}
		} else {
			requestBucket.setIsOverflow(true);
			logger.info("Cannot add more request, bucket is full for key: {}. Wait for {}s", key, getWaitingTime(key));
		}
	}

	/**
	 * Get the waiting time to send the request again once the request limit has
	 * been reached
	 * 
	 * @param key
	 * @return the waiting time in second
	 */
	public double getWaitingTime(String key) {
		RequestBucket requestBucket = getRequestBucket(key);
		if (requestBucket.isFull()) {
			long lastUpdatedTimeWhenFull = requestBucket.getLastUpdated();
			long currentTime = System.currentTimeMillis();
			long elapsed = currentTime - lastUpdatedTimeWhenFull;
			long interval = requestBucket.getLeakIntervalToMilliSecond();
			return (double) (interval - elapsed) / 1000;
		}
		return 0;
	}
}
