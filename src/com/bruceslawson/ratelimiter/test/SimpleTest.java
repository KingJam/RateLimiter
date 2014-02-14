package com.bruceslawson.ratelimiter.test;

import java.io.IOException;
import com.bruceslawson.ratelimiter.RateLimiter;
import com.bruceslawson.ratelimiter.RateLimiterMemcache;

public class SimpleTest {
	public static void main(String[] args) throws IOException {
		String limiterKey = "BrucesLoginAttempts";

		RateLimiter limiter = new RateLimiterMemcache(6, 5*60, 5, true);
		
		limiter.incrementCount(limiterKey);
		
		boolean isThisKeyLimited = limiter.isLimited(limiterKey);
		
		System.out.println("Is " + limiterKey + " limited?: " + isThisKeyLimited);
		
		limiter.close();
	}
}
