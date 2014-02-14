package com.bruceslawson.ratelimiter.test;

import java.io.IOException;

import com.bruceslawson.ratelimiter.RateLimiterMemcache;

public class Tester {
	public static void main(String[] args) throws IOException {
		String emailAddress = "bruce@bruceslawson.com";
		String itemToLimit = "LOGINS";
		String limiterKey = emailAddress + "~" + itemToLimit;
		
		RateLimiterMemcache limiter = new RateLimiterMemcache(10, 60*60, 12, true);
		limiter.incrementCount(limiterKey);

		
		
	}

}
