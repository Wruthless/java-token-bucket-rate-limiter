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
            if(mLastExecutionNanos == 0L) {
                mCounter++;
                mLastExecutionNanos = System.nanoTime();
                mNextSecondBoundary = mLastExecutionNanos + NANO_PER_SECOND;
                invoke(code);
                return true;
            } else {
                long now = System.nanoTime();
                if (now <= mNextSecondBoundary) {
                    if (mCounter < mTPS) {
                        mLastExecutionNanos = now;
                        mCounter++;
                        invoke(code);
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
