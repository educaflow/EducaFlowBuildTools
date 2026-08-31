package com.educaflow.common.buildtools.createstates;

import com.educaflow.common.buildtools.files.phaseeventmanagerfile.PhaseEventManagerFile;
import com.educaflow.common.buildtools.files.stateeventvalidator.StateEventValidatorFile;
import com.educaflow.common.buildtools.files.tipoexpediente.Fase;
import com.educaflow.common.buildtools.files.tipoexpediente.State;
import com.educaflow.common.buildtools.files.tipoexpediente.TipoExpedienteInstanceFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regla 8: los identificadores Java que el build va a emitir para un tipo de expediente no pueden
 * repetirse entre sí ni pisar un nombre reservado.
 *
 * <p>Se comprueban los identificadores <b>ya compuestos</b>, no los trozos: la conversión
 * UPPER_SNAKE_CASE → PascalCase no es inyectiva (con dígitos por medio {@code AB2C} y {@code AB_2C}
 * dan ambos {@code Ab2c}) y la colisión puede además <b>cruzar</b> estado y evento — el estado
 * {@code A_IN_EVENT_B} con el evento {@code C} y el estado {@code A} con el evento
 * {@code B_IN_EVENT_C} producen ambos {@code getForStateAInEventBInEventC} —, cosa que ninguna
 * comparación por pares detecta.
 *
 * <p>Queda fuera una sola parte de la regla: la reserva de los nombres de método público de las
 * clases base {@code PhaseEventManager}/{@code StateEventValidator}. El generador corre ANTES de compilar
 * secretaria-virtual y los build-tools no comparten código con el runtime, así que ahí esa lista solo
 * podría existir duplicada a mano; la comprueba un test (ApiBaseReservadaTest, en secretaria-virtual).
 */
public final class IdentificadoresGenerados {

    /** Reservados de los tipos anidados que no salen de los imports de la plantilla. */
    private static final Set<String> TIPOS_RESERVADOS_PROPIOS = Set.of(StatesFile.CLASS_NAME, "PhaseInternal");

    /**
     * Reservados de los campos de {@code States}: los que la propia plantilla declara.
     *
     * <p>Esta lista está escrita <b>a mano</b> y {@code MUST} revisarse si cambian los campos de
     * {@code states.template}. A diferencia de los tipos anidados —cuya reserva sí se deriva de los
     * imports de la plantilla, porque {@code import a.b.C;} es sintaxis rígida—, una declaración de
     * campo no lo es y un parser por regex fallaría en silencio ante, por ejemplo, un
     * {@code private static final List<Phase> phases}.
     */
    private static final Set<String> CAMPOS_RESERVADOS = Set.of("CODE", "NAME", "INSTANCE");

    private IdentificadoresGenerados() {
    }

    public static void check(TipoExpedienteInstanceFile tipo, Set<String> nombresImportadosPorLaPlantilla) {
        List<String> errores = new ArrayList<>();

        checkTiposAnidados(tipo, nombresImportadosPorLaPlantilla, errores);
        checkCampos(tipo, errores);
        checkMetodos(tipo, errores);

        if (errores.isEmpty() == false) {
            throw new RuntimeException("El tipo de expediente " + tipo.getCode() + " (" + tipo.getPath() + ")"
                    + " produce identificadores Java en conflicto:\n  - " + String.join("\n  - ", errores));
        }
    }

    /**
     * Un tipo anidado por fase: el PascalCase de su name.
     *
     * <p>El identificador del tipo anidado no solo compite con otros <b>tipos</b>: dentro de
     * {@code States} conviven también los <b>campos</b> que la plantilla declara ({@code CODE},
     * {@code NAME}, {@code INSTANCE}) y el campo alias {@code public static final Phase <NAME>} de
     * cada fase. Cuando un campo y un tipo anidado comparten identificador, el campo <b>obscurece</b>
     * al tipo (JLS 6.4.2/6.5.2) y dejan de compilar tanto {@code X.values()} como {@code X.ESTADO}.
     *
     * <p>Ocurre en dos casos reales, ninguno detectable comparando solo tipos entre sí:
     * <ul>
     *   <li>una fase de un solo segmento sin letras tras la inicial ({@code F1}, {@code A},
     *       {@code V2}) tiene {@code nameUpperCamelCase == name}, así que su propio campo alias
     *       obscurece a su propio enum;</li>
     *   <li>una fase {@code C_O_D_E} / {@code N_A_M_E} / {@code I_N_S_T_A_N_C_E} produce el tipo
     *       {@code CODE} / {@code NAME} / {@code INSTANCE}, que chocan con los campos de la plantilla.</li>
     * </ul>
     */
    private static void checkTiposAnidados(TipoExpedienteInstanceFile tipo, Set<String> importados, List<String> errores) {
        Map<String, String> vistos = new LinkedHashMap<>();

        Set<String> camposAlias = new LinkedHashSet<>();
        for (Fase fase : tipo.getFases()) {
            camposAlias.add(fase.getName());
        }

        for (Fase fase : tipo.getFases()) {
            String identificador = fase.getNameUpperCamelCase();

            if (TIPOS_RESERVADOS_PROPIOS.contains(identificador) || importados.contains(identificador)) {
                errores.add("la fase '" + fase.getName() + "' produce el tipo anidado '" + identificador
                        + "', que es un nombre reservado dentro de " + StatesFile.CLASS_NAME + " (la clase misma,"
                        + " su enum interno o uno de los tipos que la plantilla importa: " + importados + ")."
                        + " Un tipo anidado con ese nombre haría shadowing del import y rompería la compilación."
                        + " " + comoSalir(fase));
            }

            if (CAMPOS_RESERVADOS.contains(identificador) || camposAlias.contains(identificador)) {
                errores.add("la fase '" + fase.getName() + "' produce el tipo anidado '" + identificador
                        + "', que coincide con un CAMPO de " + StatesFile.CLASS_NAME + " (los de la plantilla "
                        + CAMPOS_RESERVADOS + " o el alias 'public static final Phase' de una fase: "
                        + camposAlias + "). El campo obscurece al tipo (JLS 6.4.2/6.5.2), así que no"
                        + " compilarían ni " + identificador + ".values() ni " + identificador + ".<ESTADO>."
                        + " " + comoSalir(fase));
            }

            String previa = vistos.put(identificador, fase.getName());
            if (previa != null) {
                errores.add("las fases '" + previa + "' y '" + fase.getName() + "' producen el mismo tipo"
                        + " anidado '" + identificador + "'. " + comoSalir(fase));
            }
        }
    }

    /** La salida que el autor tiene que aplicar, para que el fail-fast no se perciba como arbitrario. */
    private static String comoSalir(Fase fase) {
        return "Renombra la fase a algo cuyo PascalCase no coincida con su propio nombre ni con un"
                + " reservado (p. ej. 'FASE_1' en vez de '" + fase.getName() + "', que da el tipo 'Fase1').";
    }

    /** Un campo public static final Phase por fase, con el name tal cual. */
    private static void checkCampos(TipoExpedienteInstanceFile tipo, List<String> errores) {
        for (Fase fase : tipo.getFases()) {
            if (CAMPOS_RESERVADOS.contains(fase.getName())) {
                errores.add("la fase '" + fase.getName() + "' produce un campo de States con un nombre"
                        + " reservado (" + CAMPOS_RESERVADOS + ").");
            }
        }
        // El duplicado de name de fase ya lo cubre checkFases del finder (regla 2); los demás campos
        // de la clase (phases, statesByPhase, allStates) van en camelCase a propósito y un name de
        // fase es UPPER_SNAKE_CASE, así que no pueden colisionar.
    }

    /**
     * Los nombres de método de esqueletos y tests, compuestos y agrupados POR FASE: cada fase tiene
     * su propio PhaseEventManagerImpl y su propio StateEventValidatorImpl, así que la colisión es dentro
     * de la fase, no del tipo.
     */
    private static void checkMetodos(TipoExpedienteInstanceFile tipo, List<String> errores) {
        for (Fase fase : tipo.getFases()) {
            Map<String, String> vistos = new LinkedHashMap<>();

            for (State state : fase.getStates()) {
                anota(vistos, PhaseEventManagerFile.getMethodNameOnEnterEvent(state.getNameUpperCamelCase()),
                        "el estado '" + state.getName() + "'", fase, errores);
            }

            for (String evento : fase.getEventsUpperCamelCase()) {
                anota(vistos, PhaseEventManagerFile.getMethodNameTriggerEvent(evento),
                        "el evento '" + evento + "'", fase, errores);
            }

            for (State state : fase.getStates()) {
                for (String evento : state.getEventsUpperCamelCase()) {
                    anota(vistos, StateEventValidatorFile.getMethodNameBeanValidationRules(
                                    state.getNameUpperCamelCase(), evento),
                            "la pareja estado '" + state.getName() + "' / evento '" + evento + "'",
                            fase, errores);
                }
            }
        }
    }

    private static void anota(Map<String, String> vistos, String metodo, String procedencia, Fase fase, List<String> errores) {
        String previa = vistos.put(metodo, procedencia);
        if (previa != null) {
            errores.add("en la fase '" + fase.getName() + "', " + previa + " y " + procedencia
                    + " producen el mismo nombre de método '" + metodo + "'.");
        }
    }
}
