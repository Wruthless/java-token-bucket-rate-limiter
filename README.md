# Java Token Bucket Limiter

To build the core of a rate limiter, we must ensure that between any two seconds no more than
`N` transactions are allowed.

Consider the first transaction `t0`. Until `(t0 + 1)s`, we are allowed to make only N transactions.
To ensure this, at the time of the next transaction, check if `current time ≤ (t0 + 1)`. If not, we
have entered into a different second and able to make `N` transactions.

The following code section demonstrates:

```java
if (now <= mNextSecondBoundary) {       // In time limit of current second.
    if (mCounter < mTPS) {              // If a token is available.
        mLastExecutionNanos = now;
        mCounter++;                     // Allocate token.
        invoke(code);                   // Invoke code passed to throttle().
        return true;
    } else {
        return false;
    }
}
```

`mNextSecondBoundary` is defined previously. One second is added to the moment when the first transaction is complete.

```java
if(mLastExecutionNanos == 0L) {
    mCounter++; // Allocate first token.
    mLastExecutionNanos = System.nanoTime();
    mNextSecondBoundary = mLastExecutionNanos + NANO_PER_SECOND; // (10^9).
    invoke(code);
    return true;
}
```
