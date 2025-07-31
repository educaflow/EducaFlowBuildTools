package com.educaflow.common.buildtools.i18nprocessor.generatefile;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author logongas
 */
public class TextoTraducibleRepository {

    public static List<TextoTraducible> readFromI18NFile(Path path) {
        try {
            List<TextoTraducible> textosTraducibles;

            try (Reader reader = new FileReader(path.toFile())) {
                textosTraducibles = new CsvToBeanBuilder<TextoTraducible>(reader)
                        .withType(TextoTraducible.class)
                        .build()
                        .parse();
            }

            return textosTraducibles;

        } catch (Exception ex) {
            throw new RuntimeException(path.toString(), ex);
        }
    }

    public static void writeToI18NFile(List<TextoTraducible> textosTraducibles, Path filePath) {
        try {
            File file = filePath.toFile();
            if (file.exists()) {
                file.delete();
            }

            ColumnPositionMappingStrategy<TextoTraducible> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(TextoTraducible.class);
            String[] columnas = {"key", "message", "comment", "context"};
            strategy.setColumnMapping(columnas);

            try (Writer writer = new FileWriter(filePath.toFile())) {
                writer.append(String.join(",", columnas));
                writer.append("\n");

                StatefulBeanToCsv<TextoTraducible> beanToCsv = new StatefulBeanToCsvBuilder<TextoTraducible>(writer)
                        .withMappingStrategy(strategy)
                        .withApplyQuotesToAll(true)
                        .withSeparator(',')
                        .build();
                beanToCsv.write(textosTraducibles);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
