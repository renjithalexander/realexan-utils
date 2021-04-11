/**
 * 
 */
package com.realexan.functional.trial;

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

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

    private final Function<T, TryResult<T, U>> function;

    public Try(ThrowingFunction<T, U> function) {
        Objects.requireNonNull(function);
        this.function = getFunction(function);
    }

    public Try(ThrowingFunction<T, U> function, U defaultVal) {
        Objects.requireNonNull(function);
        this.function = getFunctionWithDefault(function, defaultVal);
    }

    public <Y, Z> Try<T, Z> then(Try<U, Z> another) {
        Objects.requireNonNull(another);
        return new Try<>((ThrowingFunction<T, Z>) (t) -> {
            TryResult<T, U> result = this.tryIt(t);
            if (result.isSuccess()) {
                TryResult<U, Z> next = another.tryIt(result.getOutput());
                if (next.isSuccess()) {
                    return next.getOutput();
                } else {
                    throw next.getError();
                }
            } else {
                throw result.getError();
            }

        });
    }

    public <Y, Z> Try<T, Z> then(ThrowingFunction<U, Z> function) {
        return this.then(function);
    }

    public TryResult<T, U> tryIt(T input) {
        return this.function.apply(input);
    }

    public static <T, U> TryResult<T, U> doTry(T input, Try<T, U> trial) {
        Objects.requireNonNull(trial);
        return trial.tryIt(input);
    }

    public static <T, U> TryResult<T, U> doTry(T input, ThrowingFunction<T, U> function) {
        return doTry(input, new Try<>(function));
    }

    public TryResult<T, U> tryIt(T input, U defaultVal) {
        TryResult<T, U> result = this.function.apply(input);
        if (!result.isSuccess()) {
            return new TryResult<>(input, defaultVal);
        }
        return result;
    }

    public static <T, U> TryResult<T, U> doTry(T input, Try<T, U> trial, U defaultVal) {
        Objects.requireNonNull(trial);
        return trial.tryIt(input, defaultVal);
    }

    public static <T, U> TryResult<T, U> doTry(T input, ThrowingFunction<T, U> function, U defaultVal) {
        return doTry(input, new Try<>(function), defaultVal);
    }

    public static <T, U> U getResult(T input, Try<T, U> transformer, U defaultVal) {
        TryResult<T, U> result = Try.doTry(input, transformer);
        return result.isSuccess() ? result.getOutput() : defaultVal;
    }

    public static <T, U> U getResult(T input, ThrowingFunction<T, U> function, U defaultVal) {
        TryResult<T, U> result = Try.doTry(input, function);
        return result.isSuccess() ? result.getOutput() : defaultVal;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, U> {

        U apply(T input) throws Throwable;
    }

    private static <T, U> Function<T, TryResult<T, U>> getFunction(ThrowingFunction<T, U> function) {
        Objects.requireNonNull(function);
        return (input) -> {
            try {
                return new TryResult<>(input, function.apply(input));
            } catch (Throwable e) {
                return new TryResult<>(input, e);
            }
        };

    }

    private static <T, U> Function<T, TryResult<T, U>> getFunctionWithDefault(ThrowingFunction<T, U> function,
            U defaultVal) {
        Objects.requireNonNull(function);
        return (input) -> {
            try {
                return new TryResult<>(input, function.apply(input));
            } catch (Throwable e) {
                return new TryResult<>(input, defaultVal);
            }
        };

    }

    public static void main(String... args) {
        doTry(args[0], URI::new);
    }

}
