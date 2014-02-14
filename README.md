An implementation of a rolling rate limiter that uses Couchbase or Memcahed as the storage mechanism.
If no Memcached servers are specified it will check locally (127.0.0.1) on port 11211.

To start memcache, with debug info, on default port 11211:

```
		shell> ./memcached -vv
```

Using the rate limiter:

```java
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
```

