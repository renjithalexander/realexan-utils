/**
 * 
 */
package com.realexan.thread.utils;

import static com.realexan.thread.utils.DebounceTester.p;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
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
 *          <td><a href="mailto:renjithalexander@gmail.com">Renjith Alexander</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 *
 * 
 */
public class Debounce {

    public static DebounceRunner debounce(ThrowingFunction function, long wait, long maxDelayBetweenExecutions,
            boolean immediate) {
        State state = new State(function, wait, maxDelayBetweenExecutions, immediate);
        return state;
    }

    private static class State implements DebounceRunner {

        private final Lock lock = new ReentrantLock();

        private final Timer timer = new Timer();

        private final Runnable function;

        private final boolean immediate;

        private final long waitTime;

        private final long maxDelayBetweenExecutions;

        private final boolean hasToExecuteRegularly;

        private volatile boolean isAlive = true;

        private volatile boolean isQueued = false;

        private volatile long lastExecutedTime = 0;

        private volatile long lastSubmittedTime = 0;

        private final AtomicLong submissionId = new AtomicLong(0);

        public State(ThrowingFunction function, long waitTime, long maxDelayBetweenExecutions, boolean immediate) {
            Objects.requireNonNull(function);
            this.function = toExceptionSuppressedRunnable(function);
            if (waitTime <= 0) {
                throw new IllegalArgumentException("Invalid wait time value");
            }
            this.waitTime = waitTime;
            if (maxDelayBetweenExecutions == -1 || maxDelayBetweenExecutions <= waitTime) {
                this.hasToExecuteRegularly = false;
            } else {
                this.hasToExecuteRegularly = true;
            }
            this.maxDelayBetweenExecutions = maxDelayBetweenExecutions > waitTime ? maxDelayBetweenExecutions
                    : waitTime;
            this.immediate = immediate;
        }

        private void kill() {
            timer.cancel();
            this.isAlive = false;
        }

        public boolean isQueued() {
            return isQueued;
        }

        public boolean mustRun() {
            return hasToExecuteRegularly && (now() - lastExecutedTime) >= maxDelayBetweenExecutions;
        }

        public void submit() {
            long now = now();
            long coolOff = waitTime - (now - lastSubmittedTime);
            lastSubmittedTime = now;
            submissionId.incrementAndGet();
            p("Submission id is " + submissionId);

            if (!isQueued()) {
                DebounceFunction toExecute = new DebounceFunction(submissionId.get());
                if (coolOff > 0) {
                    submissionId.incrementAndGet();
                    schedule(toExecute, coolOff);
                } else if (immediate) {
                    toExecute.run();
                } else {
                    schedule(toExecute, waitTime);
                }
            }

        }

        private void runStarted() {
            this.lastExecutedTime = now();
        }

        private void runEnded(long id) {
            if (!isLatest(id)) {
                long nextRun = waitTime - (Math.max(0, lastExecutedTime - lastSubmittedTime));
                DebounceFunction toExecute = new DebounceFunction(submissionId.get());
                schedule(toExecute, nextRun);
            } else {
                isQueued = false;
            }
        }

        private void schedule(DebounceFunction r, long delay) {
            isQueued = true;
            p("Next run" + (r.isCoolOff() ? " (cooloff)" : "") + " scheduled for " + delay);
            timer.schedule(r, delay);
        }

        private boolean isLatest(long id) {
            // p("passed id " + id + " and current id " + this.submissionId);
            return this.submissionId.get() == id;
        }

        private void runFunction() {
            function.run();
        }

        private class DebounceFunction extends TimerTask {

            private long id;

            public DebounceFunction(long submissionId) {
                this.id = submissionId;
            }

            private boolean isCoolOff() {
                return id == -1;
            }

            @Override
            public void run() {
                runWithLock(lock, () -> {
                    boolean iamCurrent = isLatest(id);
                    if (iamCurrent || mustRun()) {
                        runStarted();
                        runFunction();

                    }
                    runEnded(id);
                });
            }
        }


        @Override
        public void run() {
            runWithLock(lock, () -> {
                if (!isAlive) {
                    throw new IllegalStateException("Debouncer cancelled");
                }
                submit();
            });

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

    public static Runnable toExceptionSuppressedRunnable(ThrowingFunction fun) {
        return () -> {
            try {
                fun.execute();
            } catch (Throwable t) {

            }
        };
    }

    public static interface DebounceRunner {

        void run();

        void cancel();
    }

}
