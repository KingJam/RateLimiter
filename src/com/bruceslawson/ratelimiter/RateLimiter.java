package com.bruceslawson.ratelimiter;


/**
 * Base abstract class for all rate limiters. Provides basic functionality
 * for implementing  
 * 
 * 
 * @author Bruce Slawson &lt;bruce@bruceslawson.com&gt;
 *
 */
public abstract class RateLimiter {

		
	/**
	 * Construct the rate limiter.
	 * 
	 * @param rateLimit The count limit allowed in ratePeriodSeconds time.
	 * @param ratePeriodSeconds The rolling period of time.
	 * @param numberOfSlices The granularity of time within ratePeriodSeconds.
	 * @param isDebug Set debugging messages on or off.
	 * 
	 */
	public RateLimiter(long rateLimit, long ratePeriodSeconds, int numberOfSlices, boolean isDebug) {
		_rateLimit = rateLimit;
		_ratePeriodMillis = ratePeriodSeconds * 1000;
		_numberOfSlices = numberOfSlices;
		_sliceSizeMillis = (ratePeriodSeconds/numberOfSlices) * 1000;
		_isDebug = isDebug;
		
		_sliceNamer = new SliceNamer(ratePeriodSeconds, numberOfSlices, isDebug);
		
		if(_isDebug) {
			logger("Rate Limit: " + _rateLimit);
			logger("Rate Period (ms): " + _ratePeriodMillis);
			logger("Number Of Slices: " + _numberOfSlices);
			logger("Slice Size (ms): " + _sliceSizeMillis);
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
	 * Checks to see if key is over limit.
	 * 
	 * @param key  The unique key
	 * @return Is the key over the limit?
	 */
	public boolean isLimited(String key) {
		return isLimited(key, 0);
	}	
	
	
	/**
	 * Checks to see if key is over limit.  Increments the key by count
	 * before doing the check. Zero count means no increment.
	 * 
	 * @param key  The unique key
	 * @param count The amount to increment by
	 * @return Is the key over the limit?
	 */
	public abstract boolean isLimited(String key, int count);

	
	// For current slice
	protected String getSliceKey(String catagoryName) {
		return catagoryName + "-" + getSliceNamer().getCurrentSliceName(); 
	}
	
	
//	/**
//	 * Shuts down the rate limiter.
//	 */
//	public void shutdown() {
//		// Nothing here
//	}
//	
//	/**
//	 * Removes unused slice from the data store.
//	 */
//	public void cleanupExpiredSlices() {
//		// Nothing here
//	}
	
	
	//--------------------------------- Protected -------------------------------------------------//
	

	/**
	 * @return The number of time slices
	 */
	protected int getNumberOfSlices() {
		return _numberOfSlices;
	}


	/**
	 * @return The size of the time slices in milliseconds
	 */
	protected long getSliceSizeMillis() {
		return _sliceSizeMillis;
	}


	/**
	 * @return The maximum count allowed
	 */
	protected long getRateLimit() {
		return _rateLimit;
	}
	

	/**
	 * @return The moving window of time in milliseconds
	 */
	protected long getRatePeriodMillis() {
		return _ratePeriodMillis;
	}
	

	protected SliceNamer getSliceNamer() {
		return _sliceNamer;
	}
	
	/**
	 * Is debugging on or off?
	 * 
	 * @return
	 */
	protected boolean isDebug() {
		return _isDebug;
	}		
	
	
	/**
	 * Turn debugging on or off.
	 * 
	 * @param isDebug 
	 */
	protected void setIsDebug(boolean isDebug) {
		_isDebug = isDebug;
	}	
	
	
	/**
	 * A very basic logger that prints to stdout
	 * 
	 * @param message The message to print
	 */
	protected void logger(String message) {
		System.out.println( this.getClass().getName() + "> " + message);
	}
	
	
	
	
	//--------------------------------- Private -------------------------------------------------//
	
	private int 		_numberOfSlices;
	private long 		_sliceSizeMillis;
	private long 		_rateLimit;
	private long 		_ratePeriodMillis;
	private boolean 	_isDebug;
	
	private SliceNamer	_sliceNamer;

}
