package com.educaflow.common.buildtools.files.tipoexpediente;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author logongas
 */
public class CommaSeparatedAdapter extends XmlAdapter<String, List<String>> {

    @Override
    public List<String> unmarshal(String v) {
        if (v == null || v.trim().isEmpty()) return new ArrayList<>();
        return Arrays.asList(v.split("\\s*,\\s*"));
    }

    @Override
    public String marshal(List<String> v) {
        if (v == null || v.isEmpty()) return "";
        return String.join(",", v);
    }
}
