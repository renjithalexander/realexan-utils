package com.realexan.common;

import java.lang.reflect.Field;

import com.realexan.trial.Try;
import com.realexan.trial.TryResult;
import com.realexan.trial.TryRunnableResult;
import com.realexan.util.function.ThrowingFunction;

import static com.realexan.common.FunctionalUtils.*;

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
     * @param obj       the object for which the field is fetched.
     * @param fieldName the field name.
     * 
     * @return the TryResult containing the field value.
     */
    public static <T> FieldValue<Void, T> getField(Object obj, String fieldName) {
        return new FieldValue<Void, T>(
                Try.doTry(null, (ThrowingFunction<Void, T>) (t) -> ReflectionUtils.getFieldRaw(obj, fieldName)));
    }

    /**
     * Returns the value of the field for the object passed.
     * 
     * @param <T>       the type of the value.
     * @param obj       the object from which the field value is to be extracted.
     * @param fieldName the field name.
     * @return the value of the object.
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldRaw(Object obj, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    /**
     * Sets the field with fieldName of the object, to field value.
     * 
     * @param obj        the object for which the field has to be set.
     * @param fieldName  the name of the field.
     * @param fieldValue the value of the field.
     * @return the result
     */
    public static TryRunnableResult setField(Object obj, String fieldName, String fieldValue) {
        return Try.doTry(() -> setFieldRaw(obj, fieldName, fieldValue));
    }

    /**
     * Sets the value to the field specified by field name, for the object passed.
     * 
     * @param obj        the object for which the field has to be set.
     * @param fieldName  the name of the field.
     * @param fieldValue the value to be set to the field.
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void setFieldRaw(Object obj, String fieldName, Object fieldValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(obj, fieldValue);
    }

    /**
     * The return type for getField.
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
    public static class FieldValue<T, U> extends TryResult<T, U> {

        private FieldValue(TryResult<T, U> result) {
            super(result.getInput(), result.getOutput(), result.getError());
        }

        /**
         * Returns the value.
         * 
         * @param <V> the type of the value.
         * @return return the value;
         */
        public <V> V value() {
            return cast(super.getOutput());
        }
    }

}
