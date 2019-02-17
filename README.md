# Rate Limiter

This is a utility to rate limit the api.

## Usage

#### Parameters

**key:** identifier for the limiter you applied for <br/>
**capacity:** number of request <br/>
**interval:** time interval in second to allow the specified number of request <br/>

```java
RateLimiter rateLimiter = RateLimiter.create("myKey", 100, 3600);
rateLimiter.fill("myKey");

if (rateLimiter.getRequestBucket("myKey").getIsOverflow())
{
   // do something if limit reached.
}
```
