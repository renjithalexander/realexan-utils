/**
 * 
 */
package com.realexan.functional;

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
public class Functional {

    public static int tryParse(String s) {
        return tryParse(s, -1);
    }

    public static int tryParse(String s, int defaultVal) {
        return Try.tryOn(s, Integer::parseInt, new Integer(defaultVal));
    }

    public static <T, U> U tryDo(T input, Function<T, U> transformer, U defaultVal) {
        return Try.tryOn(input, transformer::apply, defaultVal);
    }

}
