/**
 * 
 */
package com.realexan.thread;

import static com.realexan.common.ThreadUtils.runWithLock;
import static com.realexan.common.ThreadUtils.toExceptionSuppressedRunnable;
import static com.realexan.common.ThreadUtils.now;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.realexan.common.ThrowingRunnable;;;

/**
 * 
 * A debounce functionality, which prevents flooded execution of functions on
 * flurry of triggers. It allows to have a cool off period between executions,
 * which gets extended with each trigger . It can be configured to force run at
 * specific intervals in case the triggers delay the execution for too long.
 * <p>
 * The function can be configured to execute immediately on the first trigger
 * and then wait to cool off, or delay the execution until the cool off period.
 * It can also be configured to do the executions on a separate
 * SingleThreadedExecutor, so that the delay in executions won't add up to the
 * delays in scheduling.
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
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class Debouncer {

    /**
     * Returns a debounce function.
     * 
     * @param name              the name of the debounce function.
     * @param function          the actual function to be executed.
     * @param coolOffTime       the cool off time period.
     * @param forcedRunInterval the interval for forced execution of the function,
     *                          in case the triggers don't cease for too long. A
     *                          negative value means this is disregarded. A non
     *                          negative value lesser than coolOffTime will cause to
     *                          use coolOffTime instead.
     * @param immediate         flag to denote whether to execute the function
     *                          immediately on the trigger or wait until cool off.
     * @param executor          the executor to be used to run the function. If
     *                          null, the runs will happen on scheduler thread which
     *                          manages the cool off.
     * @return a debounce function.
     */
    public static Debounce create(String name, ThrowingRunnable function, long coolOffTime, long forcedRunInterval,
            boolean immediate, ExecutorService executor) {
        return new DebounceImpl(name, function, coolOffTime, forcedRunInterval, immediate, executor);
    }

    /**
     * Returns a debounce function.
     * 
     * @param name              the name of the debounce function.
     * @param function          the actual function to be executed.
     * @param coolOffTime       the cool off time period.
     * @param forcedRunInterval the interval for forced execution of the function,
     *                          in case the triggers don't cease for too long. A
     *                          negative value means this is disregarded. A non
     *                          negative value lesser than coolOffTime will cause to
     *                          use coolOffTime instead.
     * @param immediate         flag to denote whether to execute the function
     *                          immediately on the trigger or wait until cool off.
     * @param runNonBlocked     runs the function in a single threaded executor if
     *                          the flag is true.
     * @return a debounce function.
     */
    public static Debounce create(String name, ThrowingRunnable function, long coolOffTime, long forcedRunInterval,
            boolean immediate, boolean runNonBlocked) {
        ExecutorService executor = runNonBlocked
                ? Executors.newSingleThreadExecutor(new NamedThreadFactory("Debounce-" + name + "-Threadpool"))
                : null;
        return create(name, function, coolOffTime, forcedRunInterval, immediate, executor);
    }

    /**
     * Returns a debounce function.
     * 
     * @param name        the name of the debounce function.
     * @param function    the actual function to be executed.
     * @param coolOffTime the cool off time period.
     * @return a debounce function.
     */
    public static Debounce create(String name, ThrowingRunnable function, long coolOffTime) {
        return create(name, function, coolOffTime, -1, true, false);
    }

    /**
     * Implementation of Debounce function.
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
     *          <td><a href=
     *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
     *          <td align="right">1</td>
     *          <td>Creation</td>
     *          </tr>
     *          </table>
     */
    private static class DebounceImpl implements Debounce {

        /**
         * The lock used to guard state.
         */
        private final Lock lock = new ReentrantLock();
        /**
         * Timer used to schedule next potential run.
         */
        private final Timer timer;
        /**
         * The actual function to be executed.
         */
        private final Runnable function;
        /**
         * Flag which denotes whether the execution is delayed or immediate on a
         * trigger.
         */
        private final boolean immediate;

        /**
         * The cool off time.
         */
        private final long coolOffTime;
        /**
         * The maximum interval to wait before force run if the triggers go on for too
         * long.
         */
        private final long forcedRunInterval;
        /**
         * The executor to be used to run the function, if configured to run so.
         */
        private final ExecutorService executor;
        /**
         * The flag which denotes whether the function is alive or not.
         */
        private volatile boolean isAlive = true;
        /**
         * Flag denoting that there is a schedule yet to be fired.
         */
        private volatile boolean scheduleExists = false;
        /**
         * The last submitted state.
         */
        private volatile State submission = new State(-1, 0);
        /**
         * The last executed state.
         */
        private volatile State execution = new State(-1, 0);

        /**
         * Constructor.
         * 
         * @param function          the function to be executed.
         * @param coolOffTime       the cool off time.
         * @param forcedRunInterval the forced run interval if the triggers delay the
         *                          execution too long.
         * @param immediate         flag to denote whether to execute the function
         *                          immediately on the trigger or wait until cool off.
         * @param executor          the executor to be used to run the function.
         */
        private DebounceImpl(String name, ThrowingRunnable function, long coolOffTime, long forcedRunInterval,
                boolean immediate, ExecutorService executor) {
            Objects.requireNonNull(function);
            timer = new Timer("Debounce-" + name);
            this.function = toExceptionSuppressedRunnable(function);
            if (coolOffTime <= 0) {
                throw new IllegalArgumentException("Invalid wait time value");
            }
            this.coolOffTime = coolOffTime;
            if (forcedRunInterval >= 0 && forcedRunInterval < coolOffTime) {
                forcedRunInterval = coolOffTime;
            }
            this.forcedRunInterval = forcedRunInterval;
            this.immediate = immediate;
            this.executor = executor;
        }

        /**
         * Kills the Debounce function.
         */
        private void kill() {
            timer.cancel();
            if (executor != null) {
                executor.shutdown();
            }
            this.isAlive = false;
        }

        private long getTimeElapsedSinceLastExecution() {
            if (execution.eventTime == 0) {
                return 0;
            }
            return now() - execution.eventTime;
        }

        /**
         * Returns true if forced run is configured and the time elapsed since last
         * execution time is more than forced run interval.
         * 
         * @return
         */
        private boolean forceRun() {
            return forcedRunInterval > 0 && getTimeElapsedSinceLastExecution() >= forcedRunInterval;
        }

        /**
         * Returns the next submission id sequence.
         * 
         * @return the next submission id sequence.
         */
        private long getNextSubmissionId() {
            long nextSubmissionId = submission.id + 1;
            if (nextSubmissionId < 0) {
                nextSubmissionId = 0;
            }
            return nextSubmissionId;
        }

        /**
         * Submits the next timer task.
         */
        private void submit() {
            if (!isAlive) {
                throw new IllegalStateException("Debouncer cancelled");
            }

            submission = new State(getNextSubmissionId(), now());

            // No schedules exist.
            if (!scheduleExists) {
                DebounceTask toExecute = new DebounceTask(submission.id);
                if (immediate) {
                    // Execute it right away.
                    execute(submission.id);
                }
                schedule(toExecute, coolOffTime);
            }
            // If schedules exists, the fire event will take care of the next scheduling.
        }

        /**
         * Calls the function.
         * 
         * @param id
         */
        private void execute(long id) {
            if (executor != null) {
                executor.submit(function);
            } else {
                function.run();
            }
            execution = new State(id, now());
        }

        /**
         * Prepare the next action and execute it.
         * 
         * @param id
         */
        private void eventFired(long id) {
            scheduleExists = false;
            if (submission.id == execution.id) {
                // do nothing. This is just cool off.
                return;
            }
            // If there are more submissions.
            if (id != submission.id) {
                // check if it has crossed the max delay between runs.
                boolean mustRun = forceRun();
                // Find the time for next run
                long nextRun = coolOffTime - (now() - submission.eventTime);
                // If next run is long pending, run and be done.
                if (nextRun <= 0) {
                    execute(submission.id);
                    return;
                }
                // If max delay run
                if (mustRun) {
                    execute(id);
                }
                // Schedule for next run.
                schedule(new DebounceTask(submission.id), nextRun);
            } else if (id == submission.id) {
                // This one is the latest submission. Run and be done.
                execute(id);
            }
        }

        /**
         * Schedules the timer task after the specified delay.
         * 
         * @param r
         * @param delay
         */
        private void schedule(DebounceTask r, long delay) {
            scheduleExists = true;
            // p("Next run scheduled for " + delay);
            timer.schedule(r, delay);
        }

        /**
         * The TimerTask for Debounce.
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
         *          <td><a href=
         *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
         *          <td align="right">1</td>
         *          <td>Creation</td>
         *          </tr>
         *          </table>
         */
        private class DebounceTask extends TimerTask {

            private final long id;

            private DebounceTask(long submissionId) {
                this.id = submissionId;
            }

            @Override
            public void run() {
                runWithLock(lock, () -> DebounceImpl.this.eventFired(id));
            }
        }

        /**
         * Execution state.
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
         *          <td><a href=
         *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
         *          <td align="right">1</td>
         *          <td>Creation</td>
         *          </tr>
         *          </table>
         */
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

        @Override
        public void close() throws IOException {
            cancel();

        }

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
     *          <td>23-Apr-2021</td>
     *          <td><a href=
     *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
     *          <td align="right">1</td>
     *          <td>Creation</td>
     *          </tr>
     *          </table>
     */
    public static interface Debounce extends Closeable {

        /**
         * Runs the function as per the configurations used while creating this
         * function. Throws IlegalStateException if the function has been cancelled.
         */
        void run();

        /**
         * Cancels the function.
         */
        void cancel();
    }

}
