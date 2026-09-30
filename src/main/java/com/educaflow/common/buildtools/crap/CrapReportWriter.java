package com.educaflow.common.buildtools.crap;

import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Escribe los dos informes de CRAP, ambos con los métodos ordenados de mayor a menor CRAP:
 * <ul>
 *   <li>{@code crap.csv}: una fila por método con todas las métricas. Es el formato para leer desde
 *       otras herramientas (y desde los agentes de IA).</li>
 *   <li>{@code crap.md}: resumen para personas con los métodos que superan el umbral.</li>
 * </ul>
 *
 * @author logongas
 */
public class CrapReportWriter {

    public static final String FICHERO_CSV = "crap.csv";
    public static final String FICHERO_MD = "crap.md";

    private static final String[] CABECERA_CSV = {
        "fichero", "linea", "clase", "metodo", "descriptor", "cc", "coberturaInstrucciones", "crap",
        "instruccionesCubiertas", "instruccionesTotales", "lineasCubiertas", "lineasTotales",
        "ramasCubiertas", "ramasTotales"
    };

    public void escribir(Path destino, List<MetricasMetodo> metricas, List<MetricasMetodo> superanUmbral, double umbral) throws IOException {
        Files.createDirectories(destino);
        escribirCsv(destino.resolve(FICHERO_CSV), metricas);
        escribirMd(destino.resolve(FICHERO_MD), metricas.size(), superanUmbral, umbral);
    }

    private void escribirCsv(Path path, List<MetricasMetodo> metricas) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             CSVWriter csv = new CSVWriter(writer)) {
            csv.writeNext(CABECERA_CSV, false);
            for (MetricasMetodo m : metricas) {
                csv.writeNext(new String[]{
                    m.fichero(),
                    String.valueOf(m.linea()),
                    m.clase(),
                    m.metodo(),
                    m.descriptor(),
                    String.valueOf(m.cc()),
                    formatear(m.coberturaInstrucciones()),
                    formatear(m.crap()),
                    String.valueOf(m.instruccionesCubiertas()),
                    String.valueOf(m.instruccionesTotales()),
                    String.valueOf(m.lineasCubiertas()),
                    String.valueOf(m.lineasTotales()),
                    String.valueOf(m.ramasCubiertas()),
                    String.valueOf(m.ramasTotales())
                }, false);
            }
        }
    }

    private void escribirMd(Path path, int totalMetodos, List<MetricasMetodo> superanUmbral, double umbral) throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# CRAP por método\n\n");
        md.append("`CRAP = CC² × (1 − cobertura de instrucciones)³ + CC`. Umbral: ").append(formatear(umbral))
                .append(". Métodos analizados: ").append(totalMetodos).append(".\n\n");
        md.append("Todas las métricas de todos los métodos están en `").append(FICHERO_CSV).append("`.\n\n");

        if (superanUmbral.isEmpty()) {
            md.append("Ningún método supera el umbral.\n");
        } else {
            md.append(superanUmbral.size()).append(" método(s) superan el umbral:\n\n");
            md.append("| CRAP | CC | Cobertura | Método | Fichero |\n");
            md.append("|---:|---:|---:|---|---|\n");
            for (MetricasMetodo m : superanUmbral) {
                md.append("| ").append(formatear(m.crap()))
                        .append(" | ").append(m.cc())
                        .append(" | ").append(porcentaje(m.coberturaInstrucciones()))
                        .append(" | `").append(m.nombreCorto()).append("`")
                        .append(" | `").append(m.fichero()).append(":").append(m.linea()).append("` |\n");
            }
        }
        Files.writeString(path, md, StandardCharsets.UTF_8);
    }

    static String formatear(double valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }

    static String porcentaje(double fraccion) {
        return String.format(Locale.ROOT, "%.0f%%", fraccion * 100);
    }
}
