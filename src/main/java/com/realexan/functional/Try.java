/**
 * 
 */
package com.realexan.functional;

import java.net.URI;

/**
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
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 *
 */
public class Try<T, U> {

    private final ThrowingFunction<T, U> function;

    public Try(ThrowingFunction<T, U> function) {
        this.function = function;
    }

    public TryResult<T, U> tryIt(T input) {
        return ThrowingFunction.getResult(this.function, input);
    }

    public static <T, U> TryResult<T, U> tryOn(T input, Try<T, U> trial) {
        return trial.tryIt(input);
    }

    public static <T, U> TryResult<T, U> tryOn(T input, ThrowingFunction<T, U> function) {
        return new Try<>(function).tryIt(input);
    }

    public static <T, U> U tryOn(T input, Try<T, U> transformer, U defaultVal) {
        TryResult<T, U> result = Try.tryOn(input, transformer);
        return result.isSuccess() ? result.getSuccess() : defaultVal;
    }

    public static <T, U> U tryOn(T input, ThrowingFunction<T, U> function, U defaultVal) {
        TryResult<T, U> result = Try.tryOn(input, function);
        return result.isSuccess() ? result.getSuccess() : defaultVal;
    }

    public static void main(String... args) {
        ThrowingFunction.getFunction(URI::new, args[0]);
    }

}
