package com.realexan.common;

import com.realexan.trial.Try;

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
