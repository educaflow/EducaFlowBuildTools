/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.common;

import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author logongas
 */
public class TextUtil {
    public static String caseLowerFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static List<String>  getUpperCamelCase(List<? extends Object> list) {
        List<String> upperCamelCaseList = new ArrayList<>();

        for (Object obj : list) {
            upperCamelCaseList.add(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, obj + ""));
        }

        return upperCamelCaseList;

    }
}
