package com.realexan.common;

import static com.realexan.common.FunctionalUtils.*;
import static com.realexan.junit.utils.JUtils.failTest;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.realexan.trial.Try;
import com.realexan.util.function.ThrowingSupplier;

/**
 * Unit tests for FunctionalUtils.
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
 *          <td>29-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class FunctionalUtilsTest {

    /**
     * Tests the cast method.
     * 
     * @throws Exception
     */
    @Test
    public void testCast() throws Exception {
        Object obj = new Integer(100);
        int val = cast(obj);
        assertEquals(100, val);

        Try.doTry((ThrowingSupplier<String>) () -> cast(obj)).ifSucceeded(failTest())
                .ifFailed(t -> assertTrue(t instanceof ClassCastException));
    }

    /**
     * Tests the forEach method on an array.
     * 
     * @throws Exception
     */
    @Test
    public void testForEach() throws Exception {
        Object[] obj = new Object[] { new Integer(100), null, "Hello", new Exception() };

        AtomicInteger inc = new AtomicInteger(0);
        forEach(obj, o -> {
            assertEquals(o, obj[inc.getAndIncrement()]);
        });
        assertEquals(obj.length, inc.get());
    }

    /**
     * Tests the forEach method on an iterable.
     * 
     * @throws Exception
     */
    @Test
    public void testForEachIterable() throws Exception {
        List<Object> obj = Arrays.asList(new Object[] { new Integer(100), null, "Hello", new Exception() });

        AtomicInteger inc = new AtomicInteger(0);
        forEach(obj, o -> {
            assertEquals(o, obj.get(inc.getAndIncrement()));
        });
        assertEquals(obj.size(), inc.get());
    }

    /**
     * Tests the for loop method with a consumer.
     * 
     * @throws Exception
     */
    @Test
    public void testForLoopWithConsumer() throws Exception {
        AtomicInteger inc = new AtomicInteger(5);
        forLoop(5, 100, i -> {
            assertEquals((int) i, inc.getAndIncrement());
        });
        assertEquals(100, inc.get());
    }

    /**
     * Tests the for loop method with a consumer.
     * 
     * @throws Exception
     */
    @Test
    public void testForLoopEndWithConsumer() throws Exception {
        AtomicInteger inc = new AtomicInteger(0);
        forLoop(100, i -> {
            assertEquals((int) i, inc.getAndIncrement());
        });
        assertEquals(100, inc.get());
    }

    /**
     * Tests the for loop method with a runnable.
     * 
     * @throws Exception
     */
    @Test
    public void testForLoopWithRunnable() throws Exception {
        AtomicInteger inc = new AtomicInteger(50);
        forLoop(5, 100, inc::getAndIncrement);
        assertEquals(50 + 95, inc.get());
    }

    /**
     * Tests the for loop method with a runnable.
     * 
     * @throws Exception
     */
    @Test
    public void testForLoopEndWithRunnable() throws Exception {
        AtomicInteger inc = new AtomicInteger(0);
        forLoop(100, inc::getAndIncrement);

        assertEquals(100, inc.get());
    }

}
