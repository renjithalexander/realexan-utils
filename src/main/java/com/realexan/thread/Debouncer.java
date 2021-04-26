/**
 * 
 */
package com.realexan.thread;

import static com.realexan.common.ThreadUtils.now;
import static com.realexan.common.ThreadUtils.runWithLock;
import static com.realexan.common.ThreadUtils.toExceptionSuppressedRunnable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.realexan.functional.functions.ThrowingRunnable;

/**
 * 
 * A debounce functionality, which prevents flooded executions of functions on
 * flurry of triggers. It allows to have a cool off period between executions,
 * which gets extended with each trigger. It can be configured to force run at
 * specific intervals in case the triggers delay the execution for too long.
 * <p>
 * The function can be configured to execute immediately on the first trigger
 * and then wait to cool off, or delay the execution until the cool off
 * period.<br>
 * It can also be configured to do the executions on a separate
 * SingleThreadedExecutor, or an executor service provided, so that the delay in
 * executions won't add up to the delays in scheduling.
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
     * The default idle timeout to kill the timer if no scheduling happens for the
     * duration.
     */
    private static final long DEFAULT_IDLE_TIMER_KEEPALIVE_TIME = 60_000;

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
     * @param idleThreadTimeout The idle timeout for scheduler thread, beyond which
     *                          the timer will be killed. It will be recreated when
     *                          the subsequent scheduling happens.This is to save
     *                          thread resources. A negative value means the
     *                          scheduler thread will be kept alive.
     * @return a debounce function.
     */
    public static Debounce create(String name, ThrowingRunnable function, long coolOffTime, long forcedRunInterval,
            boolean immediate, ExecutorService executor, long idleThreadTimeout) {
        return new DebounceImpl(name, function, coolOffTime, forcedRunInterval, immediate, executor, idleThreadTimeout);
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
     * @param executor          the executor to be used to run the function. If
     *                          null, the runs will happen on scheduler thread which
     *                          manages the cool off.
     * @return a debounce function.
     */
    public static Debounce create(String name, ThrowingRunnable function, long coolOffTime, long forcedRunInterval,
            boolean immediate, ExecutorService executor) {
        return create(name, function, coolOffTime, forcedRunInterval, immediate, executor,
                DEFAULT_IDLE_TIMER_KEEPALIVE_TIME);
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
     * @param idleThreadTimeout The idle timeout for scheduler thread, beyond which
     *                          the timer will be killed. It will be recreated when
     *                          the subsequent scheduling happens.This is to save
     *                          thread resources. A negative value means the
     *                          scheduler thread will be kept alive.
     * @return a debounce function.
     */
    public static Debounce create(String name, ThrowingRunnable function, long coolOffTime, long forcedRunInterval,
            boolean immediate, boolean runNonBlocked, long idleThreadTimeout) {
        ThreadPoolExecutor executor = null;
        if (runNonBlocked) {
            executor = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                    new NamedThreadFactory("Debounce-" + name + "-Threadpool"));
            executor.allowCoreThreadTimeOut(true);
        }
        return create(name, function, coolOffTime, forcedRunInterval, immediate, executor, idleThreadTimeout);
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
        return create(name, function, coolOffTime, forcedRunInterval, immediate, runNonBlocked,
                DEFAULT_IDLE_TIMER_KEEPALIVE_TIME);
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
         * The name.
         */
        private final String name;
        /**
         * Timer used to schedule next potential run.
         */
        private Timer timer;
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
         * The scheduler idle timeout.
         */
        private final long idleTimeout;
        /**
         * The flag which denotes whether the function is alive or not.
         */
        private boolean isAlive = true;
        /**
         * Flag denoting that there is a schedule yet to be fired.
         */
        private boolean scheduleExists = false;
        /**
         * The last submitted state.
         */
        private State submission = new State(-1, 0);
        /**
         * The last executed state.
         */
        private State execution = new State(-1, 0);
        /**
         * The last scheduled idle check task. This will be cancelled and the timer
         * purged when a new idle check task is scheduled, and the new task will be set
         * to this reference.
         */
        private IdleTimeoutTask lastScheduledIdleCheck = null;

        /**
         * Constructor.
         * 
         * @param function          the function to be executed.
         * @param coolOffTime       the cool off time.
         * @param forcedRunInterval the forced run interval if the triggers delay the
         *                          execution too long.
         * @param immediate         flag to denote whether to execute the function
         *                          immediately on the trigger or wait until cool off.
         * @param idleTimeout       The idle timeout for timer thread, beyond which the
         *                          timer will be killed. It will be recreated when the
         *                          subsequent scheduling happens.
         * @param executor          the executor to be used to run the function.
         */
        private DebounceImpl(String name, ThrowingRunnable function, long coolOffTime, long forcedRunInterval,
                boolean immediate, ExecutorService executor, long idleTimeout) {
            Objects.requireNonNull(function);
            this.name = name;
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
            this.idleTimeout = idleTimeout;
        }

        /**
         * Kills the Debounce function.
         */
        private void kill() {
            if (timer != null) {
                timer.cancel();
            }
            if (executor != null) {
                executor.shutdown();
            }
            this.isAlive = false;
        }

        private long getTimeElapsedSinceLastExecution() {
            // Nothing executed yet. Initialize the execution with current time.
            if (execution.id == -1 && execution.eventTime == 0) {
                execution = new State(-1, now());
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
         * Submits the next timer task. The idea is to not schedule more than one timer
         * task so that thousands of submits won't hog the timer task queue. When the
         * nearest timer fires, the next course of action is identified - schedule
         * another run or schedule an idle check task for shutting down the timer
         * thread(if configured so).<br>
         * At any point of time, there will be a maximum of only one run task and one
         * idle check task scheduled in the timer.
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
            // If schedules exist, its fire event will take care of the next scheduling.
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
         * Prepare the next action and execute it. If no run scheduling is done,
         * schedules an idle check task.
         * 
         * @param id
         */
        private void eventFired(long id) {
            scheduleExists = false;
            if (submission.id == execution.id) {
                // do nothing. This is just cool off.
                scheduleIdleCheck();
                return;
            }
            // If there are more submissions.
            if (id != submission.id) {
                // check if it has crossed the max delay between runs.
                boolean mustRun = forceRun();
                // Find the delay until next run
                long nextRun = coolOffTime - (now() - submission.eventTime);
                // Run has to be done.
                if (nextRun <= 0 || mustRun) {
                    execute(submission.id);
                }
                // long pending next run, which is already run(above). Now be done.
                if (nextRun <= 0) {
                    scheduleIdleCheck();
                    return;
                }
                // Schedule for next run.
                schedule(new DebounceTask(submission.id), nextRun);
            } else {
                // This one is the latest submission. Run and be done.
                execute(id);
                scheduleIdleCheck();
            }
        }

        /**
         * Schedules the timer task after the specified delay.
         * 
         * @param r
         * @param delay
         */
        private void schedule(DebounceTask r, long delay) {
            if (timer == null) {
                timer = new Timer("Debounce-" + name);
            }
            scheduleExists = true;
            // p("Next run scheduled for " + delay);
            timer.schedule(r, delay);
        }

        /**
         * Schedules an IdleTimeoutTask if idle timeout is configured.
         */
        private void scheduleIdleCheck() {
            if (timer != null && idleTimeout > 0) {
                if (this.lastScheduledIdleCheck != null) {
                    this.lastScheduledIdleCheck.cancel();
                    // There will be at most two items in the queue, which is a heap data structure.
                    // Thus removing the cancelled tasks through purge would be quick.
                    timer.purge();
                }
                this.lastScheduledIdleCheck = new IdleTimeoutTask(submission.id);
                timer.schedule(this.lastScheduledIdleCheck, idleTimeout);
            }
        }

        /**
         * If there have been no tasks for the idle time, kill the timer. The timer will
         * be recreated when the next scheduling happens.This way there will not be any
         * overhead of having an idle thread for non frequently used Debounce functions.
         * 
         * @param lastSubmissionId the submission id when the idle timer task was
         *                         scheduled.
         */
        private void idleCheck(IdleTimeoutTask idleCheckTask) {
            // No new submissions after lastSubmissionId
            if (submission.id == idleCheckTask.id) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
            if (idleCheckTask.equals(this.lastScheduledIdleCheck)) {
                this.lastScheduledIdleCheck = null;
            }
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

            protected final long id;

            private DebounceTask(long submissionId) {
                this.id = submissionId;
            }

            @Override
            public void run() {
                runWithLock(lock, () -> DebounceImpl.this.eventFired(id));
            }
        }

        /**
         * The task which kills the timer in case it stays idle for the defined idle
         * timeout.
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
         *          <td>25-Apr-2021</td>
         *          <td><a href=
         *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
         *          <td align="right">1</td>
         *          <td>Creation</td>
         *          </tr>
         *          </table>
         */
        private class IdleTimeoutTask extends DebounceTask {

            private IdleTimeoutTask(long lastSubmissionId) {
                super(lastSubmissionId);
            }

            @Override
            public void run() {
                runWithLock(lock, () -> DebounceImpl.this.idleCheck(this));
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
     * The Debounce function. It allows controlled execution of the original
     * function which was used to create the debounce function.
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
         * function. Throws IlegalStateException if the debounce has been cancelled.
         */
        void run();

        /**
         * Cancels the function.
         */
        void cancel();
    }

}
