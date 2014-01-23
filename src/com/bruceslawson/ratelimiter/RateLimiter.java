package com.bruceslawson.ratelimiter;


public abstract class RateLimiter {
	
	/**
	 * Construct the rate limiter.
	 * 
	 * @param rateCount The counts limit allowed in ratePeriodSeconds time.
	 * @param ratePeriodSeconds The rolling period of time to check count.
	 * @param numberOfBuckets The granularity of time within ratePeriodSeconds.
	 * 
	 * @throws IOException
	 */
	public RateLimiter(long rateCount, long ratePeriodSeconds, int numberOfBuckets) {
		this(rateCount, ratePeriodSeconds, numberOfBuckets, false);
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
	public RateLimiter(long rateCount, long ratePeriodSeconds, int numberOfBuckets, boolean isDebug) {
		_rateCount = rateCount;
		_ratePeriodMillis = ratePeriodSeconds * 1000;
		_numberOfBuckets = numberOfBuckets;
		_bucketSizeMillis = (ratePeriodSeconds/numberOfBuckets) * 1000;
		_isDebug = isDebug;
		
		if(_isDebug) {
			logger("Rate Count: " + _rateCount);
			logger("Rate Period (ms): " + _ratePeriodMillis);
			logger("Number Of Buckets: " + _numberOfBuckets);
			logger("Bucket Size (ms): " + _bucketSizeMillis);
		}
	}

	
	/**
	 * Will increment the key count by 1.
	 * 
	 * @param key The unique key
	 * 
	 */
	public void incrementCount(String key) {
		incrementCount(key, 1);
	}
	
	
	/**
	 * Will increment the key count by count.
	 * 
	 * @param key The unique key
	 * @param count The amount to increment by
	 */
	public abstract void incrementCount(String key, int count);
	
	
	/**
	 * Checks to see if key is over limit.  Increments the key by 1
	 * before doing the check.
	 * 
	 * @param key  The unique key
	 * @return Is the key over the limit?
	 */
	public boolean isLimited(String key) {
		return isLimited(key, 1);
	}

	
	/**
	 * Checks to see if key is over limit.  Increments the key by count
	 * before doing the check.
	 * 
	 * @param key  The unique key
	 * @param count The amount to increment by
	 * @return Is the key over the limit?
	 */
	public boolean isLimited(String key, int count) {
		return isLimited(key, true, count);
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
	public abstract boolean isLimited(String key, boolean isUpdateCount, int count);
	
	
	/**
	 * Turn debugging on or off.
	 * 
	 * @param isDebug 
	 */
	public void setIsDebug(boolean isDebug) {
		_isDebug = isDebug;
	}

	
	/**
	 * Is debugging on or off?
	 * 
	 * @return
	 */
	public boolean isDebug() {
		return _isDebug;
	}	

	
	/**
	 * Shut down the rate limiter, if necessary.  This implementation
	 * does nothing.
	 */
	public void shutdown() {
		// do nothing
	}
	
	
	//--------------------------------- Protected -------------------------------------------------//
	
	protected int _numberOfBuckets;
	protected long _bucketSizeMillis;
	protected long _rateCount;
	protected long _ratePeriodMillis;
	protected boolean _isDebug;
	
	
	protected void logger(String message) {
		System.out.println( this.getClass().getName() + "> " + message);
	}

}
