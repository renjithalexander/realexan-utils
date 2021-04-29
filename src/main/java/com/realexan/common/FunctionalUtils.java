/**
 * 
 */
package com.realexan.common;

import java.util.function.Consumer;

import com.realexan.util.function.ThrowingRunnable;

/**
 * 
 * Utilities realted to functional programming.
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
 *          <td>08-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 *
 */
public class FunctionalUtils {

    public static final Runnable NO_OP_RUNNABLE = () -> {
    };

    public static final ThrowingRunnable NO_OP_THROWING_RUNNABLE = () -> {
    };

    public static final Consumer<?> NO_OP_CONSUMER = (t) -> {
    };

    /**
     * Type casts the input to output. Could cause ClassCastException if not used
     * with diligence.
     * 
     * @param <T> type to which the cast has to be done.
     * @param t   the input.
     * @return the input casted to the output type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object t) {
        return (T) t;
    }

    /**
     * Iterates through the iterable and feeds it to the consumer.
     * 
     * @param <T>      the type of elements in the iterable.
     * @param ite      the iterable.
     * @param consumer the consumer.
     */
    public static <T> void forEach(Iterable<T> ite, Consumer<T> consumer) {
        for (T t : ite) {
            consumer.accept(t);
        }
    }

    /**
     * Iterates through the array and feeds it to the consumer.
     * 
     * @param <T>      the type of elements in the array.
     * @param arr      the array.
     * @param consumer the consumer.
     */
    public static <T> void forEach(T[] arr, Consumer<T> consumer) {
        for (T t : arr) {
            consumer.accept(t);
        }
    }

    /**
     * Runs a for loop from start to end(excluded) supplying the value to the
     * consumer.
     * 
     * @param start    the starting index.
     * @param end      ending index.
     * @param consumer the function which consumes the index.
     */
    public static void forLoop(int start, int end, Consumer<Integer> consumer) {
        for (int i = start; i < end; ++i) {
            consumer.accept(i);
        }
    }

    /**
     * Runs a for loop from 0 to end(excluded) supplying the value to the consumer.
     * 
     * @param end      ending index.
     * @param consumer the function which consumes the index.
     */
    public static void forLoop(int end, Consumer<Integer> consumer) {
        forLoop(0, end, consumer);
    }

    /**
     * Runs a for loop from start to end(excluded) running the runnable.
     * 
     * @param start    the starting index.
     * @param end      ending index.
     * @param runnable the function which runs.
     */
    public static void forLoop(int start, int end, Runnable runnable) {
        for (int i = start; i < end; ++i) {
            runnable.run();
        }
    }

    /**
     * Runs a for loop from 0 to end(excluded) running the runnable.
     * 
     * @param end      ending index.
     * @param runnable the function which runs.
     */
    public static void forLoop(int end, Runnable runnable) {
        forLoop(0, end, runnable);
    }

    public static <T> Consumer<T> r2C(Runnable r) {
        return (c) -> {
            r.run();
        };
    }

}
