/**
 * 
 */
package com.realexan.functional;

import java.util.function.Function;

/**
 * 
 * 
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
 *          <td><a href="mailto:renjithalexander@gmail.com">renjithalexander</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 *
 */
public interface ThrowingFunction<T, U> {

    U apply(T input) throws Throwable;

    static <T, U> Function<T, TryResult<U>> getFunction(ThrowingFunction<T, U> function, T input) {
        return (Void) -> {
            try {
                return new TryResult<>(function.apply(input));
            } catch (Throwable e) {
                return new TryResult<>(e);
            }
        };

    }

    static <T, U> U execute(ThrowingFunction<T, U> function, T input, U defaultVal) {
        TryResult<U> result = getResult(function, input);
        return result.isSuccess() ? result.getSuccess() : defaultVal;
    }

    static <T, U> U execute(ThrowingFunction<T, U> function, T input) {
        return execute(function, input, null);
    }

    static <T, U> TryResult<U> getResult(ThrowingFunction<T, U> function, T input) {
        return getFunction(function, input).apply(input);
    }

}
