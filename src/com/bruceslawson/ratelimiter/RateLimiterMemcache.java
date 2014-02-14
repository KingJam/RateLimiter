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
 * 		<code>RateLimiter limiter = new RateLimiterMemcached(6, 5*60, 5, false);<br>
 * 		boolean isThisKeyLimited = limiter.isLimited("username@domain.com");</code>
 *
 * @author Bruce Slawson &lt;bruce@bruceslawson.com&gt;
 */
public class RateLimiterMemcache extends RateLimiter {	

		
	/**
	 * Construct the rate limiter.
	 * 
	 * @param rateLimit The counts limit allowed in ratePeriodSeconds time.
	 * @param ratePeriodSeconds The rolling period of time to check count.
	 * @param numberOfSlices The granularity of time within ratePeriodSeconds.
	 * @param isDebug Set debugging on or off.
	 * @param memcachedServers The memcached servers
	 * 
	 * @throws IOException
	 */
	public RateLimiterMemcache(long rateLimit, long ratePeriodSeconds, int numberOfSlices, boolean isDebug, InetSocketAddress ... memcachedServers) throws IOException {		
		super(rateLimit, ratePeriodSeconds, numberOfSlices, isDebug);
		
		// Setup the memcache connections
	    if(memcachedServers == null || memcachedServers.length < 1) {
			if(isDebug()) {
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
		_mcdClient.incr(getSliceKey(key), count, count, getMemCacheSliceExpiration());
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
	public boolean isLimited(String key, int count) {
		
		if(count != 0) {
			incrementCount(key, count);
		}
		
		// Get all the keys
		String[] keys = getSliceNamer().getAllSliceNames();
		for(int i = 0; i < keys.length; i++) {
			keys[i] = getMemCacheKey(key, keys[i]);;
		}
		
		// Get all the slices at once
		Map<String, Object> slices = _mcdClient.getBulk(keys);
		
		// Sum the slices
		Long slicesSum = 0L;
		for(int i = 0; i < keys.length; i++) {
			String sliceCountString = (String)slices.get(keys[i]);
			
			if(isDebug()) {
				logger("Slice " + keys[i] + " for key " + key + ": " + sliceCountString);
			}
			
			if(sliceCountString != null) {
				slicesSum += Long.parseLong(sliceCountString);
			}
		}
		
		// Are we over the limit?
		boolean isOverLimit = false;
		if(slicesSum > getRateLimit()) {
			isOverLimit = true;
		}
		
		if(isDebug()) {
			logger("Slice sum for key " + key + ": " + slicesSum);
			logger("For key " + key + ". Is over limit?: " + isOverLimit);
		}
		
		return isOverLimit;
	}
	
	
	/**
	 * Shuts down the memcached client connection.
	 */
	public void close() {
		_mcdClient.shutdown();
	}

	
	
	
	//--------------------------------- Private -------------------------------------------------//
	
	private static final int SLICE_EXPIRATION_PADDING_SECONDS = 600;  // 5 minutes
	private MemcachedClient _mcdClient;

	
	// For given slice
	private String getMemCacheKey(String key, String sliceKey) {
		return key + "-" + sliceKey;
	}

	
	private int getMemCacheSliceExpiration() {
		// Add padding to rate period
		return (int)((getRatePeriodMillis()/1000) + SLICE_EXPIRATION_PADDING_SECONDS);
	}
		
	
	
	
	
	
	
	/////////////////////////////////// test stuff ////////////////////////////////////////////////
	

	public static void main(String[] args) throws IOException, InterruptedException {	
		long rateCount = 20;
		long ratePeriodSeconds = 5*60; // 5 minutes
		int numberOfBuckets = 5;
		boolean isDebug = true;
		InetSocketAddress[] memcahdServers = {new InetSocketAddress("127.0.0.1", 11211)};

		RateLimiterMemcache limiter = new RateLimiterMemcache(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug, memcahdServers);
		
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
		limiter.close();
	}

	
	private static void sleep(long millis) throws InterruptedException {
		System.out.println("sleeping (ms): " + millis);
		Thread.sleep(millis);
	}

}



