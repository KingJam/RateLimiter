package com.bruceslawson.ratelimiter;

/**
 * Generates names based on time and number of slices.
 * 
 * 
 * @author Bruce Slawson &lt;bruce@bruceslawson.com&gt;
 *
 */
public class TimeSliceNamer {
	
	
	protected TimeSliceNamer(long ratePeriodSeconds, int numberOfSlices, boolean isDebug) {
		_ratePeriodMillis = ratePeriodSeconds * 1000;
		_numberOfSlices = numberOfSlices;
		_sliceSizeMillis = (ratePeriodSeconds/numberOfSlices) * 1000;
		_isDebug = isDebug;
	}
	
	
	protected String getCurrentSliceName() {
		long now = System.currentTimeMillis();
		long currentTime = now / _ratePeriodMillis;
		long currentBucket = (now / _sliceSizeMillis) % _numberOfSlices;
		String key = currentTime + "_" + currentBucket;

		if(_isDebug) {
			DebugPrinter.print(TimeSliceNamer.class, "Current slice key: " + key);
		}
				
		return key;
	}
	
	
	protected String[] getAllSliceNames() {
		long now = System.currentTimeMillis();
		long sliceTime = now / _ratePeriodMillis;
		long sliceNumber = (now / _sliceSizeMillis) % _numberOfSlices;
		String[] names = new String[_numberOfSlices];		
		String allNamesString = "";
		
		for(int i = 0; i < _numberOfSlices; i++) {
			names[i] = sliceTime + "_" + sliceNumber;
			
			if(_isDebug) {
				allNamesString += names[i];
				if(i < (_numberOfSlices - 1)) {
					allNamesString += ", ";
				}
			}
			
			if(sliceNumber == 0) {
				sliceNumber = _numberOfSlices - 1;
				sliceTime--;
			} else {
				sliceNumber--;
			}
		}
		
		if(_isDebug) {
			DebugPrinter.print(TimeSliceNamer.class, "All slice names: " + allNamesString);
		}
		
		return names;
	}
	
	
	
	
	//-------------------------------- Private ----------------------------------------//
	
	private int _numberOfSlices;
	private long _sliceSizeMillis;
	private long _ratePeriodMillis;
	private boolean _isDebug;

}
