package com.realexan.trial;

/**
 * The result of running a ThrowingRunnable.
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
 *          <td>28-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class TryRunnableResult extends TryResult<Void, Void> {
    
    //public static final TryRunnableResult SUCCESS = new TryRunnableResult(null);

    /**
     * Constructor.
     * 
     * @param t the error, if any, which occred while running the ThrowingRunnable.
     */
    public TryRunnableResult(Throwable t) {
        super(null, t);
    }

}
