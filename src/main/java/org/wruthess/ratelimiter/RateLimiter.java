package org.wruthess.ratelimiter;

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
     * should be used before executing the code.
     *
     * @return true if the code will execute, false otherwise.
     */
    boolean acquire();

    /**
     * Allows multiple permits at the same time. Watch out for expensive permits.
     * @param permits Permits required.
     * @return true on success, false otherwise.
     */
    boolean acquire(int permits);

    /**
     * Interface to represent a contiguous piece of code that needs to be rate limited.
     */
    interface Code {
        // Execute the code delegated to this interface.
        void invoke();
    }

}
