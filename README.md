An implementation of a rolling rate limiter that uses Couchbase or Memcahed as the storage mechanism.
If no Memcached servers are specified it will check locally (127.0.0.1) on port 11211.

To start emcached with debug info and on default port 11211:

```
		shell> ./memcached -vv
```

Using the rate limiter:

```java
		String limiterKey = "BRUCES_LOGIN_ATTEMPTS";

		RateLimiter limiter = new RateLimiterMemcached(6, 5*60, 5, false);
		
		boolean isThisKeyLimited = limiter.isLimited(limiterKey);
		
		limiter.incrementCount(limiterKey);
		
		isThisKeyLimited = limiter.isLimited(limiterKey);
```

