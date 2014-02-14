package com.bruceslawson.ratelimiter.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;

import com.bruceslawson.ratelimiter.RateLimiter;
import com.bruceslawson.ratelimiter.RateLimiterMemcache;
import com.bruceslawson.ratelimiter.SliceNamer;

/**
 * @author Bruce Slawson &lt;bruce@bruceslawson.com&gt;
 *
 */
public class Tester {

	public static void main(String[] args) throws IOException {	
		long rateCount = 20;
		long ratePeriodSeconds = 5*60; // 5 minutes
		int numberOfBuckets = 5;
		boolean isDebug = true;
		InetSocketAddress[] memcahdServers = {new InetSocketAddress("127.0.0.1", 11211)};

		
		RateLimiterMemcache limiter = null;
		try {
			limiter = new RateLimiterMemcache(rateCount, ratePeriodSeconds, numberOfBuckets, isDebug, memcahdServers);
			
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
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			try {
				limiter.close();
			} catch (Exception e) {
				// ignore
			}
		}
		
	}

	
	
	
	private static void sleep(long millis) {
		
		System.out.println("sleeping: " + millis);
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}


	
	

	
	
}
