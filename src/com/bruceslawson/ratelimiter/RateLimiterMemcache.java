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
 * 		<code>RateLimiter limiter = new RateLimiterMemcache(6, 5*60, 5, false);<br>
 * 		boolean isThisKeyLimited = limiter.isLimited("username@domain.com");</code>
 *
 * @author Bruce Slawson &lt;bruce@bruceslawson.com&gt;
 */
public class RateLimiterMemcache extends RateLimiter {	
	private static final String DEFAULT_MEMCACHE_HOST = "localhost";
	private static final int DEFAULT_MEMCACHE_PORT = 11211;
		
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
				DebugPrinter.print(this.getClass(), "No memcached server provided. Using default " + DEFAULT_MEMCACHE_HOST + ":" + DEFAULT_MEMCACHE_PORT);
			}
	    	_mcdClient = new MemcachedClient(new InetSocketAddress(DEFAULT_MEMCACHE_HOST, DEFAULT_MEMCACHE_PORT));
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
				DebugPrinter.print(this.getClass(), "Slice " + keys[i] + " for key " + key + ": " + sliceCountString);
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
			DebugPrinter.print(this.getClass(), "Slice sum for key " + key + ": " + slicesSum);
			DebugPrinter.print(this.getClass(), "For key " + key + ". Is over limit?: " + isOverLimit);
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
	

}



