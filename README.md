An implementation of a rolling rate limiter that uses Couchbase or Memcahed as the storage mechanism.
 
A rolling rate limiter uses a moving window of time so the the limiting function is more accurate.  
For example, say you want to limit the number of emails a user can send in an hour of time.  You wish to
do this so that the load on the mail server remains constant as possible by minimizing spikes.

Without a moving window of time a rate limiter could have sudden spikes of email sends at the beginning of each 
new hour with the limiter cutting them off until the start of the next new hour.  With a rolling limiter the 
sends are more smoothly distributed as the window of time moves. Previous activity is not forgotten at
the beginning of each new hour.  

This implementation allows specification of the limit, window size, and the granularity of time slices in that
window. The higher the number of time slices the smoother the rate limiting function.  A balance in smoothness
versus time/size efficiency needs to be struck.

If no Memcache/Couchbase servers are specified it will check locally (127.0.0.1) on port 11211.

To start memcache, with debug info, on default port 11211:

```
		shell> ./memcached -vv
```

Depends on the spymemcached client library which is included in the lib directory.

API docs are in the docs folder.




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

