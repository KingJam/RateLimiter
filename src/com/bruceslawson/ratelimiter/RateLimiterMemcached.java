package com.bruceslawson.ratelimiter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;

import net.spy.memcached.MemcachedClient;


/**
 * Basic implementation of a rolling rate limiter that uses memcahed as the storage mechanism.
 * If no memcached servers are specified it will check locally (127.0.0.1) on port 11211.  I 
 * still need to make memcahed connections more configurable and more robust. No effort
 * has been made, as of yet, to make this class thread safe.
 * <p>
 * To start memcached with debug info and on default port 11211:<br><br>
 * 		<code>shell> ./memcached -vv</code>
 * <p>
 * Example usage:<br><br>
 * 		<code>RateLimiter limiter = new RateLimiterMemcached(6, 5*60, 5);<br>
 * 		boolean isThisKeyLimited = limiter.isLimited("username@domain.com");</code>
 *
 * @author Bruce Slawson &lt;bruce.slawson@gmail.com&gt;
 */
public class RateLimiterMemcached extends RateLimiter {	
	
	/**
	 * Construct the rate limiter.
	 * 
	 * @param rateCount The counts limit allowed in ratePeriodSeconds time.
	 * @param ratePeriodSeconds The rolling period of time to check count.
	 * @param numberOfBuckets The granularity of time within ratePeriodSeconds.
	 * 
	 * @throws IOException
	 */
	public RateLimiterMemcached(long rateCount, long ratePeriodSeconds, int numberOfBuckets) throws IOException {
		this(rateCount, ratePeriodSeconds, numberOfBuckets, false);
	}
	

	/**
	 * Construct the rate limiter.
	 * 
	 * @param rateCount The counts limit allowed in ratePeriodSeconds time.
	 * @param ratePeriodSeconds The rolling period of time to check count.
	 * @param numberOfBuckets The granularity of time within ratePeriodSeconds.
	 * @param memcachedServers The memcached servers.
	 * 
	 * @throws IOException
	 */
	public RateLimiterMemcached(long rateCount, long ratePeriodSeconds, int numberOfBuckets, InetSocketAddress ... memcachedServers) throws IOException {
		this(rateCount, ratePeriodSeconds, numberOfBuckets, false, memcachedServers);
	}
	
	
	/**
	 * Construct the rate limiter.
	 * 
	 * @param rateCount The counts limit allowed in ratePeriodSeconds time.
	 * @param ratePeriodSeconds The rolling period of time to check count.
	 * @param numberOfBuckets The granularity of time within ratePeriodSeconds.
	 * @param isDebug Set debugging on or off.
	 * 
	 * @throws IOException
	 */
	public RateLimiterMemcached(long rateCount, long ratePeriodSeconds, int numberOfBuckets, boolean isDebug) throws IOException {		
		this(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug, null);
	}
	
	
	/**
	 * Construct the rate limiter.
	 * 
	 * @param rateCount The counts limit allowed in ratePeriodSeconds time.
	 * @param ratePeriodSeconds The rolling period of time to check count.
	 * @param numberOfBuckets The granularity of time within ratePeriodSeconds.
	 * @param isDebug Set debugging on or off.
	 * @param memcachedServers The memcached servers
	 * 
	 * @throws IOException
	 */
	public RateLimiterMemcached(long rateCount, long ratePeriodSeconds, int numberOfBuckets, boolean isDebug, InetSocketAddress ... memcachedServers) throws IOException {		
		super(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug);
		
		// Get the bucket key generator
		_keyedBuckets = new BucketKeys(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug);
		
		// Setup the memcached connections
	    if(memcachedServers == null || memcachedServers.length < 1) {
			if(_isDebug) {
				logger("No memcached server provided. Using default 127.0.0.1:11211");
			}
	    	_mcdClient = new MemcachedClient(new InetSocketAddress("127.0.0.1", 11211));
	    } else {
	    	_mcdClient = new MemcachedClient(memcachedServers);
	    }
	}
	
	
	/**
	 * Will increment the key count by count.
	 * 
	 * @param key The unique key
	 * @param count The amount to increment by
	 */
	public void incrementCount(String key, int count) {
		_mcdClient.incr(getMemCacheKey(key), count, count, getMemCacheBucketExpiration());
	}
	
	
	/**
	 * Checks to see if key is over limit.  Increments the key by count, if
	 * isUpdateCount is true, before doing the check.
	 * 
	 * @param key The unique key.
	 * @param isUpdateCount Should count be updated?
	 * @param count The amount to increment by.
	 * @return Is the key over the limit?
	 */
	public boolean isLimited(String key, boolean isUpdateCount, int count) {
		
		if(isUpdateCount) {
			incrementCount(key, count);
		}
		
		// Get all the keys
		String[] keys = _keyedBuckets.getAllBucketKeys();
		for(int i = 0; i < keys.length; i++) {
			keys[i] = getMemCacheKey(key, keys[i]);;
		}
		
		// Get all the buckets at once
		Map buckets = _mcdClient.getBulk(keys);
		
		// Sum the buckets
		Long bucketSum = 0L;
		for(int i = 0; i < keys.length; i++) {
			String bucketCountString = (String)buckets.get(keys[i]);
			
			if(_isDebug) {
				logger("Bucket " + keys[i] + " for key " + key + ": " + bucketCountString);
			}
			
			if(bucketCountString != null) {
				bucketSum += Long.parseLong(bucketCountString);
			}
		}
		
		// Are we over the limit?
		boolean isOverCount = false;
		if(bucketSum > _rateCount) {
			isOverCount = true;
		}
		
		if(_isDebug) {
			logger("Bucket sum for key " + key + ": " + bucketSum);
			logger("For key " + key + ". Is over limit?: " + isOverCount);
		}
		
		return isOverCount;
	}
	
	
	/**
	 * Shuts down the memcached client connection.
	 */
	public void shutdown() {
		_mcdClient.shutdown();
	}

	
	
	
	//--------------------------------- Private -------------------------------------------------//
	
	private static final int BUCKET_EXPIRATION_PADDING_SECONDS = 600;  // 5 minutes
	private BucketKeys _keyedBuckets;
	private MemcachedClient _mcdClient;

	
	// For current bucket
	private String getMemCacheKey(String key) {
		return key + "-" + _keyedBuckets.getCurrentBucketKey(); 
	}
	
	
	// For given bucket
	private String getMemCacheKey(String key, String bucketKey) {
		return key + "-" + bucketKey;
	}

	
	private int getMemCacheBucketExpiration() {
		// Add padding to rate period
		return (int)((_ratePeriodMillis/1000) + BUCKET_EXPIRATION_PADDING_SECONDS);
	}
		
	
	
	
	/////////////////////////////////// test stuff ////////////////////////////////////////////////
	

	public static void main(String[] args) throws IOException, InterruptedException {	
		long rateCount = 20;
		long ratePeriodSeconds = 5*60; // 5 minutes
		int numberOfBuckets = 5;
		boolean isDebug = true;
		InetSocketAddress[] memcahdServers = {new InetSocketAddress("127.0.0.1", 11211)};

		RateLimiter limiter = new RateLimiterMemcached(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug, memcahdServers);
		
		String email1 = "bruce.slawson@gmail.com";
		String email2 = "slawsonb@gmail.com";		
		int maxCount = 3;
		int maxSleepMillis = 30000;
		
		Random rdn = new Random(System.currentTimeMillis());
		int count = 0;
		long sleepMillis = 0;
		for(int i = 0; i < 10; i++) {
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
		limiter.shutdown();
	}

	
	private static void sleep(long millis) throws InterruptedException {
		System.out.println("sleeping (ms): " + millis);
		Thread.currentThread().sleep(millis);
	}

}



