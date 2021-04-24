package com.realexan.thread.utils;

import org.junit.Test;

import com.realexan.common.ThreadUtils;
import com.realexan.thread.Debouncer;
import com.realexan.thread.Debouncer.Debounce;

public class DebouncerTest {
    
    @Test
    public void testDebouncer() throws Exception {
        Debounce debounce = Debouncer.prepare(() ->System.out.println(ThreadUtils.now()/1000), 3000);
        for (int i = 0; i < 5; ++i) {
            debounce.run();
            ThreadUtils.sleep(2500);
        }
        ThreadUtils.sleep(5000);
    }

}
