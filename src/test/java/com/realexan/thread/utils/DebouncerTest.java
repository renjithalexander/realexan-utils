package com.realexan.thread.utils;

import static com.realexan.common.FunctionalUtils.NO_OP_THROWING_RUNNABLE;
import static com.realexan.common.FunctionalUtils.forEach;
import static com.realexan.common.FunctionalUtils.forLoop;
import static com.realexan.common.ReflectionUtils.getField;
import static com.realexan.common.ThreadUtils.sleep;
import static com.realexan.junit.utils.JUtils.failTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.realexan.common.ThreadUtils;
import com.realexan.thread.Debouncer;
import com.realexan.thread.Debouncer.Debounce;
import com.realexan.trial.Try;
import com.realexan.trial.TryResult;
import com.realexan.util.function.ThrowingRunnable;

/**
 * Test for Debouncer.
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
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class DebouncerTest {

    private Debounce debounce;

    private final String name = "junit";

    @After
    public void cleanup() throws Exception {
        if (debounce != null) {
            debounce.cancel();
        }

    }

    /**
     * Tests creation and cancellation of the debounce function.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateAndCancel() throws Exception {
        debounce = Debouncer.create(name, NO_OP_THROWING_RUNNABLE, 1000);
        assertNotNull(debounce);

        // Timer sill be lazy initialized.
        TryResult<String, Object> res = Try.doTry("timer", t -> getField(debounce, t));
        res.ifSucceeded(Assert::assertNull).ifFailed(failTest());

        // Try triggering it for a hundred times.
        Try.doTry(() -> forLoop(100, debounce::run)).ifFailed(failTest());

        // Now the timer is initialized
        Object timer = Try.doTry(() -> getField(debounce, "timer")).ifSucceeded(Assert::assertNotNull)
                .ifFailed(failTest()).getOutput();

        int queueSize = getField(getField(timer, "queue"), "size");
        // The queue size must not go over 2. That too, a maximum of one DebouceTask,
        // and a maximum of one TimeoutTask.
        assertTrue(queueSize <= 2);

        debounce.cancel();
        // Once cancelled, it must not succeed
        Try.doTry(() -> forLoop(100, debounce::run)).ifSucceeded(failTest())
                .ifFailed(e -> assertTrue(e.getCause() instanceof IllegalStateException));

    }

    /**
     * Tests debounce with immediate firing and a cool off period of 1000ms
     * 
     * @throws Exception
     */
    @Test
    public void testDebouncerExecutionsImmediate() throws Exception {
        TestRunnable function = new TestRunnable();
        debounce = Debouncer.create(name, function, 1000);
        assertTrue(function.callbacks.isEmpty());
        debounce.run();
        // Immediate call back
        assertEquals(1, function.callbacks.size());
        // The first function call is through caller thread.
        assertEquals(Thread.currentThread(), function.callBackThreads.get(0));

        Thread.sleep(2000);
        // No more function calls for just one trigger
        assertEquals(1, function.callbacks.size());

        CountDownLatch count = new CountDownLatch(2);
        ActionListener listener = (e) -> count.countDown();
        function.addListener(listener);
        // 100 triggers
        forLoop(100, debounce::run);

        // Wait for all function calls
        assertTrue(count.await(5000, TimeUnit.MILLISECONDS));

        assertEquals(3, function.callbacks.size());
        // The function calls must be cool off time apart.
        assertTrue(function.callbacks.get(2) - function.callbacks.get(1) >= 1000);
        // The new function call is from debounce thread.
        assertEquals("Debounce-junit", function.callBackThreads.get(2).getName());
    }

    /**
     * Tests debounce with delayed firing and a cool off period of 1000ms
     * 
     * @throws Exception
     */
    @Test
    public void testDebouncerExecutionsDelayed() throws Exception {
        TestRunnable function = new TestRunnable();
        debounce = Debouncer.create(name, function, 1000, -1, false, false);
        assertTrue(function.callbacks.isEmpty());
        debounce.run();
        // No immediate function call.
        assertEquals(0, function.callbacks.size());

        CountDownLatch count = new CountDownLatch(1);
        ActionListener listener = (e) -> count.countDown();
        function.addListener(listener);
        // 100 triggers
        forLoop(100, debounce::run);
        // Still no function calls.
        assertEquals(0, function.callbacks.size());
        // Wait for the function call.
        assertTrue(count.await(5000, TimeUnit.MILLISECONDS));
        // Make sure there are no more function calls.
        Thread.sleep(3000);
        // For all the triggers, there should be only one function call.
        assertEquals(1, function.callbacks.size());
        // That function call must be made from the Debounce thread.
        assertEquals("Debounce-junit", function.callBackThreads.get(0).getName());
    }

    /**
     * Tests debounce with delayed firing and a cool off period of 500ms and forced
     * run interval of 1000ms
     * 
     * @throws Exception
     */
    @Test
    public void testDebouncerExecutionsWithForcedRun() throws Exception {
        TestRunnable function = new TestRunnable();
        debounce = Debouncer.create(name, function, 500, 1000, false, false);
        assertTrue(function.callbacks.isEmpty());

        CountDownLatch count = new CountDownLatch(1);
        ActionListener listener = (e) -> count.countDown();
        function.addListener(listener);
        // 100 triggers interleaved by 100ms.
        forLoop(20, (i) -> {
            debounce.run();
            sleep(100);
        });
        // There should be at least one function call due to forced run
        assertTrue(function.callbacks.size() >= 1);
        // Wait for the function call.
        assertTrue(count.await(5000, TimeUnit.MILLISECONDS));
        // Make sure there are no more function calls.
        Thread.sleep(3000);
        // There should be two or more function calls.
        assertTrue(function.callbacks.size() >= 2);
        // All those function calls must be made from the Debounce thread.
        forEach(function.callBackThreads, t -> assertEquals("Debounce-junit", t.getName()));
    }

    /**
     * Tests debounce with immediate firing non blocking.
     * 
     * @throws Exception
     */
    @Test
    public void testDebouncerExecutionsNonBlocking() throws Exception {
        TestRunnable function = new TestRunnable();
        debounce = Debouncer.create(name, function, 1000, -1, true, true);
        assertTrue(function.callbacks.isEmpty());
        CountDownLatch count = new CountDownLatch(1);
        ActionListener listener = (e) -> count.countDown();
        function.addListener(listener);

        debounce.run();
        assertTrue(count.await(5000, TimeUnit.MILLISECONDS));
        // Wait for function call
        assertEquals(1, function.callbacks.size());
        System.out.println(function.callBackThreads.get(0));
        // The new function call is from debounce thread pool.
        assertTrue(function.callBackThreads.get(0).getName().contains("Debounce-junit-Threadpool"));
    }

    private class TestRunnable implements ThrowingRunnable {

        List<ActionListener> listeners = new ArrayList<>();

        List<Long> callbacks = new ArrayList<>();

        List<Thread> callBackThreads = new ArrayList<>();

        @Override
        public void execute() throws Throwable {
            callbacks.add(ThreadUtils.now());
            callBackThreads.add(Thread.currentThread());
            forEach(listeners, l -> l.actionPerformed(null));
        }

        public void addListener(ActionListener listener) {
            listeners.add(listener);
        }

    }

}
