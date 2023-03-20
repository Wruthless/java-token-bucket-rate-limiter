package org.wruthess.ratelimiter;

public class SimpleTokenBucketRateLimiter implements RateLimiter {

    private static final long NANO_PER_SECOND = 1000000000;
    private final int mTPS;
    private long mLastExecutionNanos;
    private long mNextSecondBoundary;
    private long mCounter;

    private final Object mLock = new Object();
    private final Object mBoundaryLock = new Object();

    public SimpleTokenBucketRateLimiter(int rate) {
        this.mTPS = rate;
        this.mCounter = 0;
        this.mLastExecutionNanos = 0L;
        this.mNextSecondBoundary = 0L;
    }

    @Override
    public boolean throttle(Code code) {

        if (mTPS <= 0) {
            return false;
        }

        synchronized (mLock) {
            // Add one second to the moment when the first transaction is done.
            if(mLastExecutionNanos == 0L) {
                mCounter++; // Allocate first token.
                mLastExecutionNanos = System.nanoTime();
                mNextSecondBoundary = mLastExecutionNanos + NANO_PER_SECOND; // (10^9).
                invoke(code);
                return true;
            } else {

                // Until (tn + 1)s, N transactions are allowed. At the next transaction, check if
                // current time <= (tn + 1). If not, we are in another second and are once
                // again allowed to make N transactions.
                long now = System.nanoTime();
                if (now <= mNextSecondBoundary) {       // In time limit of current second.
                    if (mCounter < mTPS) {              // If a token is available.
                        mLastExecutionNanos = now;
                        mCounter++;                     // Allocate token.
                        invoke(code);                   // Invoke code passed to throttle().
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    // Reset the counter in different second
                    mCounter = 0;
                    mLastExecutionNanos = 0L;
                    mNextSecondBoundary = 0L;
                    return throttle(code);
                }
            }
        }
    }

    @Override
    public boolean acquire() {
        if(mTPS == 0L) {
            return false;
        }

        synchronized (mBoundaryLock) {
            if(mLastExecutionNanos == 0L) {
                mLastExecutionNanos = System.nanoTime();
                mCounter++;
                mNextSecondBoundary = mLastExecutionNanos + NANO_PER_SECOND;
                return true;
            } else {
                long now = System.nanoTime();
                if (now <= mNextSecondBoundary) {
                    if(mCounter < mTPS) {
                        mLastExecutionNanos = now;
                        mCounter++;
                        return true;
                    } else return false;
                } else {
                    // Reset counter in different second
                    mCounter = 0;
                    mLastExecutionNanos = 0L;
                    mNextSecondBoundary = 0L;
                    return acquire();
                }
            }
        }
    }

    @Override
    public boolean acquire(int permits) {
        throw new NoImplementationException();
    }

    private void invoke(Code code) {
        try {
            code.invoke();
        } catch (Throwable th) {
            System.out.println(th);
        }
    }

}
