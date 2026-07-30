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

- El source/target del compilador Maven es **Java 11** (`pom.xml`), pero el consumidor (`secretaria-virtual`) se ejecuta sobre **Java 21** — mantén el código compatible con Java 11.
- `-parameters` está activado (se conservan los nombres de parámetros; algunas herramientas dependen de reflexión).

### Ejecutar una única herramienta a mano

Cada tarea es `java -cp <jar+deps> <mainClass> <args...>`. El orden de los argumentos coincide con el `build.gradle` de `secretaria-virtual` (`../secretaria-virtual/build.gradle`), que es la fuente de la verdad sobre cómo se invocan. La mayoría reciben `<rutaOrigen> <rutaDestino>`, donde origen es `./src/main/java` y destino es algún directorio bajo `./build/resources/main` o `./build/src-gen/main/java`.

## Los puntos de entrada (`*/Main.java`)

Cada paquete bajo `com.educaflow.common.buildtools` es una herramienta con su `Main`. Este es el núcleo del proyecto — entiéndelos primero.

**Integradas en el `generateCode` de Axelor (se ejecutan *antes* de que Axelor genere el código de las entidades):**
- `createfiles.Main` — para cada `TipoExpediente`, **genera los ficheros fuente que faltan** (`domains.xml` de Axelor, `views.xml`, el `.java` del `EventManager`, el `.kt` del `StateEventValidator`) a partir de plantillas Pebble, y luego los **valida** vía Spoon (`check()` devuelve mensajes de error que abortan el build).
- `richdomainclass.MainModelXml` — enriquece el **XML** de dominio antes de que Axelor lo lea (`addExtraCodeToDomainXml`), usando `extra-code-domain-xml.template`.
- `richdomainclass.Main` — enriquece la **clase Java** de dominio generada (`addExtraCodeToDomainClass`), inyectando enums para los estados/eventos/perfiles del expediente vía Spoon + `extra-code-domain.template`. Escribe en `build/src-gen/main/java`.

**Integradas en `processResources` (`dependsOn`, se ejecutan *antes* de copiar los recursos):**
- `xml2pdf.Main` — genera los **PDF rellenables de los documentos de los trámites** directamente desde su XML de definición (raíz `<documento>`, en carpetas `documentospdf`/`documentos`, sin prefijo `_`) usando la librería PDF de Apache FOP, escribiendo en `build/src-gen/main/resources` con la misma ruta de paquete del XML (así el PDF queda en el classpath sin versionarse en git). Al cargar cada XML lo valida contra el esquema `documento.xsd` (recurso del JAR junto a `Xml2Pdf`; los XML lo referencian en su `xsi:noNamespaceSchemaLocation` con la URL raw de GitHub de este repo en master, pero la validación usa siempre la copia del JAR, sin red) y aborta si no valida. Falla si junto al XML existe un `.pdf` versionado con el mismo nombre (ambigüedad). Las fuentes Roboto, el logo GVA y `documento.xsd` van como recursos dentro del JAR.

**Integradas en `processResources` (`finalizedBy`, se ejecutan *después* de copiar los recursos):**
- `viewprocessor.Main` — preprocesa el XML de vistas de Axelor: expande los paneles reutilizables `<template-form>` en vistas concretas, y luego escribe el XML depurado en `build/resources/main/views`.
- `i18nprocessor.Main` — genera/actualiza los CSV de i18n por directorio (invocando un **proceso traductor externo**, `apertium`, pasado como 3.er argumento), y luego copia `i18n_es.csv`/`i18n_ca.csv` → `custom_es.csv`/`custom_ca.csv` bajo `build/resources/main/i18n`.

**Integradas en `build` (`finalizedBy`, se ejecutan *después* del build):**
- `createdatainittipoexpediente.Main` — genera los ficheros semilla `data-init` (CSV/XML) de cada `TipoExpediente` bajo `build/resources/main/tiposExpedientes`.

> **Nota:** varias tareas de copia/renderizado que antes eran clases `Main` de este repo (`copysql`, `documentopdf`, `copydomain`, `copyinitdata`, `generatedocs`) se migraron a **tareas Gradle nativas** en `secretaria-virtual/build.gradle` (tareas `Copy`, `doLast` con `copy {}`, y una tarea con PlantUML en el `buildscript`). Ya no existen aquí. Si necesitas cambiar esas copias, edita el `build.gradle`, no este JAR.

## Concepto de dominio central: `TipoExpediente`

Todo gira en torno a los **tipos de expediente**. El árbol de fuentes de `secretaria-virtual` contiene ficheros `TipoExpedienteInstance.xml`; `files.tipoexpediente.TipoExpedienteInstanceFileFinder.findTiposExpedienteFile(root)` recorre el árbol, deserializa cada uno con JAXB en un `TipoExpedienteInstanceFile` (`files/tipoexpediente/`), y la mayoría de las herramientas iteran sobre esa lista.

Un `TipoExpediente` lleva: `code`, `name`, `tramite`, los ámbitos (`ambitoCreador`/`ambitoResponsable`/`ambitoAuditor`), `states` (cada uno con eventos + un perfil), las listas derivadas `events`/`profiles`, los tipos de documento PDF, y los FQCN de su `EventManager` / `StateEventValidator`. Esto define un **modelo de máquina de estados**: estados → eventos → perfiles, que los generadores convierten en enums, validadores y vistas de Axelor.

## Infraestructura compartida

- **`common/SpoonUtil.java`** — envuelve [Spoon](https://spoon.gforge.inria.fr/) para leer/transformar el código Java como AST (encontrar métodos anotados, inyectar código). Usado por `richdomainclass` y por la validación de `createfiles`.
- **`common/TemplateUtil.java`** — envuelve el motor de plantillas [Pebble](https://pebbletemplates.io/). `evaluateTemplate(name, context)` carga un `*.template` desde `src/main/resources/` en el classpath. El auto-escaping está **desactivado**; hay una función personalizada `asterisks` y un tratamiento especial de saltos de línea. Todo el código/XML generado pasa por aquí.
- **`common/XMLUtil`, `FileUtil`, `TextUtil`** — utilidades de DOM, recorrido del sistema de ficheros, y utilidades de cadenas/nomenclatura (p. ej. inflexión de Axelor).
- **`src/main/resources/*.template`** — plantillas Pebble, el origen de todo el código generado. Cada una se corresponde con un generador (p. ej. `domain-model.template` → `DomainModelFile`, `event-manager.template` → `EventManagerFile`, `views.template` → `ViewsFile`, `state-event-validator.template` → `StateEventValidatorFile`). Edita estas para cambiar la salida generada.
- **`files/*`** — un subpaquete por cada tipo de artefacto generado/validado (`domainclass`, `domainmodel`, `views`, `eventmanagerfile`, `stateeventvalidator`, `tipoexpediente`, `i18n`). Las clases `*File` gestionan tanto la creación (`create...IfNotExists`) como la validación (`check()`).

## `scripts-antiguos/` — la forma vieja de generar el PDF desde el XML

La carpeta `scripts-antiguos/` contiene los dos scripts Python que eran la **forma vieja** de generar el PDF rellenable a partir del XML de definición de un documento, en dos pasos y necesitando LibreOffice:

- `xml2odt.py` — convierte el XML de definición en un `.odt` de LibreOffice Writer con controles de formulario (usa `assets/logo-gva.png` y `assets/styles-template.xml`).
- `odt2pdf.py` — convierte ese `.odt` en PDF rellenable con LibreOffice headless, conservando los campos AcroForm y fusionando los duplicados `_2`, `_3`…

La forma **actual** es la herramienta Java `xml2pdf.Main` de este JAR (XML → PDF directo con Apache FOP, sin `.odt` intermedio ni LibreOffice), integrada en el build de `secretaria-virtual`. Los scripts se conservan aquí solo como referencia/respaldo — estas son las únicas copias canónicas; el skill `k-documentos` de `secretaria-virtual` ya solo documenta el **formato del XML**, no la generación.

## Convenciones y detalles a tener en cuenta

- **Idioma:** los identificadores, comentarios y mensajes de log/excepción están en **castellano** (`origen`/`destino`, "Iniciando tarea…"). Sigue este estilo.
- Los generadores son **idempotentes por omisión**: `createfiles` solo escribe los ficheros que aún no existen (`create...IfNotExists`) — nunca sobrescribe fuentes editadas a mano. En cambio, las herramientas `rich*` siempre regeneran dentro de `build/`.
- Una herramienta que devuelve un mensaje no vacío desde `check()` (createfiles) o que acumula errores de traducción (i18nprocessor) **lanza una excepción y hace fallar el build** — son barreras de validación, no solo generadores.
- Las rutas de las tareas son relativas al directorio del proyecto `secretaria-virtual` (`workingDir`), no a este repositorio.
- Solo `target/` está ignorado por git.
