/**
 * 
 */
package com.realexan.thread.utils;


import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * 
 *
 * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
 * @version
 *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
 *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
 *          <td width="10%"><b>Date</b></td>
 *          <td width="10%"><b>Author</b></td>
 *          <td width="10%"><b>Version</b></td>
 *          <td width="*"><b>Description</b></td>
 *          </tr>
 *          <tr bgcolor="white" id="TableRowColor">
 *          <td>23-Apr-2021</td>
 *          <td><a href="mailto:renjithalexander@gmail.com">Renjith
 *          Alexander</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 *
 * 
 */
public class Debouncer {

    public static Debounce prepare(ThrowingFunction function, long wait, long maxDelayBetweenExecutions,
            boolean immediate) {
        DebounceImpl state = new DebounceImpl(function, wait, maxDelayBetweenExecutions, immediate);
        return state;
    }

    private static class DebounceImpl implements Debounce {

        private final Lock lock = new ReentrantLock();

        private final Timer timer = new Timer();

        private final Runnable function;

        private final boolean immediate;

        private final long waitTime;

        private final long maxDelayBetweenExecutions;

        private volatile boolean isAlive = true;

        private volatile boolean scheduleExists = false;

        private volatile State submission = new State(-1, 0);

        private volatile State execution = new State(-1, 0);


        public DebounceImpl(ThrowingFunction function, long waitTime, long maxDelayBetweenExecutions, boolean immediate) {
            Objects.requireNonNull(function);
            this.function = toExceptionSuppressedRunnable(function);
            if (waitTime <= 0) {
                throw new IllegalArgumentException("Invalid wait time value");
            }
            this.waitTime = waitTime;
            if (maxDelayBetweenExecutions >= 0 && maxDelayBetweenExecutions < waitTime) {
                maxDelayBetweenExecutions = waitTime;
            }
            this.maxDelayBetweenExecutions = maxDelayBetweenExecutions;
            this.immediate = immediate;
        }

        private void kill() {
            timer.cancel();
            this.isAlive = false;
        }
        
        private long getTimeElapsedSinceLastExecution() {
            if (execution.eventTime == 0) {
                return 0;
            }
            return now() - execution.eventTime;
        }


        public boolean forceRun() {
            return maxDelayBetweenExecutions > 0 && getTimeElapsedSinceLastExecution() >= maxDelayBetweenExecutions;
        }
        
        private long getNextSubmissionId() {
            long nextSubmissionId = submission.id + 1;
            if (nextSubmissionId < 0) {
                nextSubmissionId = 0;
            }
            return nextSubmissionId;
        }

        public void submit() {
            if (!isAlive) {
                throw new IllegalStateException("Debouncer cancelled");
            }
            
            submission = new State(getNextSubmissionId(), now());
            //p("Submission id is " + submission.id);

            if (!scheduleExists) {
                DebounceTask toExecute = new DebounceTask(submission.id);
                if (immediate) {
                    // Execute it right away.
                    execute(submission.id);
                }
                schedule(toExecute, waitTime);
            }

        }

        private void execute(long id) {
            function.run();
            execution = new State(id, now());
        }
        
        public void eventFired(long id) {
            scheduleExists = false;
            //p("eventfired...id: " + id + ", sub id: " + submission.id + ", exe id:" + execution.id);
            if (submission.id == execution.id) {
                // do nothing. This is just cool off.
                return;
            } 
            // If there are more submissions.
            if (id != submission.id) {
               // check if it has crossed the max delay between runs.
                boolean mustRun = forceRun();
                // Find the time for next run
                long nextRun = waitTime - (now() - submission.eventTime);
                // If next run is long pending, run and be done.
                if (nextRun <= 0) {
                    execute(submission.id);
                    return;
                }
                // If max delay run
                if (mustRun) {
                    //p("Must run exe");
                    execute(id);
                }
                // Schedule for next run.
                schedule(new DebounceTask(submission.id), nextRun);
            } else if (id == submission.id) {
                // This one is the latest submission. Run and be done.
                execute(id);
            }
        }


        private void schedule(DebounceTask r, long delay) {
            scheduleExists = true;
            //p("Next run scheduled for " + delay);
            timer.schedule(r, delay);
        }


        private class DebounceTask extends TimerTask {

            private final long id;

            public DebounceTask(long submissionId) {
                this.id = submissionId;
            }

            @Override
            public void run() {
                runWithLock(lock, () -> DebounceImpl.this.eventFired(id));
            }
        }

        private class State {

            final long id;
            final long eventTime;

            public State(long id, long eventTime) {
                this.id = id;
                this.eventTime = eventTime;
            }
        }

        @Override
        public void run() {
            runWithLock(lock, this::submit);

        }

        @Override
        public void cancel() {
            runWithLock(lock, this::kill);
        }
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private static void runWithLock(Lock lock, ThrowingFunction fn) {
        lock.lock();
        try {
            fn.execute();
        } catch (Throwable e) {
            throw new RuntimeException("Exception while calling function", e);
        } finally {
            lock.unlock();
        }
    }

    @FunctionalInterface
    public static interface ThrowingFunction {
        void execute() throws Throwable;
    }

    /**
     * Runs a <code>ThrowingFunction</code> suppressing the exceptions thrown during the run.
     * @param fun
     * @return
     */
    public static Runnable toExceptionSuppressedRunnable(ThrowingFunction fun) {
        return () -> {
            try {
                fun.execute();
            } catch (Throwable t) {

            }
        };
    }

    /**
     * The Debounce function interface.
     * 
     * @author <a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a>
     * @version
     *          <table border="1" cellpadding="3" cellspacing="0" width="95%">
     *          <tr bgcolor="#EEEEFF" id="TableSubHeadingColor">
     *          <td width="10%"><b>Date</b></td>
     *          <td width="10%"><b>Author</b></td>
     *          <td width="10%"><b>Version</b></td>
     *          <td width="*"><b>Description</b></td>
     *          </tr>
     *          <tr bgcolor="white" id="TableRowColor">
     *          <td>24-Apr-2021</td>
     *          <td><a href="mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
     *          <td align="right">1</td>
     *          <td>Creation</td>
     *          </tr>
     *          </table>
     */
    public static interface Debounce {

        void run();

        void cancel();
    }

}
