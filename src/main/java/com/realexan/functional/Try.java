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
 *          <td><a href="mailto:renjithalexander@gmail.com">renjithalexander</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 *
 */
public class Try<T, U> {

    private final T input;

    private final ThrowingFunction<T, U> function;

    private TryResult<U> result;

    public Try(T t, ThrowingFunction<T, U> function) {
        this.input = t;
        this.function = function;
    }

    public static <T, U> Try<T, U> tryOn(T input, Try<T, U> trial) {
        return new Try<>(input, y -> trial.function.apply(y));
    }

    public Try<T, U> tryIt() {
        if (result != null) {
            result = ThrowingFunction.getResult(this.function, this.input);
        }
        return this;
    }

    public Try<T, U> onFailure(Try<T, U> tryAgain) {
        if (result == null) {
            tryIt();
        }
        if (!result.isSuccess()) {
            return tryAgain.tryIt();
        }
        return this;
    }

    public U onFailure(U defaultVal) {
        if (result == null) {
            tryIt();
        }
        if (!result.isSuccess()) {
            return defaultVal;
        }
        return result.getSuccess();
    }

    public U getResult() {
        return getResult(false);
    }

    public U getResultTryAfresh() {
        return getResult(true);
    }

    private U getResult(boolean afresh) {
        if (afresh || result == null) {
            tryIt();
        }
        return result.getSuccess();
    }

    public static <T, U> U tryDo(T input, Try<T, U> transformer, U defaultVal) {
        Try<T, U> tryIt = Try.tryOn(input, transformer);
        tryIt.tryIt();
        if (tryIt.result.isSuccess()) {
            return tryIt.result.getSuccess();
        }
        return defaultVal;
    }

    public static void main(String... args) {
        ThrowingFunction.getFunction(URI::new, args[0]);
    }

}
