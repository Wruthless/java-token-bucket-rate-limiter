package org.wruthess.ratelimiter;

public class Main {

    /**
     * Driver code. Create threads with each attempting to count from 0 to 100
     * with a delay of 50ms. The API does not support smoothing, it forces
     * the threads to wait until the next token is assigned rather than dropping
     * requests. Upon rejection, the program returns false. Queuing up the drop
     * requests is possible.
     *
     * @param args
     */
    public static void main(String[] args) {
        final RateLimiter limiter = new SimpleTokenBucketRateLimiter(1);
        Thread[] group = new Thread[6];

        Runnable r = () -> {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (limiter.acquire()) {
                    System.out.println("Values:- " + Thread.currentThread().getName() + " : " + i);
                }
            }
        };

        for (int i = 0; i < 6; i++) {
            group[i] = new Thread(r);
            group[i].start();
        }
    }
}
