/**
 * 
 */
package com.realexan.trial;

import java.util.function.Consumer;

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
public class TryResult<T, U> {

    private final T input;

    private final U output;

    private final Throwable error;

    public TryResult(T input, U success) {
        this(input, success, null);
    }

    public TryResult(T input, Throwable error) {
        this(input, null, error);
    }

    protected TryResult(T input, U success, Throwable error) {
        this.input = input;
        this.output = success;
        this.error = error;
    }

    /**
     * @return the output
     */
    public U getOutput() {
        return output;
    }

    public U getOutput(U ifFailed) {
        if (!isSuccess()) {
            return ifFailed;
        }
        return this.output;
    }

    /**
     * @return the error
     */
    public Throwable getError() {
        return error;
    }

    public T getInput() {
        return input;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public void then(Consumer<TryResult<T, U>> c) {
        c.accept(this);
    }

    public TryResult<T, U> ifSucceeded(Consumer<U> c) {
        if (isSuccess()) {
            c.accept(output);
        }
        return this;
    }

    public TryResult<T, U> ifFailed(Consumer<Throwable> c) {
        if (!isSuccess()) {
            c.accept(error);
        }
        return this;
    }

    public TryResult<T, U> ifSucceededRun(Runnable r) {
        if (isSuccess()) {
            r.run();
        }
        return this;
    }

    public TryResult<T, U> ifFailedRun(Runnable r) {
        if (!isSuccess()) {
            r.run();
        }
        return this;
    }

}
