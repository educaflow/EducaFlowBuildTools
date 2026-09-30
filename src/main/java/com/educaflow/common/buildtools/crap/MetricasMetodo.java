package com.educaflow.common.buildtools.crap;

/**
 * La complejidad ciclomática, la cobertura y el CRAP de un método, tal y como salen del informe XML
 * de JaCoCo. La CC y la cobertura vienen de la misma herramienta y del mismo bytecode, así que no
 * pueden discrepar entre sí.
 *
 * <p>La cobertura con la que se calcula el CRAP es la de <b>instrucciones</b>: es la más fina y está
 * definida en cualquier método con código, también en los que no tienen ramas. Las de líneas y ramas
 * se guardan solo como información.
 *
 * @param fichero ruta del fuente relativa al directorio de trabajo (p.ej. {@code ./src/main/java/com/educaflow/.../Foo.java})
 * @param linea primera línea del método, o {@code 0} si JaCoCo no la conoce
 * @param clase nombre completo de la clase con puntos (las internas con {@code $})
 * @author logongas
 */
public record MetricasMetodo(
        String fichero,
        int linea,
        String clase,
        String metodo,
        String descriptor,
        int cc,
        int instruccionesCubiertas,
        int instruccionesTotales,
        int lineasCubiertas,
        int lineasTotales,
        int ramasCubiertas,
        int ramasTotales) {

    /** Fracción de instrucciones cubiertas por los tests, entre 0 y 1. */
    public double coberturaInstrucciones() {
        return instruccionesTotales == 0 ? 0 : (double) instruccionesCubiertas / instruccionesTotales;
    }

    /** {@code CRAP(m) = CC(m)² × (1 − cobertura(m))³ + CC(m)}. */
    public double crap() {
        double descubierto = 1 - coberturaInstrucciones();
        return (double) cc * cc * descubierto * descubierto * descubierto + cc;
    }

    /** {@code Clase.metodo} con el nombre simple de la clase, para los mensajes. */
    public String nombreCorto() {
        return clase.substring(clase.lastIndexOf('.') + 1) + "." + metodo;
    }
}
