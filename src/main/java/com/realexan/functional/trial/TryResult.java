/**
 * 
 */
package com.realexan.functional.trial;

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
public class TryResult<T, S> {

    private final T input;

    private final S output;

    private final Throwable error;

    public TryResult(T input, S success) {
        this(input, success, null);
    }

    public TryResult(T input, Throwable error) {
        this(input, null, error);
    }

    private TryResult(T input, S success, Throwable error) {
        this.input = input;
        this.output = success;
        this.error = error;
    }

    /**
     * @return the output
     */
    public S getOutput() {
        return output;
    }

    public S getOutput(S ifFailed) {
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
        return output != null;
    }

    public void then(Consumer<TryResult<T, S>> c) {
        c.accept(this);
    }

    public void thenOnSuccess(Consumer<S> c) {
        if (isSuccess()) {
            c.accept(output);
        }
    }

    public void thenOnFailure(Consumer<Throwable> c) {
        if (!isSuccess()) {
            c.accept(error);
        }
    }

}
