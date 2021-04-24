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

    public <Y, Z> Try<T, Z> then(Try<U, Z> another, Try<U, Z> onFailure) {
        Objects.requireNonNull(another);
        Objects.requireNonNull(onFailure);
        return new Try<>((ThrowingFunction<T, Z>) (t) -> {
            TryResult<T, U> result = this.tryIt(t);
            Try<U, Z> nextRun = result.isSuccess() ? another : onFailure;

            TryResult<U, Z> next = nextRun.tryIt(result.getOutput());
            if (next.isSuccess()) {
                return next.getOutput();
            } else {
                throw next.getError();
            }

        });
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
        return this.then(new Try<>(function));
    }

    public Try<T, U> thenTest(TestFunction<U> function) {
        Objects.requireNonNull(function);
        Try<U, Void> checkTry = getTry(function);
        return new Try<T, U>((ThrowingFunction<T, U>) (t) -> {
            TryResult<T, U> result = this.tryIt(t);
            if (result.isSuccess()) {
                TryResult<U, Void> next = checkTry.tryIt(result.getOutput());
                if (next.isSuccess()) {
                    return result.getOutput();
                } else {
                    throw next.getError();
                }
            } else {
                throw result.getError();
            }
        });
    }

    public Try<T, U> onError(Try<U, U> another) {
        Objects.requireNonNull(another);
        return new Try<>((ThrowingFunction<T, U>) (t) -> {
            TryResult<T, U> result = this.tryIt(t);
            if (!result.isSuccess()) {
                TryResult<U, U> next = another.tryIt(result.getOutput());
                if (next.isSuccess()) {
                    return next.getOutput();
                } else {
                    throw next.getError();
                }
            }
            return result.getOutput();

        });
    }

    public TryResult<T, U> tryIt(T input) {
        return this.function.apply(input);
    }

    public static <T, U> Try<T, U> getTry(ThrowingFunction<T, U> function) {
        return new Try<>(function);
    }

    public static <T, U> TryResult<T, U> doTry(T input, Try<T, U> trial) {
        Objects.requireNonNull(trial);
        return trial.tryIt(input);
    }

    public static <T, U> TryResult<T, U> doTry(T input, ThrowingFunction<T, U> function) {
        return doTry(input, getTry(function));
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
        return doTry(input, getTry(function), defaultVal);
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

    @FunctionalInterface
    public interface TestFunction<T> extends ThrowingFunction<T, Void> {

        Void apply(T input) throws Throwable;
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

    public static void main(String... args) {
        doTry(args[0], URI::new);
    }

}
