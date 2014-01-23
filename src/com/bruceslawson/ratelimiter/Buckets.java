package com.bruceslawson.ratelimiter;

public class Buckets {
	
	public Buckets(long rateCount, long ratePeriodSeconds, int numberOfBuckets) {
		this(rateCount, ratePeriodSeconds, numberOfBuckets, false);
	}
	
	public Buckets(long rateCount, long ratePeriodSeconds, int numberOfBuckets, boolean isDebug) {		
		_numberOfBuckets = numberOfBuckets;
		_bucketSizeSeconds = ratePeriodSeconds/numberOfBuckets;
		_rateCount = rateCount;
		_isDebug = isDebug;
		_bucket = new long[_numberOfBuckets];
		
		// Zero out buckets
		for(int i = 0; i < _numberOfBuckets; i++) {
			_bucket[i] = 0;
		}

		if(_isDebug) {
			logger("Rate Count: " + _rateCount);
			logger("Rate Period (seconds): " + ratePeriodSeconds);
			logger("Number Of Buckets: " + _numberOfBuckets);
			logger("Bucket Size (seconds): " + _bucketSizeSeconds);
		}
	}
	
	
	public void incrementCount() {
		incrementCount(1);
	}
	
	public void incrementCount(int count) {
		if(_currentBucket < 0) {
			_currentBucket = 0;
			_currentBucketTimeMillis = System.currentTimeMillis();
		} else {
			long nowMillis = System.currentTimeMillis();
			
			// Check to see if we need to move to next bucket
			long numberOfBucketsToSkip = (nowMillis - _currentBucketTimeMillis) / (_bucketSizeSeconds * 1000);
			if(numberOfBucketsToSkip > 0) {
				
				if(_isDebug) {
					logger("Time for a new bucket");
					logger("Skipping " + numberOfBucketsToSkip + " buckets.");
				}
				
				// If at last bucket, shift to the left
				if((_currentBucket + numberOfBucketsToSkip) >= _numberOfBuckets ) {
					if(_isDebug) {
						logger("Shifting buckets");
					}
					
					// Make new buckets 
					long[] tempBucket = new long[_numberOfBuckets];
					for(int i = 0; i < _numberOfBuckets; i++) {
						tempBucket[i] = 0;
					}
					
					// If we need to copy anything
					if(numberOfBucketsToSkip < _numberOfBuckets) {
						// Do the copy
						int startBucketIndex = (int)(_currentBucket + numberOfBucketsToSkip - _numberOfBuckets) + 1;
						for(int i = startBucketIndex, j = 0; i < _numberOfBuckets; i++, j++) {
							tempBucket[j] = _bucket[i];
						}						
					}
					
					// swap in new buckets
					_bucket = tempBucket;
					
					// Set to last bucket
					_currentBucket = _numberOfBuckets - 1;
				} else {
					// No shift needed.  Just increment.
					_currentBucket += numberOfBucketsToSkip;
				}
				
				_currentBucketTimeMillis += (_bucketSizeSeconds * 1000) * numberOfBucketsToSkip;
			}
		}
		
		if(_isDebug) {
			logger("Incrementing Bucket[" + _currentBucket + "] by  " + count);
		}
		
		// increment bucket
		_bucket[_currentBucket] += count;
	}
	
	
	
	public boolean isLimited() {
		return isLimited(1);
	}
	
	public boolean isLimited(int count) {
		return isLimited(true, count);
	}
	
	public boolean isLimited(boolean isUpdateCount) {
		return isLimited(isUpdateCount, 1);
	}

	public boolean isLimited(boolean isUpdateCount, int count) {
		
		if(isUpdateCount) {
			incrementCount(count);
		}	
		
		long bucketSum = 0;
		for(int i = 0; i < _numberOfBuckets; i++) {
			if(_isDebug) {
				logger("Bucket[" + i + "]: " + _bucket[i]);
			}
			bucketSum += _bucket[i];
		}
		
		if(_isDebug) {
			logger("Bucket Sum: " + bucketSum);
		}
		
		boolean isOverCount = false;
		if(bucketSum > _rateCount) {
			isOverCount = true;
		}
		
		return isOverCount;
	}
	
	
	public void setIsDebug(boolean isDebug) {
		_isDebug = isDebug;
	}

	public boolean isDebug() {
		return _isDebug;
	}	

	
	
	
	
	//----------------------------------- Private ------------------------------------//
	
	private int _numberOfBuckets;
	private long _bucketSizeSeconds;
	private long _rateCount;
	private long[] _bucket;	
	private long _currentBucketTimeMillis = -1;
	private int _currentBucket = -1;
	private boolean _isDebug;

	
	private void logger(String message) {
		System.out.println( this.getClass().getName() + "> " + message);
	}

	
	
	
	
	
	
	
	
	
	/////////////////////////////// Test stuff /////////////////////////////////////
	
	public static void main(String[] args) throws InterruptedException {
		long rateCount = 6;
		long ratePeriodSeconds = 100;
		int numberOfBuckets = 10;
		boolean isDebug = true;
		
		Buckets buckets = new Buckets(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug);
		
		for(int i = 0; i < 5; i++) {
			System.out.println("isLimited: " + buckets.isLimited());
			sleep(1000);
			System.out.println("isLimited: " + buckets.isLimited());
			sleep(2000);
			System.out.println("isLimited: " + buckets.isLimited());
			System.out.println("isLimited: " + buckets.isLimited(false));
			sleep(5000);
			System.out.println("isLimited: " + buckets.isLimited());
			sleep(10000);
			System.out.println("isLimited: " + buckets.isLimited(false));
			System.out.println("isLimited: " + buckets.isLimited());
			sleep(20000);
			System.out.println("isLimited: " + buckets.isLimited());
			sleep(60000);
			System.out.println("isLimited: " + buckets.isLimited());
		}
		
		System.out.println("\n\nDone!");
	}
	
	
	
	private static void sleep(long millis) throws InterruptedException {
		System.out.println("sleeping: " + millis);
		Thread.currentThread().sleep(millis);
	}
	
	
}
