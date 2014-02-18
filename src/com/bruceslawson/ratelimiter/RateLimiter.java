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
		
		_timeSliceNamer = new TimeSliceNamer(ratePeriodSeconds, numberOfSlices, isDebug);
		
		if(_isDebug) {
			DebugPrinter.print(RateLimiter.class, "Rate Limit: " + _rateLimit);
			DebugPrinter.print(RateLimiter.class, "Rate Period (ms): " + _ratePeriodMillis);
			DebugPrinter.print(RateLimiter.class, "Number Of Slices: " + _numberOfSlices);
			DebugPrinter.print(RateLimiter.class, "Slice Size (ms): " + _sliceSizeMillis);
		}
	}

	
	/**
	 * Will increment the key count by 1.
	 * 
	 * @param limterKey The unique key
	 * 
	 */
	public void incrementCount(String limterKey) {
		incrementCount(limterKey, 1);
	}
	
	
	/**
	 * Will increment the key count by count.
	 * 
	 * @param limiterKey The unique key
	 * @param count The amount to increment by
	 */
	public abstract void incrementCount(String limiterKey, int count);
	

	/**
	 * Checks to see if key is over limit.
	 * 
	 * @param limiterKey  The unique key
	 * @return Is the key over the limit?
	 */
	public boolean isLimited(String limiterKey) {
		return isLimited(limiterKey, 0);
	}	
	
	
	/**
	 * Checks to see if key is over limit.  Increments the key by count
	 * before doing the check. Zero count means no increment.
	 * 
	 * @param limiterKey  The unique key
	 * @param count The amount to increment by
	 * @return Is the key over the limit?
	 */
	public abstract boolean isLimited(String limiterKey, int count);
	
	

	/**
	 * Shuts down the rate limiter.
	 */
	public void close() {
		// nothing needed.
	}
	

	
	//--------------------------------- Protected -------------------------------------------------//

	/**
	 * Returns all the current slice names.
	 * 
	 * @param limiterKey
	 * @return
	 */
	protected String[] getSliceNames(String limiterKey) {
		
		String[] names = _timeSliceNamer.getAllSliceNames();
		for(int i = 0; i < names.length; i++) {
			names[i] = getSliceName(limiterKey, names[i]);
		}
		
		
		return names;
	}
	
	/**
	 * Creates the full key name based on the limiterKey and the sliceName
	 * @param limiterKey
	 * @param sliceName
	 * @return
	 */
	protected static String getSliceName(String limiterKey, String sliceName) {
		return limiterKey + "~-~" + sliceName;
	}
	
	/**
	 * Gets the full slice name for the current time
	 * 
	 * @param limiterKey
	 * @return
	 */
	protected String getCurrentSliceName(String limiterKey) {
		return getSliceName(limiterKey, _timeSliceNamer.getCurrentSliceName());
	}

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
	

	protected TimeSliceNamer getSliceNamer() {
		return _timeSliceNamer;
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
	

	
	
	//--------------------------------- Private -------------------------------------------------//
	
	private int 		_numberOfSlices;
	private long 		_sliceSizeMillis;
	private long 		_rateLimit;
	private long 		_ratePeriodMillis;
	private boolean 	_isDebug;
	
	private TimeSliceNamer	_timeSliceNamer;

}
