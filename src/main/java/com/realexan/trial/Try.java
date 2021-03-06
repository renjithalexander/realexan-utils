/**
 * 
 */
package com.realexan.trial;

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

import com.realexan.util.function.ThrowingFunction;
import com.realexan.util.function.ThrowingRunnable;
import com.realexan.util.function.ThrowingSupplier;

/**
 * Mimicking Try-Catch.
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

    /**
     * Returns a Try function composed of the ThrowingFunction. Try.tryIt(input)
     * will execute the function and returns a TryResult.
     * 
     * @param function the ThrowingFunction.
     */
    public Try(ThrowingFunction<T, U> function) {
        Objects.requireNonNull(function);
        this.function = input -> {
            try {
                return new TryResult<>(input, function.apply(input));
            } catch (Throwable e) {
                return new TryResult<>(input, e);
            }
        };
    }

    /**
     * Returns a Try function which composes a Try function which runs once this Try
     * succeeds and another which runs once this fails.
     * 
     * @param <Z>       The final output type after the composed Try functions are
     *                  run.
     * @param another   the Try function that would be run on success of this Try.
     * @param onFailure the Try function that would be execute once this Try
     *                  function fails.
     * @return a Try
     */
    public <Z> Try<T, Z> then(Try<U, Z> another, Try<U, Z> onFailure) {
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

    /**
     * Returns a Try function which composes a Try function which runs once this Try
     * succeeds.
     * 
     * @param <Z>     The final output type after the composed Try functions are
     *                run.
     * @param another the Try function that would be run on success of this Try.
     * @return a Try
     */
    public <Z> Try<T, Z> then(Try<U, Z> another) {
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

    /**
     * Composes a Try with the ThrowingFunction to be executed on the success of
     * this try function.
     * 
     * @param <Z>      the final return type.
     * @param function the ThrowingFunction to be executed if this Try succeeds.
     * @return a Try.
     */
    public <Z> Try<T, Z> then(ThrowingFunction<U, Z> function) {
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

    public TryResult<T, U> tryIt(T input, U defaultVal) {
        TryResult<T, U> result = this.function.apply(input);
        if (!result.isSuccess()) {
            return new TryResult<>(input, defaultVal);
        }
        return result;
    }

    public static <T, U> Try<T, U> getTry(ThrowingFunction<T, U> function) {
        return new Try<>(function);
    }

    public static <T, U> TryResult<T, U> doTry(T input, ThrowingFunction<T, U> function) {
        try {
            return new TryResult<>(input, function.apply(input));
        } catch (Throwable e) {
            // e.printStackTrace();
            return new TryResult<>(input, e);
        }
    }

    public static TryRunnableResult doTry(ThrowingRunnable runnable) {
        try {
            runnable.execute();
            return new TryRunnableResult(null);
        } catch (Throwable t) {
            return new TryRunnableResult(t);
        }

    }

    public static <U> TryResult<Void, U> doTry(ThrowingSupplier<U> supplier) {
        try {
            return new TryResult<Void, U>(null, supplier.get());
        } catch (Throwable t) {
            return new TryResult<Void, U>(null, null, t);
        }
    }

    public static <T, U> TryResult<T, U> doTry(T input, Try<T, U> trial, U defaultVal) {
        Objects.requireNonNull(trial);
        return trial.tryIt(input, defaultVal);
    }

    public static <T, U> TryResult<T, U> doTry(T input, ThrowingFunction<T, U> function, U defaultVal) {
        return doTry(input, getTry(function), defaultVal);
    }

    public static <T, U> U getResult(T input, ThrowingFunction<T, U> function, U defaultVal) {
        TryResult<T, U> result = Try.doTry(input, function);
        return result.isSuccess() ? result.getOutput() : defaultVal;
    }

    @FunctionalInterface
    public interface TestFunction<T> extends ThrowingFunction<T, Void> {

        Void apply(T input) throws Throwable;
    }

    public static void main(String... args) {
        doTry(args[0], URI::new);
    }

}
