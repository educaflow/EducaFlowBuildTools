package com.educaflow.common.buildtools.crap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calcula el CRAP (Change Risk Anti-Patterns) de cada método a partir del informe XML de JaCoCo y
 * rompe el build si alguno supera el umbral:
 *
 * <pre>
 *   CRAP(m) = CC(m)² × (1 − cobertura(m))³ + CC(m)
 * </pre>
 *
 * donde la CC (complejidad ciclomática) y la cobertura (de instrucciones) salen las dos de JaCoCo.
 * Un método sin tests dispara el CRAP con el cuadrado de su CC; uno con toda la cobertura se queda en
 * su CC. Se baja troceando el método o añadiéndole tests.
 *
 * <p>Escribe <b>siempre</b> los informes ({@link CrapReportWriter}) y después, si algún método
 * supera el umbral, lista esos métodos en stderr y termina con error.
 *
 * <p>Uso:
 * <pre>
 *   Main &lt;jacocoXml&gt; &lt;destino&gt; &lt;umbral&gt; &lt;origen&gt;
 * </pre>
 * donde {@code jacocoXml} es el informe XML de JaCoCo, {@code destino} la carpeta de los informes,
 * {@code umbral} el CRAP máximo permitido y {@code origen} la raíz de los fuentes
 * (p.ej. {@code ./src/main/java}), con la que se da la ruta del fichero de cada método.
 *
 * <p>Códigos de salida: {@code 0} ningún método supera el umbral, {@code 1} uso incorrecto,
 * {@code 2} error leyendo el informe de JaCoCo o escribiendo los informes y {@code 3} algún método
 * supera el umbral.
 *
 * @author logongas
 */
public class Main {

    private static final int SALIDA_OK = 0;
    private static final int SALIDA_USO_INCORRECTO = 1;
    private static final int SALIDA_ERROR_PROCESANDO = 2;
    private static final int SALIDA_UMBRAL_SUPERADO = 3;

    public static void main(String[] args) {
        if (args.length != 4) {
            salirConError(SALIDA_USO_INCORRECTO, "Uso: Main <jacocoXml> <destino> <umbral> <origen>");
        }

        Path jacocoXml = Paths.get(args[0]);
        Path destino = Paths.get(args[1]);
        double umbral = leerUmbral(args[2]);
        Path origen = Paths.get(args[3]).normalize();

        if (Files.exists(jacocoXml) == false) {
            salirConError(SALIDA_USO_INCORRECTO, "No existe el informe de JaCoCo " + jacocoXml);
        }

        List<MetricasMetodo> superanUmbral = null;
        try {
            List<MetricasMetodo> metricas = new JacocoReportReader(origen).leer(jacocoXml).stream()
                    .sorted(Comparator.comparingDouble(MetricasMetodo::crap).reversed())
                    .collect(Collectors.toList());
            superanUmbral = metricas.stream()
                    .filter(m -> m.crap() > umbral)
                    .collect(Collectors.toList());

            new CrapReportWriter().escribir(destino, metricas, superanUmbral, umbral);
            System.out.println("CRAP calculado para " + metricas.size() + " método(s). Informes en " + destino);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            salirConError(SALIDA_ERROR_PROCESANDO, ex.getMessage());
        }

        if (superanUmbral.isEmpty() == false) {
            StringBuilder mensaje = new StringBuilder();
            mensaje.append(superanUmbral.size()).append(" método(s) superan el CRAP máximo de ")
                    .append(CrapReportWriter.formatear(umbral)).append(":\n");
            for (MetricasMetodo m : superanUmbral) {
                mensaje.append("  - ").append(m.fichero()).append(":").append(m.linea())
                        .append(" ").append(m.nombreCorto())
                        .append(" CRAP=").append(CrapReportWriter.formatear(m.crap()))
                        .append(" CC=").append(m.cc())
                        .append(" cobertura=").append(CrapReportWriter.porcentaje(m.coberturaInstrucciones()))
                        .append("\n");
            }
            mensaje.append("Trocea el método para bajar su complejidad o añádele tests unitarios. Detalle en ")
                    .append(destino.resolve(CrapReportWriter.FICHERO_MD));
            salirConError(SALIDA_UMBRAL_SUPERADO, mensaje.toString());
        }

        System.exit(SALIDA_OK);
    }

    private static double leerUmbral(String valor) {
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException ex) {
            salirConError(SALIDA_USO_INCORRECTO, "El umbral no es un número: " + valor);
            return 0;
        }
    }

    private static void salirConError(int codigoSalida, String mensaje) {
        System.err.println("ERROR: " + mensaje);
        System.exit(codigoSalida);
    }
}
