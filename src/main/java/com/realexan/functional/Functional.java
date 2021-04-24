/**
 * 
 */
package com.realexan.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.realexan.functional.trial.Try;

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
public class Functional {

    /**
     * Try to parse the string to integer, and returns the result. If fails, returns
     * -1;
     * 
     * @param s the String passed.
     * @return the parsed value or -1 if parsing failed.
     */
    public static int tryParse(String s) {
        return tryParse(s, -1);
    }

    /**
     * Try to parse the string to integer, and returns the result. If fails, returns
     * default value;
     * 
     * @param s          the String passed.
     * @param defaultVal the default value
     * @return the parsed value or default value if parsing failed.
     */
    public static int tryParse(String s, int defaultVal) {
        return Try.getResult(s, Integer::parseInt, defaultVal);
    }

    /**
     * Iterates through the iterable and feeds it to the consumer.
     * 
     * @param <T>      the type of elements in the iterable.
     * @param ite      the iterable.
     * @param consumer the consumer.
     */
    public static <T> void consume(Iterable<T> ite, Consumer<T> consumer) {
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
    public static <T> void consume(T[] arr, Consumer<T> consumer) {
        for (T t : arr) {
            consumer.accept(t);
        }
    }

    public static void main(String... args) {
        List<String> strs = new ArrayList<>();
        for (int i = 1; i <= 100; ++i) {
            strs.add("" + i);
        }
        consume(strs, System.out::println);

        String[] arr = new String[100];
        for (int i = 1; i <= 100; ++i) {
            arr[i-1] = "" + i;
        }
        consume(arr, System.out::println);
    }
}
