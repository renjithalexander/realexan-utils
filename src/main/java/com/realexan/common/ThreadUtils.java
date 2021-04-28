package com.realexan.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.locks.Lock;

import com.realexan.util.function.ThrowingRunnable;

/**
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
public class ThreadUtils {

    /**
     * Runs a <code>ThrowingFunction</code> suppressing the exceptions thrown during
     * the run.
     * 
     * @param fun the enclosing function.
     * @return a Runnable which composes the function, and suppresses any exception
     *         thrown.
     */
    public static Runnable toExceptionSuppressedRunnable(ThrowingRunnable fun) {
        return () -> {
            try {
                fun.execute();
            } catch (Throwable t) {

            }
        };
    }

    /**
     * Runs the <code>ThrowingRunnable</code> after holding the lock. Releases the
     * lock once done.
     * 
     * @param lock the lock to be held before executing the function.
     * @param fn   the function to be executed.
     */
    public static void runWithLock(Lock lock, ThrowingRunnable fn) {
        lock.lock();
        try {
            fn.execute();
        } catch (Throwable e) {
            throw new RuntimeException("Exception while calling function", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current time in milliseconds.
     * 
     * @return the current time in milliseconds.
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Sleeps the current thread.
     */
    public static void sleep(long delay) {
        toExceptionSuppressedRunnable(() -> Thread.sleep(delay)).run();
    }

    /**
     * Returns the stack trace of the Throwable in String format.
     * 
     * @param t the throwable.
     * @return stack trace.
     */
    public static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
