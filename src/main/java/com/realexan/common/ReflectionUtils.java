package com.realexan.common;

import java.lang.reflect.Field;

import com.realexan.functional.trial.Try;
import com.realexan.functional.trial.TryResult;

/**
 * Utility functions for Reflection.
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
 *          <td>26-Apr-2021</td>
 *          <td><a href=
 *          "mailto:renjithalexander@gmail.com">renjithalexander@gmail.com</a></td>
 *          <td align="right">1</td>
 *          <td>Creation</td>
 *          </tr>
 *          </table>
 */
public class ReflectionUtils {

    /**
     * Returns TryResult which contains value of the field, with the field name in
     * the object.<br>
     * The TryResult will contain the error, if fetching the value failed.
     * 
     * @param fieldName the field name.
     * @param obj       the object for which the field is fetched.
     * @return the TryResult containing the field value.
     */
    public static TryResult<Void, Object> getField(String fieldName, Object obj) {
        return Try.doTry(null, (t) -> ReflectionUtils.getFieldRaw(fieldName, obj));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldRaw(String fieldName, Object obj)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

}
