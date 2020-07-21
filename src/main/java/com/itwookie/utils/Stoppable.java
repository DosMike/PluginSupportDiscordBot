package com.itwookie.utils;

public class Stoppable extends Thread implements AutoCloseable {
    private boolean started = false;
    private boolean running = false;

    @Override
    public final void run() {
        running = true;
        started = true;
        try {
            onStart();
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
        }
        while (running) {
            try {
                onLoop();
            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
            Thread.yield(); //for safety, so the app does not lock up, if the user forgets to sleep
        }
        running = false;
        try {
            onHalt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final boolean isStarted() {
        return started;
    }

    public final boolean isRunning() {
        return running;
    }

    /**
     * @return true if the thread stopped running after starting
     */
    public final boolean isFinished() {
        return started && !running && !isAlive();
    }

    public void onStart() {

    }

    /**
     * this function is permanently executed with no delay.<br>
     * If your implementation requires timing of any kind or you wish to pause, it's your responsibility to do so.
     */
    public void onLoop() {

    }

    public void onHalt() {

    }

    /**
     * the stop method for thread is final, so this method is called halt.<br>
     * not that halting a thread can only happen between loop executions!<br>
     * to accelerate a loop cycle the thread is interrupted once after disabling the halt flag
     */
    public final void halt() {
        running = false;
        interrupt();
    }

    /**
     * auto closeable is implemented for convenience
     */
    @Override
    public final void close() {
        halt();
    }
}
