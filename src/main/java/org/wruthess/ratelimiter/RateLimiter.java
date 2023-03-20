package org.wruthess.ratelimiter;

/*
    A rate limiter supports limiting the rate of execution of a piece of code.
    The rate is defined in terms of TPS(Transactions per second).
    A rate of 5 would suggest five transactions/second. The transactions
    could be DB, API, or simple function calls.

    In the case of a piece of code needing rate limiting, it could be
    a function call. If the code to be rate limited spreads across
    multiple functions, we need to use the acquire method.
 */
public interface RateLimiter {

    /**
     * Rate limit any code passed.
     *
     * @param code representation of the code needing rate limiting
     * @return true if executed, false otherwise.
     */
    boolean throttle(Code code);

    /**
     * If the rate limited code cannot be contiguous, this method
     * should be used before executing the code. Can be used to pass
     * a block of code if we have contiguous code.
     *
     * @return true if the code will execute, false otherwise.
     */
    boolean acquire();

    /**
     * Allows multiple permits at the same time. Watch out for expensive permits.
     * Can be used in general before the API, DB, or any call needing throttling.
     * If the code following this would execute, it would return true,
     * and false if it is rate limited. These request may be queued or rejected.
     *
     * @param permits Permits required.
     * @return true on success, false otherwise.
     */
    boolean acquire(int permits);

    // Interface to represent a contiguous piece of code that needs to be rate limited.
    interface Code {
        // Execute the code delegated to this interface.
        void invoke();
    }

}
