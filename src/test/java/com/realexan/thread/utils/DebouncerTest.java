package com.realexan.thread.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.realexan.common.ThreadUtils;
import com.realexan.common.ThrowingRunnable;
import com.realexan.thread.Debouncer;
import com.realexan.thread.Debouncer.Debounce;

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

    @After
    public void cleanup() throws Exception {
        if (debounce != null) {
            debounce.cancel();
        }

    }

    @Test
    public void testDebouncerInitialExecutionsWithCoolOff() throws Exception {
        TestRunnable function = new TestRunnable();
        debounce = Debouncer.create(function, 1000);
        Assert.assertTrue(function.callbacks.isEmpty());
        debounce.run();
        Assert.assertEquals(1, function.callbacks.size());

        CountDownLatch count = new CountDownLatch(1);
        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                count.countDown();
            }

        };
        function.addListener(listener);
        for (int i = 0; i < 5; ++i) {
            debounce.run();
        }
        Assert.assertTrue(count.await(5000, TimeUnit.MILLISECONDS));

        Assert.assertEquals(2, function.callbacks.size());
        System.out.println(function.callbacks);
        Assert.assertTrue(function.callbacks.get(1) - function.callbacks.get(0) >= 1000);
    }

    @Test
    public void testDebouncerContinuousExecution() throws Exception {
        TestRunnable function = new TestRunnable();
        CountDownLatch count = new CountDownLatch(2);
        debounce = Debouncer.create(function, 1000);
        Assert.assertTrue(function.callbacks.isEmpty());

        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                count.countDown();
            }

        };
        function.addListener(listener);
        for (int i = 0; i < 100; ++i) {
            debounce.run();
        }
        Assert.assertTrue(count.await(5000, TimeUnit.MILLISECONDS));

        Assert.assertEquals(2, function.callbacks.size());
        System.out.println(function.callbacks);
        Assert.assertTrue(function.callbacks.get(1) - function.callbacks.get(0) >= 1000);
    }

    private class TestRunnable implements ThrowingRunnable {

        List<ActionListener> listeners = new ArrayList<>();

        List<Long> callbacks = new ArrayList<>();;

        @Override
        public void execute() throws Throwable {
            System.out.println(ThreadUtils.now());
            callbacks.add(ThreadUtils.now());
            for (ActionListener l : listeners) {
                l.actionPerformed(null);
            }
        }

        public void addListener(ActionListener listener) {
            listeners.add(listener);
        }

    }

}
