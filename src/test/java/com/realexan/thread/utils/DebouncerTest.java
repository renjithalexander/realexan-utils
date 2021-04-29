package com.realexan.thread.utils;

import static com.realexan.common.FunctionalUtils.NO_OP_THROWING_RUNNABLE;
import static com.realexan.common.FunctionalUtils.forEach;
import static com.realexan.common.FunctionalUtils.forLoop;
import static com.realexan.common.ReflectionUtils.getField;
import static com.realexan.junit.utils.JUConsumer.failTest;
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

    @Test
    public void testDebouncerInitialExecutionsWithCoolOff() throws Exception {
        TestRunnable function = new TestRunnable();
        debounce = Debouncer.create(name, function, 1000);
        assertTrue(function.callbacks.isEmpty());
        debounce.run();
        assertEquals(1, function.callbacks.size());

        CountDownLatch count = new CountDownLatch(1);
        ActionListener listener = (e) -> count.countDown();
        function.addListener(listener);
        forLoop(5, debounce::run);

        assertTrue(count.await(5000, TimeUnit.MILLISECONDS));

        assertEquals(2, function.callbacks.size());
        System.out.println(function.callbacks);
        assertTrue(function.callbacks.get(1) - function.callbacks.get(0) >= 1000);
    }

    @Test
    public void testDebouncerContinuousExecution() throws Exception {
        TestRunnable function = new TestRunnable();
        CountDownLatch count = new CountDownLatch(2);
        debounce = Debouncer.create(name, function, 1000);
        assertTrue(function.callbacks.isEmpty());

        ActionListener listener = e -> count.countDown();
        function.addListener(listener);
        forLoop(5, debounce::run);

        assertTrue(count.await(5000, TimeUnit.MILLISECONDS));

        assertEquals(2, function.callbacks.size());
        System.out.println(function.callbacks);
        assertTrue(function.callbacks.get(1) - function.callbacks.get(0) >= 1000);
    }

    private class TestRunnable implements ThrowingRunnable {

        List<ActionListener> listeners = new ArrayList<>();

        List<Long> callbacks = new ArrayList<>();;

        @Override
        public void execute() throws Throwable {
            System.out.println(ThreadUtils.now());
            callbacks.add(ThreadUtils.now());
            forEach(listeners, l -> l.actionPerformed(null));
        }

        public void addListener(ActionListener listener) {
            listeners.add(listener);
        }

    }

}
