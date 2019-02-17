package com.sauryatech.ratelimiter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author sauryadhwojacharya
 *
 */
public class RequestBucket {

	private String key;

	/**
	 * Number of capacity to hold the request
	 */
	private int capacity;

	/**
	 * time period in second to serve the specified capacity of request
	 */
	private int interval;

	/**
	 * last updated time of the request bucket
	 */
	private long lastUpdated;

	/**
	 * time in second to leak/remove a request from queue
	 */
	private double leakInterval;

	/**
	 * whether the request bucket is overflowed with request or not
	 */
	private boolean isOverflow;

	/**
	 * queue to hold the incoming request in a bucket
	 */
	private Queue<String> requestQueue;

	/**
	 * Leak Scheduler of the request bucket
	 */
	private RequestBucketScheduler requestBucketScheduler;

	public RequestBucket(String key, Integer capacity, Integer interval) {
		this.key = key;
		this.capacity = capacity;
		this.interval = interval;
		this.leakInterval = (double) this.interval / (double) this.capacity;
		this.requestQueue = new LinkedList<String>();
		this.requestBucketScheduler = new RequestBucketScheduler(this);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public double getLeakInterval() {
		return leakInterval;
	}

	public void setLeakInterval(double leakInterval) {
		this.leakInterval = leakInterval;
	}

	public Queue<String> getRequestQueue() {
		return requestQueue;
	}

	public void setRequestQueue(Queue<String> requestQueue) {
		this.requestQueue = requestQueue;
	}

	public boolean getIsOverflow() {
		return isOverflow;
	}

	public void setIsOverflow(boolean isOverflow) {
		this.isOverflow = isOverflow;
	}

	public RequestBucketScheduler getRequestBucketScheduler() {
		return requestBucketScheduler;
	}

	public void setRequestBucketScheduler(RequestBucketScheduler requestBucketScheduler) {
		this.requestBucketScheduler = requestBucketScheduler;
	}

	public void fill() {
		this.requestQueue.add(this.key);
	}

	public void remove() {
		this.requestQueue.poll();
	}

	public int getCapacityLeft() {
		return this.capacity - this.requestQueue.size();
	}

	public int getCapacityUsed() {
		return this.requestQueue.size();
	}

	public boolean isFull() {
		return this.requestQueue.size() == this.capacity;
	}

	public long getLeakIntervalToMilliSecond() {
		return (long) getLeakInterval() * 1000;
	}
}
