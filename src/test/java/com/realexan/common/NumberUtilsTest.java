package com.realexan.common;

import static com.realexan.common.FunctionalUtils.forEach;
import static com.realexan.common.FunctionalUtils.forLoop;
import static com.realexan.common.NumberUtils.tryParse;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for NumberUtils.
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
 *          <td>29-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class NumberUtilsTest {
    /**
     * Tests the tryParse method for integer.
     * 
     * @throws Exception
     */
    @Test
    public void testIntegerParse() throws Exception {
        int[] passingVals = { 1, 100, Integer.MAX_VALUE, Integer.MIN_VALUE, 0 };
        String[] tests = new String[passingVals.length];

        forLoop(passingVals.length, i -> tests[i] = "" + passingVals[i]);
        forLoop(passingVals.length, i -> assertEquals(passingVals[i], tryParse(tests[i])));

        String[] failingTests = { "12" + Integer.MAX_VALUE, Integer.MIN_VALUE + "00", " ", " 10", null };
        forEach(failingTests, f -> assertEquals(-1, tryParse(f)));
    }

    /**
     * Tests the tryParse method for integer, with default values.
     * 
     * @throws Exception
     */
    @Test
    public void testIntegerParseWithDefault() throws Exception {
        String[] failingTests = { "12" + Integer.MAX_VALUE, Integer.MIN_VALUE + "00", " ", " 10", null };
        forEach(failingTests, f -> assertEquals(100, tryParse(f, 100)));
    }

}
