/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.propertymerger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class Main {

    public static void merge(String basePath, String overridePath, String outputPath) throws IOException {
        Properties baseProps = new Properties();
        Properties overrideProps = new Properties();

        System.out.println("Fichero de propiedades: " + basePath);
        try (InputStream baseIn = new FileInputStream(basePath);
             Reader baseReader = new InputStreamReader(baseIn, StandardCharsets.ISO_8859_1)) {
            baseProps.load(baseReader);
        }

        System.out.println("Fichero que sobreescribe las propiedades: " + overridePath);
        try (InputStream overrideIn = new FileInputStream(overridePath);
             Reader overrideReader = new InputStreamReader(overrideIn, StandardCharsets.ISO_8859_1)) {
            overrideProps.load(overrideReader);
        }

        // Sobrescribir las propiedades base con las del override
        for (String key : overrideProps.stringPropertyNames()) {
            baseProps.setProperty(key, overrideProps.getProperty(key));
        }

        System.out.println("application.description=" + baseProps.get("application.description"));
        
        
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.ISO_8859_1)) {
            // Claves ordenadas alfabéticamente
            TreeMap<String, String> sorted = new TreeMap<>();
            for (String key : baseProps.stringPropertyNames()) {
                sorted.put(key, baseProps.getProperty(key));
            }

            // Escribir manualmente
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        }

        System.out.println("Propiedades combinadas guardadas en: " + outputPath);
    }

    public static void main(String[] args) throws IOException {
        if (args.length!=3) {
            throw new RuntimeException("Uso: java PropertyMerger base.properties override.properties output.properties");
        }

        String basePath = args[0];
        String overridePath = args[1];
        String outputPath = args[2];

        merge(basePath, overridePath, outputPath);
    }
}
