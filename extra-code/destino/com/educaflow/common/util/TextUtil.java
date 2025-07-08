package com.educaflow.common.util;

public class TextUtil {
    public static String getHumanCaseFromScreamingSnakeCase(String screamingSnakeCase) {
        if (screamingSnakeCase == null || screamingSnakeCase.isEmpty()) {
            return screamingSnakeCase;
        }
        StringBuilder humanCase = new StringBuilder();

        String[] parts = screamingSnakeCase.split("_");

        for (String part : parts) {
            if (humanCase.length()>0) {
                humanCase.append(" ");
                humanCase.append(part.toLowerCase());
            } else {
                humanCase.append(toLowerCaseExceptFirstsLetter(part));
            }
        }

        return humanCase.toString();
    }

    public static String toLowerCaseExceptFirstsLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String toFirstsLetterToLowerCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    public static String toFirstsLetterToUpperCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
