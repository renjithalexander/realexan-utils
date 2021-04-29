package com.realexan.junit.utils;

import static com.realexan.common.FunctionalUtils.r2C;

import java.util.function.Consumer;

import org.junit.Assert;

/**
 * Junit utilities.
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
 *          <td>27-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class JUtils {

    public static Consumer<?> assertFail = r2C(Assert::fail);

    @SuppressWarnings("unchecked")
    public static <T> Consumer<T> failTest() {
        return (Consumer<T>) assertFail;
    }

}
