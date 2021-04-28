package com.realexan.common;

import com.realexan.trial.Try;

/**
 * Utilities for numerical operations.
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
public class NumberUtils {

    /**
     * Try to parse the string to integer, and returns the result. If fails, returns
     * -1;
     * 
     * @param s the String passed.
     * @return the parsed value or -1 if parsing failed.
     */
    public static int tryParse(String s) {
        return tryParse(s, -1);
    }

    /**
     * Try to parse the string to integer, and returns the result. If fails, returns
     * default value;
     * 
     * @param s          the String passed.
     * @param defaultVal the default value
     * @return the parsed value or default value if parsing failed.
     */
    public static int tryParse(String s, int defaultVal) {
        return Try.getResult(s, Integer::parseInt, defaultVal);
    }

}
