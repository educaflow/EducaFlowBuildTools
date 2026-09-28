# CLAUDE.md

Este fichero proporciona orientación a Claude Code (claude.ai/code) cuando trabaja con el código de este repositorio.

## Qué es este proyecto

`EducaFlowBuildTools` es una librería Java construida con Maven (JAR, `com.educaflow:EducaFlowBuildTools:1.0-SNAPSHOT`) que proporciona **generadores y procesadores de código de tiempo de compilación** para la aplicación `secretaria-virtual`. `secretaria-virtual` es un módulo [Axelor](https://axelor.com) (plataforma JEE low-code); su build de Gradle declara este JAR bajo una configuración personalizada `educaFlowBuildToolsDependency` e invoca la clase `Main` de cada herramienta como una tarea `JavaExec` integrada en el ciclo de vida del build de Axelor.

Este repositorio no produce **código de ejecución para la aplicación** — cada punto de entrada es un `main(String[] args)` autónomo que lee/escribe ficheros durante el build de `secretaria-virtual`. No hay tests unitarios (`src/test/java` está vacío).

## Compilar e instalar

```bash
mvn clean install          # compila + instala el JAR en el repositorio Maven local (~/.m2)
./install.sh               # lo mismo (simplemente ejecuta `mvn clean install`)
```

Tras cambiar cualquier herramienta de aquí, debes ejecutar `mvn install` para que el build de Gradle de `secretaria-virtual` recoja el nuevo JAR del repositorio Maven local. No hay ningún otro paso de publicación.

- El compilador Maven usa `release` **21** (`pom.xml`), igual que el consumidor (`secretaria-virtual`), que se ejecuta sobre **Java 21**. Puedes usar sintaxis y API de Java 21 (records, `switch` con patrones, text blocks…).
- `-parameters` está activado (se conservan los nombres de parámetros; algunas herramientas dependen de reflexión).

### Ejecutar una única herramienta a mano

Cada tarea es `java -cp <jar+deps> <mainClass> <args...>`. El orden de los argumentos coincide con el `build.gradle` de `secretaria-virtual` (`../secretaria-virtual/build.gradle`), que es la fuente de la verdad sobre cómo se invocan. La mayoría reciben `<rutaOrigen> <rutaDestino>`, donde origen es `./src/main/java` y destino es algún directorio bajo `./build/resources/main` o `./build/src-gen/main/java`.

## Los puntos de entrada (`*/Main.java`)

Cada paquete bajo `com.educaflow.common.buildtools` es una herramienta con su `Main`. Este es el núcleo del proyecto — entiéndelos primero.

> **El `[paqueteRaiz]` de los trámites**: todas las herramientas que descubren trámites o tipos de expediente aceptan como **último argumento, opcional**, el paquete raíz de los trámites (con puntos), por omisión `com.educaflow.tramites`. Ver [Dónde viven los trámites](#dónde-viven-los-trámites-tramiteslayout).

**NO integrada en el build — se lanza a mano (o desde un agente de IA):**
- `createfiles.Main` (`<origen> [paqueteRaiz] [--tipo=<ruta>] [--fase=<FASE>]`) — para cada `TipoExpediente`, **genera los ficheros fuente que faltan** a partir de plantillas Pebble, con el layout **por fase**: en la raíz de la versión, `domains.xml` de Axelor y el `views.xml` del form de plantillas; y por **cada fase**, su subcarpeta `<vN>/<fase en minúsculas>/` con `PhaseEventManagerImpl.java`, `StateEventValidatorImpl.kt` y su `views.xml`. **Solo genera: no valida nada.** Imprime una línea `CREADO <ruta>` por fichero creado y un resumen final.
  - **No cuelga de `generateCode`**: la tarea `CreateFilesTask` sigue registrada en `secretaria-virtual/build.gradle` pero se invoca a mano al crear un tipo de expediente, `./gradlew -q CreateFilesTask -Ptipo=<carpeta del tipo>`, no en cada build. Así el build no escribe en `src/main/java`. Los cuatro ficheros se versionan en git, de modo que solo faltan en la ventana entre escribir el `TipoExpedienteInstance.xml` y generarlos; si se compila en ese hueco, `richdomainclass.MainModelXml` aborta diciendo qué `domains.xml` falta.
  - `--fase=<FASE>` acota además a una sola fase, que es la receta para **añadir una fase nueva** a un tipo ya existente sin tocar las demás: con `--fase` no se generan los ficheros de la raíz de la versión, porque no son de ninguna fase. Si la fase no existe, falla diciendo cuáles hay. Lo traslada el `-Pfase` de la tarea de Gradle.
  - `--tipo=<ruta>` acota la generación a un único tipo de expediente (la ruta de su `TipoExpedienteInstance.xml` o la de la carpeta que lo contiene); sin esa opción se procesan todos. Es lo que traslada el `-Ptipo` de la tarea de Gradle. Si el filtro no casa con ningún tipo, falla en vez de callarse.
  - Códigos de salida: `0` todo bien (se hayan creado ficheros o no), `1` uso incorrecto (argumentos inválidos o `--tipo=` sin coincidencias), `2` error procesando los tipos (XML inválido, error de E/S…). Gradle los colapsa todos a «build fallido», pero el mensaje de error va a stderr y se ve igual; los códigos siguen sirviendo si se invoca la clase directamente con `java -cp`.
  - **La validación de que el código escrito a mano concuerda con la máquina de estados ya no vive aquí**: está en los tests de `secretaria-virtual`, en `src/test/java/com/educaflow/tiposexpedientes`. Al leer **bytecode** en vez de código fuente alcanzan también al `StateEventValidator`, que es Kotlin y que Spoon nunca pudo parsear. Si cambias el convenio de nombres o las plantillas de esta herramienta, esos tests son lo que hay que mirar.

**Integradas en el `generateCode` de Axelor (se ejecutan *antes* de que Axelor genere el código de las entidades):**
- `richdomainclass.MainModelXml` (`<origen> <destinoSrcGen> [paqueteRaiz]`) — enriquece el **XML** de dominio antes de que Axelor lo lea (`addExtraCodeToDomainXml`), usando `extra-code-domain-xml.template`.
- `richdomainclass.Main` (`<origen> <destinoSrcGen> [paqueteRaiz]`) — enriquece la **clase Java** de dominio generada (`addExtraCodeToDomainClass`), inyectando el `<extra-code-model>` que declare el `domains.xml`. Los enums de estados/eventos/perfiles que antes emitía aquí desaparecieron con la clase `States` (`extra-code-domain.template` quedó a 0 bytes). No usa AST: inserta el texto renderizado justo antes de la última `}` del fichero. Escribe en `build/src-gen/main/java`.

**Integrada en la compilación (`compileJava`/`compileKotlin` dependen de ella):**
- `createstates.Main` (`<origen> <destinoSrcGen> [paqueteRaiz] [rutaDominioTipoExpediente]`) — la tarea
  `GenerateStatesTask` del `build.gradle`. Proyecta el `TipoExpedienteInstance.xml` de cada tipo en **una** clase
  `<basePackageName>.States`, que emite en `build/src-gen-states/main/java` (un `srcDir` propio, para no mezclarla
  con lo que genera Axelor). Es la **única** fuente de la máquina de estados en runtime: la clase lleva un enum
  público por fase (sus constantes son los estados, con título, perfil, eventos y `closed`), un alias
  `Phase` por fase, `CODE`/`NAME` y un `INSTANCE` que implementa `TipoExpedienteStates`. **No se versiona ni se
  edita**: se reemite en cada build.
  - Antes de escribir nada **valida fail-fast**: `ProfilesDelDominio` comprueba que cada `profile` no vacío existe
    en el enum global `Profile` del dominio (la garantía que antes daba el data-init de la entidad
    `EstadoTipoExpediente`, hoy borrada), e `IdentificadoresGenerados` comprueba que los identificadores Java
    compuestos no colisionan entre sí ni con nombres reservados de `States`. La reserva de **tipos anidados** se
    deriva de los `import` de `states.template` (sintaxis rígida, no hay que duplicarla a mano); la de **campos**
    está escrita a mano en la propia clase, porque una declaración de campo no es parseable por regex sin fallar
    en silencio.

**Integradas en `processResources` (`dependsOn`, se ejecutan *antes* de copiar los recursos):**
- `xml2pdf.Main` (`<origen> <destino> [rutaExecTraductor] [paqueteRaiz]`) — **resuelve los XML de definición de los documentos PDF de los trámites** (raíz `<documentoFormulario>` o `<documentoTexto>`, en carpetas `documentospdf`/`documentos`, sin prefijo `_`) buscándolos bajo el paquete raíz de los trámites y escribiendo con `DocumentoXmlResolver` el XML resuelto (autocontenido: includes expandidos, título del trámite, valenciano traducido, sufijo `__!!` quitado, estructura de 12 columnas validada) en `build/src-gen/main/resources` con la misma ruta de paquete y el mismo nombre `.xml` (así queda en el classpath sin versionarse en git). **El PDF NO se genera aquí**: lo dibuja la aplicación en runtime (`com.educaflow.base.infrastructure.pdfgenerator` de secretaria-virtual, con iText), porque los atributos `visible`/`siOculto` dependen de los datos del expediente; por eso este JAR ya no depende de Apache FOP ni lleva fuentes ni logo. Al cargar cada XML lo valida contra el esquema de su tipo (`documentoFormulario.xsd` o `documentoTexto.xsd`, recursos del JAR junto a `DocumentoXmlResolver`; los XML lo referencian en su `xsi:noNamespaceSchemaLocation` con la URL raw de GitHub de este repo en master, pero la validación usa siempre la copia del JAR, sin red) y aborta si no valida. Los `_*.xml` son fragmentos reutilizables (raíz `<fragmento>`, declarada en **los dos** esquemas con el contenido de su tipo, de modo que un fragmento de formulario incluido en un documento de texto no valida): cada `<include href="_x.xml"/>` hijo de la raíz del documento o del fragmento se sustituye por los hijos de la raíz del fragmento, recursivamente y con detección de ciclos; se valida cada fichero y también el documento expandido, y el chequeo incremental de mtime tiene en cuenta los fragmentos incluidos transitivamente. Falla si junto al XML existe un `.pdf` versionado con el mismo nombre (ambigüedad), si la raíz no es la de ningún tipo de documento, si un elemento lleva `siOculto` sin `visible` (los dos tipos) o si la estructura propia del tipo no cuadra: en FORMULARIO, que una `<fila>` no encaje en la rejilla de 12 columnas; en TEXTO, que una `<fila>` de una `<tabla>` no tenga exactamente `columnas` hijos (las dos se comprueban sobre el documento completo, ignorando `visible`).
  - **Los dos tipos de documento** los declara el enum `TipoDocumento` (raíz + esquema), que es **el único sitio** donde se sabe qué raíz es un documento: lo usan tanto `xml2pdf.Main` como `TipoExpedienteInstanceFileFinder`, que es quien mete cada documento en el enum `TipoDocumentoPdf` del tipo de expediente. `<documentoFormulario>` es la tabla sobre la rejilla de 12 columnas (secciones, filas, campos, checks, textos); `<documentoTexto>` es prosa (párrafos, listas, espacios y tablas de N columnas) precedida de un `<titulo>` **obligatorio**, que la aplicación estampa en la cabecera de la primera página junto al logo de la GVA. A diferencia del formulario, a un documento de texto sin `<titulo>` no se le inyecta el nombre del trámite: no valida contra su XSD y el build aborta.
  - **El `<titulo>` no es obligatorio** (solo en FORMULARIO; en TEXTO el `<titulo>` es un elemento más del cuerpo, puede repetirse y **no se inyecta nada**): si el formulario ya expandido no trae ninguno, se le añade uno al principio del todo con el `<name>` (castellano) del `TramiteInstance.xml` del trámite al que pertenece, que se busca **subiendo por las carpetas padre** desde la del propio XML, **con tope en el paquete raíz** (`TramitesLayout.findTramiteInstanceAncestro`). Su valenciano lo pone después el traductor, como el de cualquier otro texto. Si no hay `TramiteInstance.xml` por encima, o no tiene `<name>`, **falla el build**. El chequeo incremental de mtime tiene en cuenta también ese `TramiteInstance.xml`.
  - **El `<valenciano>` no es obligatorio**: si un elemento (`titulo`/`seccion`/`campo`/`check`/`texto`) no lleva ese hijo, se calcula traduciendo su `<castellano>` con el **proceso traductor externo** (`common/Traductor`, `apertium`), pasado como 3.er argumento opcional (por omisión `apertium`). Un `<valenciano></valenciano>` **vacío** sigue significando "este texto no lleva valenciano" — es la diferencia entre omitir el elemento y ponerlo vacío. Se traduce después de expandir los includes (así también los textos de los fragmentos) y las traducciones se cachean en memoria durante toda la ejecución. Los campos inline `${expresion}` y las URL se protegen con un marcador para que el traductor no los toque; para que no se traduzca ninguna otra cosa (siglas, nombres propios…) se le pega al final el sufijo `Traductor.SUFIJO_NO_TRADUCIR` (`__!!`) en el `<castellano>`, que no se dibuja en el PDF. Si el traductor no sabe traducir alguna palabra, **falla el build** indicando que se añada el `<valenciano>` a mano o se marque la palabra.

**Integradas en `processResources` (`finalizedBy`, se ejecutan *después* de copiar los recursos):**
- `viewprocessor.Main` (`<origen> <destino> [paqueteRaiz]`) — preprocesa el XML de vistas de Axelor: expande los paneles reutilizables en vistas concretas y escribe el XML depurado en `build/resources/main/views`. Un `views.xml` de fase **no es autosuficiente**: `TipoExpedienteViewsContext` deduce la fase del **nombre de la carpeta**, sube hasta el `TipoExpedienteInstance.xml` para localizar el form de plantillas en la raíz de la versión, y falla con mensajes explícitos ante los fallos típicos (carpeta que no es una fase, form de estado en la raíz, `<Code>` del form de plantillas que no es el del tipo). `tags/Form` compone el nombre global de la vista **con la fase** (`exp-<Code>-<Phase>-<State>[-<Profile>]-form`, el mismo que arma `PhaseEventManager.getViewName` en runtime) y comprueba que un views de fase solo declare estados de su propia fase y perfiles que el tipo use.
- `i18nprocessor.Main` (`<origen> <destino> <rutaExecTraductor> [paqueteRaiz]`) — genera/actualiza los CSV de i18n por directorio (invocando un **proceso traductor externo**, `apertium`, pasado como 3.er argumento), y luego copia `i18n_es.csv`/`i18n_ca.csv` → `custom_es.csv`/`custom_ca.csv` bajo `build/resources/main/i18n`. Recorre **todo** `src/main/java` (hay CSV fuera de los trámites): el `[paqueteRaiz]` solo le sirve para construir el finder de tipos de expediente.

**Integradas en `build` (`finalizedBy`, se ejecutan *después* del build):**
- `createdatainittipoexpediente.Main` (`<origen> <destino> [paqueteRaiz]`) — genera los ficheros semilla `data-init` (CSV/XML) de cada `TipoExpediente` bajo `build/resources/main/tiposExpedientes`. Además del registro del propio tipo, emite su `input/<Code>-aces.xml` con los `<aces>` del `TipoExpedienteInstance.xml` (tabla `AceProfileTipoExpediente`, ver [`<aces>`](#aces-perfiles-de-un-trámite-o-tipo-de-expediente)) y su `input/auth-<Code>.xml`: el permiso `<Code>.all` sobre la subclase de `Expediente` del tipo (cuyo FQCN da `TipoExpedienteInstanceFile.getFqcnExpediente()`) y su enganche a los grupos `admins` y `users`. Sin ese permiso el trámite sale en solo lectura para todo el mundo, porque `AuthSecurity` no recorre superclases; generarlo evita tener que acordarse de escribirlo a mano en el `auth-expedientes.xml` de `secretaria-virtual`. El enganche al grupo funciona porque `ModuleManager.createDefault` crea `admins`/`users` antes de instalar los módulos y porque el binding de una colección **añade** (`Property.addAll`), no reemplaza.
- `createdatainittramite.Main` (`<origen> <destino> [paqueteRaiz]`) — genera los ficheros semilla `data-init` de cada **trámite** bajo `build/resources/main/tramites`: `<code>/definicion/data-init` (el trámite y sus `<aces>` en `AceProfileTramite`, `priority="1"`) y, solo si el trámite declara `<defaultTipoExpediente>`, `<code>/tipo_expediente_activo/data-init` (`priority="-1"`). Borra `<destino>/tramites` entero antes de generar. El `<defaultTipoExpediente>` puede ser directamente un code o el nombre de la carpeta del tipo (`v1`, `v2`…), que se busca **recursivamente** bajo la carpeta del trámite y se resuelve a su code (falla si hay más de una coincidencia). Era una tarea Groovy (`generateDataInitTramites`) del `build.gradle`; ahora la tarea de Gradle es un `JavaExec` que solo invoca esta clase, **sin nada de lógica**.

### `<aces>`: perfiles de un trámite o tipo de expediente

- `TramiteInstance.xml` y `TipoExpedienteInstance.xml` admiten un bloque opcional `<aces>` con `<ace perfil="…" tipoUsuario="…"/>` o `<ace perfil="…" cargo="…"/>`; lo lee `files/ace/Ace` vía JAXB.
- `Ace.check` aborta el build si falta `perfil` o si no hay exactamente uno de `tipoUsuario`/`cargo`. **No** valida que el perfil exista en el enum `Profile`: si no existe, falla la carga del data-init al arrancar.
- El trámite o tipo no se escribe en el `<ace>`: la plantilla lo añade con el `code` del propio fichero.
- En el `input-config.xml` generado van **después** del `<input>` que crea el trámite/tipo, porque los `<input>` de un mismo fichero se cargan en orden. Hay dos `<input>` por fichero (`aces/ace[@tipoUsuario]` y `aces/ace[@cargo]`) porque la clave del `search` cambia según el sujeto.
- El data-init solo hace upsert: que quitar un `<ace>` lo quite de la BD depende de que `secretaria-virtual` vacíe esas tablas al arrancar (`DataBaseStartup`).

> **Nota:** varias tareas de copia/renderizado que antes eran clases `Main` de este repo (`copysql`, `documentopdf`, `copydomain`, `copyinitdata`, `generatedocs`) se migraron a **tareas Gradle nativas** en `secretaria-virtual/build.gradle` (tareas `Copy`, `doLast` con `copy {}`, y una tarea con PlantUML en el `buildscript`). Ya no existen aquí. Si necesitas cambiar esas copias, edita el `build.gradle`, no este JAR.

## Concepto de dominio central: `TipoExpediente`

Todo gira en torno a los **tipos de expediente**. El árbol de fuentes de `secretaria-virtual` contiene ficheros `TipoExpedienteInstance.xml`; `new files.tipoexpediente.TipoExpedienteInstanceFileFinder(tramitesLayout).findTiposExpedienteFile()` recorre el paquete raíz de los trámites, deserializa cada uno con JAXB en un `TipoExpedienteInstanceFile` (`files/tipoexpediente/`), y la mayoría de las herramientas iteran sobre esa lista.

Un `TipoExpediente` lleva: `code`, `name`, `tramite`, sus **`fases`** (`files/tipoexpediente/Fase`) y, dentro de cada una, sus `states` (cada uno con eventos + un perfil), más las listas derivadas `events`/`profiles` y los tipos de documento PDF. El `<states>` plano de la raíz es el **formato antiguo** y se rechaza con un error que lo explica.

La **fase** es la pieza central del modelo de ficheros: da la carpeta y el paquete de sus clases, el FQCN de su `PhaseEventManagerImpl` y su `StateEventValidatorImpl`, y la unión de los eventos de sus estados. La identidad de un estado es la **pareja** (fase, estado): su código solo es único dentro de su fase. Ya **no** se persisten los FQCN de esas clases: el data-init guarda un único `basePackageName` (el paquete de la carpeta de versión), del que cuelga un paquete por fase, y el runtime resuelve con él más el `codePhase` del expediente.

Esto define un **modelo de máquina de estados**: fases → estados → eventos → perfiles, que los generadores convierten en la clase `States`, validadores y vistas de Axelor.

> Los ámbitos `ambitoCreador`/`ambitoResponsable`/`ambitoAuditor` se siguen parseando, pero son> **inertes**: sus propiedades y su enum están comentados en la entidad y el data-init ya no los persiste.

## Dónde viven los trámites: `TramitesLayout`

Tanto los **trámites** (`TramiteInstance.xml`) como los **tipos de expediente** (`TipoExpedienteInstance.xml`) se descubren **por la presencia de su fichero maestro, a cualquier profundidad** bajo un paquete raíz configurable (por omisión `com.educaflow.tramites`): las carpetas intermedias sirven solo de agrupación. `files/tramite/TramitesLayout` concentra todo lo que depende de esa estructura y es lo que se pasa a los finders:

- `getRootPackagePath()` (= `origen` + el paquete con `/`), `getSharedPath()` (los `TipoDocumentoPdf` compartidos), `existeRaiz()`.
- `findTramiteInstanceAncestro(desde)` — sube por las carpetas padre buscando el `TramiteInstance.xml`, **incluyendo la raíz y cortando ahí**; `null` si el punto de partida ni siquiera está bajo la raíz. `buscarTramiteInstanceAncestroSinTope(desde)` es la variante estática sin tope, y solo la usa el `DocumentoXmlResolver.main` autónomo (a mano no se sabe cuál es la raíz de fuentes).
- `getTramiteInstanceDelTipo(tipoXml)` — el trámite de un tipo de expediente; falla con «no está bajo el paquete raíz» o «tipo de expediente huérfano».
- `checkTramitesNoAnidados()` — un trámite no puede estar dentro de otro.
- `paqueteRaizFromArgs(args, index)` — el argumento opcional de los `Main`.

Las validaciones son **eager** (se hacen aunque el XML declare todos sus campos y no necesite heredar nada), para que aborten en la fase más temprana del build. A ellas se suma, en `findTiposExpedienteFile()`, la de **codes de tipo duplicados**: al admitir tipos a cualquier profundidad, dos carpetas homónimas bajo el mismo trámite (`grupoA/v1` y `grupoB/v1`) derivarían el mismo `code`. Si la raíz no existe: aviso por consola y lista vacía, sin excepción.

`files/tramite/TramiteInstanceFile` (+ su `TramiteInstanceFileFinder`) es el **único** modelo del `TramiteInstance.xml`: lo usan `createdatainittramite`, el `TipoExpedienteInstanceFile` que hereda code/name de su trámite, y `xml2pdf` para el título de los documentos sin `<titulo>`. Ojo con los detalles heredados del Groovy al que sustituye: `code`/`name`/`tipoTramite` son obligatorios y van con `trim()`; el `<help>` va **sin `trim()`** (dentro de un CDATA) y es `""` si no existe; `publico`/`privado` son `String` y no `boolean` para poder distinguir «no declarado» (`null`, no se emite el atributo) de «declarado y vacío» (`""`, sí se emite); y un `<defaultTipoExpediente>` **en blanco** cuenta como no declarado.

## Infraestructura compartida

- **`common/TemplateUtil.java`** — envuelve el motor de plantillas [Pebble](https://pebbletemplates.io/). `evaluateTemplate(name, context)` carga un `*.template` desde `src/main/resources/` en el classpath. El auto-escaping está **desactivado**; hay funciones personalizadas `asterisks` y `escapeXml` (esta última obligatoria para los valores de atributo, justo porque no hay auto-escaping) y un tratamiento especial de saltos de línea. Todo el código/XML generado pasa por aquí.
- **`common/Traductor.java`** — traduce de castellano a valenciano lanzando el **proceso traductor externo** (`apertium spa-cat_valencia`) y lanza `FalloTraduccionException` si el traductor marca con `*` alguna palabra que no conoce (salvo que lleve el sufijo `SUFIJO_NO_TRADUCIR`, `__!!`, que se elimina de la traducción). Lo usan `i18nprocessor` (para los CSV de i18n) y `xml2pdf` (`DocumentoXmlResolver`, para el `<valenciano>` que falte).
- **`common/XMLUtil`, `FileUtil`, `TextUtil`** — utilidades de DOM, recorrido del sistema de ficheros, y utilidades de cadenas/nomenclatura (p. ej. inflexión de Axelor).
- **`src/main/resources/*.template`** — plantillas Pebble, el origen de todo el código generado. Cada una se corresponde con un generador (p. ej. `domain-model.template` → `DomainModelFile`, `phase-event-manager.template` → `PhaseEventManagerFile`, `state-event-validator.template` → `StateEventValidatorFile`, `states.template` → `createstates.StatesFile`, y las dos de vistas: `views-templates.template` → `ViewsFile`, el `views.xml` de la **raíz de la versión** con el único form de plantillas `exp-<Code>-Templates`, y `views-fase.template` → `ViewsFaseFile`, el `views.xml` de **cada fase** con los `<form state="...">` de sus estados). Edita estas para cambiar la salida generada. Las plantillas de un **método suelto** (`phase-event-manager-trigger-method.template`, `phase-event-manager-onenter-method.template`, `state-event-validator-method.template`) las incluye con `{% include %}` la plantilla del fichero completo y además se renderizan por separado desde los `getSourceCode*`: por eso el snippet que un test ofrece para pegar no puede divergir del código generado.
- **`files/*`** — un subpaquete por cada tipo de artefacto generado (`domainclass`, `domainmodel`, `views`, `eventmanagerfile`, `stateeventvalidator`, `tipoexpediente`, `tramite`, `i18n`). Las clases `*File` **solo crean** (`create...IfNotExists`, que devuelve `true` si ha creado el fichero); ya no validan. Las de `eventmanagerfile` y `stateeventvalidator` exponen además, en público, el **convenio de nombres** (`getMethodNameTriggerEvent`, `getMethodNameOnEnterEvent`, `getMethodNameBeanValidationRules`, `getModelFQCN`) y los **renderizadores del código fuente de un método suelto** (`getSourceCode*`), que reutilizan los tests de `secretaria-virtual` para que el método que dicen que falta sea literalmente el que este generador habría escrito.

## `scripts-antiguos/` — la forma vieja de generar el PDF desde el XML

La carpeta `scripts-antiguos/` contiene los dos scripts Python que eran la **forma vieja** de generar el PDF rellenable a partir del XML de definición de un documento, en dos pasos y necesitando LibreOffice:

- `xml2odt.py` — convierte el XML de definición en un `.odt` de LibreOffice Writer con controles de formulario (usa `assets/logo-gva.png` y `assets/styles-template.xml`).
- `odt2pdf.py` — convierte ese `.odt` en PDF rellenable con LibreOffice headless, conservando los campos AcroForm y fusionando los duplicados `_2`, `_3`…

La forma **actual** es en dos partes: `xml2pdf.Main` de este JAR resuelve el XML en el build de `secretaria-virtual`, y la propia aplicación dibuja el PDF en runtime con iText (`com.educaflow.base.infrastructure.pdfgenerator`); entre medias hubo una versión que dibujaba el PDF en el build con Apache FOP (`Xml2Pdf`, retirada al añadir la visibilidad condicional `visible`/`siOculto`, que necesita los datos del expediente). Los scripts se conservan aquí solo como referencia/respaldo — estas son las únicas copias canónicas; el fichero `documentos.md` del skill `k-tipo-expediente` de `secretaria-virtual` documenta el **formato del XML**.

## Convenciones y detalles a tener en cuenta

- **Idioma:** los identificadores, comentarios y mensajes de log/excepción están en **castellano** (`origen`/`destino`, "Iniciando tarea…"). Sigue este estilo.
- Los generadores son **idempotentes por omisión**: `createfiles` solo escribe los ficheros que aún no existen (`create...IfNotExists`) — nunca sobrescribe fuentes editadas a mano. En cambio, las herramientas `rich*` siempre regeneran dentro de `build/`.
- `i18nprocessor` **lanza una excepción y hace fallar el build** cuando acumula errores de traducción: es una barrera de validación, no solo un generador. `createfiles` ya **no** lo es — solo genera, y su validación vive en los tests de `secretaria-virtual` (ver arriba).
- Las rutas de las tareas son relativas al directorio del proyecto `secretaria-virtual` (`workingDir`), no a este repositorio.
- Solo `target/` está ignorado por git.
