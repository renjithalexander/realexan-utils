package com.realexan.thread.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.realexan.common.ThreadUtils;
import com.realexan.common.ThrowingRunnable;
import com.realexan.thread.Debouncer;
import com.realexan.thread.Debouncer.Debounce;

public class DebouncerTest {
    
    
    @Test
    public void testDebouncer() throws Exception {
        TestRunnable function = new TestRunnable();
        Debounce debounce = Debouncer.create(function, 1000);
        Assert.assertTrue(function.callbacks.isEmpty());
        debounce.run();
        Assert.assertEquals(1, function.callbacks.size());
        function.clear();
        
        CountDownLatch count = new CountDownLatch(2);
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
    
    private class TestRunnable implements ThrowingRunnable {
        
        List<ActionListener> listeners = new ArrayList<>();
        
        
        List<Long> callbacks = new ArrayList<>();;

        @Override
        public void execute() throws Throwable {
            System.out.println(ThreadUtils.now());
            callbacks.add(ThreadUtils.now());
            for(ActionListener l : listeners) {
                l.actionPerformed(null);
            }
        }
        
        public void addListener(ActionListener listener) {
            listeners.add(listener);
        }
     
        public void removeListener(ActionListener listener) {
            listeners.remove(listener);
        }
        public void clear() {
            callbacks.clear();
        }
        
        
        
    }

}
