package com.realexan.common;

import java.lang.reflect.Field;

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
    public static <T> T getField(Object obj, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field f = findField(clazz, fieldName);
        f.setAccessible(true);
        return (T) f.get(obj);

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
    public static void setField(Object obj, String fieldName, Object fieldValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field f = findField(clazz, fieldName);
        f.setAccessible(true);
        f.set(obj, fieldValue);
    }

    /**
     * Finds the field from the Class supplied, or any of its concrete ancestor
     * classes.
     * 
     * @param clazz     the class in which the field has to be searched for.
     * @param fieldName the name of the field.
     * @return a Field object corresponding to the fieldName.
     * @throws NoSuchFieldException if no field with the field name found in the
     *                              hierarchy.
     */
    public static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field field = null;
        NoSuchFieldException nsfe = null;
        // search in the entire hierarchy.
        do {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                // Keep the first exception to be thrown later, if the field is not found in the
                // hierarchy.
                if (nsfe == null) {
                    nsfe = e;
                }
                // TODO extend it for interfaces too.
                clazz = clazz.getSuperclass();
            }
        } while (clazz != null);
        // If the field not found in the hierarchy.
        if (field == null && nsfe != null) {
            throw nsfe;
        }
        return field;
    }

}
