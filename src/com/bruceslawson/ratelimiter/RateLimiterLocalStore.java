package com.bruceslawson.ratelimiter;

import java.util.HashMap;
import java.util.Random;

public class RateLimiterLocalStore extends RateLimiter {
	
	public RateLimiterLocalStore(long rateCount, long ratePeriodSeconds, int numberOfBuckets) {
		this(rateCount, ratePeriodSeconds, numberOfBuckets, false);
	}
	
	public RateLimiterLocalStore(long rateCount, long ratePeriodSeconds, int numberOfBuckets, boolean isDebug) {
		super(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug);

		_keyedBuckets = new HashMap<String, Buckets>();
	}
	
	
	public void incrementCount(String key, int count) {
		if(_isDebug) {
			logger("Incrementing count by " + count + " for key " + key);
		}
		
		Buckets buckets = _keyedBuckets.get(key);
		if(buckets == null) {
			buckets = new Buckets(_rateCount, _ratePeriodMillis/1000L, _numberOfBuckets, _isDebug);
			_keyedBuckets.put(key, buckets);
		}
		
		buckets.incrementCount(count);
	}
	

	public boolean isLimited(String key, boolean isUpdateCount, int count) {
		
		Buckets buckets = _keyedBuckets.get(key);
		if(buckets == null) {
			buckets = new Buckets(_rateCount, _ratePeriodMillis/1000L, _numberOfBuckets, _isDebug);
			_keyedBuckets.put(key, buckets);
		}
		
		boolean isLimited = buckets.isLimited(isUpdateCount, count);
		
		if(_isDebug) {
			logger("Key " + key + " is" + (isLimited ? " " : " not ") + "limited ");
		}

		return isLimited;
	}
	
	
	
	
	//----------------------------- Private --------------------------------------//
	
	private HashMap<String, Buckets> _keyedBuckets;
	
	
	
	
	
	
	/////////////////////////////// Test stuff /////////////////////////////////////
	
	public static void main(String[] args) throws InterruptedException {		
		long rateCount = 20;
		long ratePeriodSeconds = 5*60; // 5 minutes
		int numberOfBuckets = 5;
		boolean isDebug = true;

		RateLimiter limiter = new RateLimiterLocalStore(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug);
		
		String email1 = "bruceslawson@earthlink.net";
		String email2 = "slawsonb@gmail.com";		
		int maxCount = 3;
		int maxSleepMillis = 30000;
		
		Random rdn = new Random(System.currentTimeMillis());
		int count = 0;
		long sleepMillis = 0;
		for(int i = 0; i < 100; i++) {
			count = rdn.nextInt(maxCount) + 1;
			sleepMillis = rdn.nextInt(maxSleepMillis);
			System.out.println("isLimited(\"" + email1 + "\", " + count + "): " + limiter.isLimited(email1, count));
			sleep(sleepMillis);
			
			count = rdn.nextInt(maxCount) + 1;
			sleepMillis = rdn.nextInt(maxSleepMillis);
			System.out.println("isLimited(\"" + email2 + "\", " + count + "): " + limiter.isLimited(email2, count));
			sleep(sleepMillis);
		}
		
		System.out.println("\n\nDone!");
	}
	
	
	
	
	private static void sleep(long millis) throws InterruptedException {
		System.out.println("sleeping (ms): " + millis);
		Thread.currentThread().sleep(millis);
	}
	

}
