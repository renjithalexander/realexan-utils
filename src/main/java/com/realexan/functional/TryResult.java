/**
 * 
 */
package com.realexan.functional;

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
public class TryResult<S> {

    private final S success;

    private final Throwable error;

    public TryResult(S success) {
        this(success, null);
    }

    public TryResult(Throwable error) {
        this(null, error);
    }

    private TryResult(S success, Throwable error) {
        this.success = success;
        this.error = error;
    }

    /**
     * @return the success
     */
    public S getSuccess() {
        return success;
    }

    /**
     * @return the error
     */
    public Throwable getError() {
        return error;
    }

    public boolean isSuccess() {
        return success != null;
    }

}
