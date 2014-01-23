package com.bruceslawson.ratelimiter;

public class BucketKeys {
	

	protected BucketKeys(long rateCount, long ratePeriodSeconds, int numberOfBuckets) {
		this(rateCount, ratePeriodSeconds, numberOfBuckets, false);
	}
	
	protected BucketKeys(long rateCount, long ratePeriodSeconds, int numberOfBuckets, boolean isDebug) {
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
	
	
	protected String getCurrentBucketKey() {
		long now = System.currentTimeMillis();
		long currentTime = now / _ratePeriodMillis;
		long currentBucket = (now / _bucketSizeMillis) % _numberOfBuckets;
		String key = currentTime + "_" + currentBucket;

		if(_isDebug) {
			logger("Current bucket key: " + key);
		}
				
		return key;
	}
	
	
	protected String[] getAllBucketKeys() {
		long now = System.currentTimeMillis();
		long bucketTime = now / _ratePeriodMillis;
		long bucketNumber = (now / _bucketSizeMillis) % _numberOfBuckets;
		String[] keys = new String[_numberOfBuckets];		
		String allKeysString = "";
		
		for(int i = 0; i < _numberOfBuckets; i++) {
			keys[i] = bucketTime + "_" + bucketNumber;
			
			if(_isDebug) {
				allKeysString += keys[i];
				if(i < (_numberOfBuckets - 1)) {
					allKeysString += ", ";
				}
			}
			
			if(bucketNumber == 0) {
				bucketNumber = _numberOfBuckets - 1;
				bucketTime--;
			} else {
				bucketNumber--;
			}
		}
		
		if(_isDebug) {
			logger("All bucket keys: " + allKeysString);
		}
		
		return keys;
	}
	
	
	
	
	//-------------------------------- Private ----------------------------------------//
	
	private int _numberOfBuckets;
	private long _bucketSizeMillis;
	private long _rateCount;
	private long _ratePeriodMillis;
	private boolean _isDebug;

	
	private void logger(String message) {
		System.out.println( this.getClass().getName() + "> " + message);
	}

	
	
	
	
	
	
	
	///////////////////////////// test stuff /////////////////////////////////////////
	
	
	public static void main(String[] args) throws InterruptedException {
		BucketKeys limiter = new BucketKeys(6, 5*60, 5, true);
		
		for(int i = 0; i < 100; i++) {
			limiter.getCurrentBucketKey();
			limiter.getAllBucketKeys();
			sleep(7321);
		}
		
		

		
	}
	
	private static void sleep(long millis) throws InterruptedException {
		System.out.println("sleeping: " + millis);
		Thread.currentThread().sleep(millis);
	}

}
