@startuml
skinparam style strictuml
skinparam maxMessageSize 220
skinparam sequenceMessageAlign left
skinparam ParticipantPadding 10
skinparam BoxPadding 8
autonumber

title Diagrama de Secuencia (versión final) - CU021: Dar de alta Pregunta (incluye CU033: Generar pregunta con IA)

actor "Consultor" as actor
boundary ":PantallaAltaPregunta" as ui
boundary ":PantallaAsistenteIA" as uiia
control ":CompetenciaController" as cctrl
control ":OpcionRespuestaController" as octrl
control ":PreguntaController" as pctrl
control ":GestorCompetencias" as gcomp
control ":GestorOpcionesRespuesta" as gop
control ":GestorPreguntas" as gestor
participant ":IAsistenteIA\n(AdaptadorServicioIA)" as ia
participant ":APIGemini" as api
entity "f:Factor" as f
entity "op:OpcionRespuesta" as op
participant ":FactorDAO" as fdao
participant ":OpcionRespuestaDAO" as odao
participant ":PreguntaDAO" as pdao
database ":BD PostgreSQL" as bd

activate ui
== Paso 1: carga de los datos de selección (corrección E5: DTO a la UI) ==
ui -> cctrl : listarCompetencias()
activate cctrl
cctrl -> gcomp : listarCompetencias()
activate gcomp
ref over gcomp, bd : CompetenciaDAO.buscarTodos() (igual que en CU020)
gcomp --> cctrl : List<CompetenciaDTO>
deactivate gcomp
cctrl --> ui : List<CompetenciaDTO>
deactivate cctrl
ui -> octrl : listarOpciones()
activate octrl
octrl -> gop : listarOpciones()
activate gop
gop -> odao : buscarTodos()
activate odao
odao -> bd : SELECT Opcion_Respuesta, Item_Opcion (activas)
activate bd
bd --> odao : filas
deactivate bd
odao --> gop : List<OpcionRespuesta>
deactivate odao
loop por cada OpcionRespuesta
  gop -> gop : crear OpcionRespuestaDTO con sus ItemOpcionDTO (orden, descripción)
end
gop --> octrl : List<OpcionRespuestaDTO>
deactivate gop
octrl --> ui : List<OpcionRespuestaDTO>
deactivate octrl

loop mientras el actor elija "SI" en "¿Desea cargar otra?" (5.A)
  ui --> actor : presenta el formulario en blanco: Competencia, Factor, Nombre de Pregunta,\nPregunta, Descripción (opcional), Opción de Respuesta y los botones\n"Generar con IA", "Aceptar" y "Cancelar" [paso 1]

  alt 1.A.2: el actor presiona "Cancelar"
    actor -> ui : presiona "Cancelar"
    ui --> actor : vuelve a CU020 - Gestionar Preguntas [1.A.2.2]
  else 1.A.1: el actor presiona "Generar con IA" (CU033)
    actor -> ui : presiona "Generar con IA"
    ui -> uiia ** : <<create>> (ventana emergente con Competencia y Factor)
    activate uiia
    actor -> uiia : selecciona Competencia
    uiia -> cctrl : listarFactores(idCompetencia)
    activate cctrl
    ref over cctrl, bd : GestorCompetencias.listarFactores(idCompetencia) (igual que en CU020)
    cctrl --> uiia : List<FactorDTO>
    deactivate cctrl
    actor -> uiia : selecciona Factor y presiona "Generar pregunta" [CU033 paso 2]
    uiia -> pctrl : generarConIA(s: SolicitudIADTO)
    activate pctrl
    pctrl -> gestor : generarPreguntaIA(s: SolicitudIADTO)
    activate gestor
    gestor -> gestor : validar(s) (competencia y factor obligatorios)
    alt CU033 2.A: faltan datos
      gestor --> pctrl : ValidacionException(lista de omisiones)
      pctrl --> uiia : 400 + ErrorDTO
      uiia --> actor : muestra TODAS las omisiones sin tapar campos ni botones
    else datos completos
      gestor -> fdao : buscarPorId(idFactor)
      activate fdao
      fdao -> bd : SELECT Factor
      fdao --> gestor : f
      deactivate fdao
      gestor -> f : getCompetencia()
      activate f
      f --> gestor : competencia
      deactivate f
      gestor -> odao : buscarTodos()
      activate odao
      odao -> bd : SELECT Opcion_Respuesta, Item_Opcion
      odao --> gestor : List<OpcionRespuesta>
      deactivate odao
      gestor -> ia : generarPregunta(competencia, f, opciones) [CU033 paso 3]
      activate ia
      ia -> ia : armarPrompt(competencia, factor y descripciones,\ncatálogo de opciones de respuesta)
      ia -> api : generateContent(prompt)
      activate api
      alt CU033 4.A: no se puede conectar con el servicio
        api --> ia : error / timeout
        ia --> gestor : ServicioIAException
        gestor --> pctrl : ServicioIAException
        pctrl --> uiia : 503 + ErrorDTO
        uiia --> actor : "Ocurrió un error al comunicarse con el servicio de IA.\nIntente nuevamente más tarde"
      else respuesta correcta
        api --> ia : JSON (nombre, enunciado, descripción, opción elegida)
        deactivate api
        ia -> ia : convertir JSON en PreguntaDTO
        ia --> gestor : PreguntaDTO
        deactivate ia
        gestor --> pctrl : PreguntaDTO
        deactivate gestor
        pctrl --> uiia : PreguntaDTO
        deactivate pctrl
        uiia --> ui : PreguntaDTO (cierra la ventana emergente)
        deactivate uiia
        ui --> actor : precarga Competencia, Factor, Nombre de Pregunta, Pregunta, Descripción y\nOpción de Respuesta, y muestra la grilla Orden / Descripción / Ponderación [CU033 paso 4]
      end
    end
  else carga manual
    actor -> ui : selecciona Competencia
    ui -> cctrl : listarFactores(idCompetencia)
    activate cctrl
    ref over cctrl, bd : GestorCompetencias.listarFactores(idCompetencia) (igual que en CU020)
    cctrl --> ui : List<FactorDTO>
    deactivate cctrl
    ui --> actor : carga los Factores de la Competencia (Observaciones)
    actor -> ui : selecciona Factor, ingresa Nombre, Pregunta y Descripción y elige la Opción de Respuesta
    ui --> actor : muestra la grilla de opciones: Orden (lectura), Descripción (lectura),\nPonderación (editable), ordenada por orden (Observaciones)
  end

  actor -> ui : completa las ponderaciones y presiona "Aceptar" [paso 2]
  ui -> pctrl : registrar(datos: PreguntaDTO)
  activate pctrl
  pctrl -> gestor : registrarPregunta(datos: PreguntaDTO)
  activate gestor
  gestor -> gestor : validar(datos): omisiones, longitudes máximas y\nponderaciones (cero o positivas y suman 10)
  alt 2.A / 2.B: faltan datos, algún campo excede su longitud o las ponderaciones no son válidas
    gestor --> pctrl : ValidacionException(lista con TODOS los errores)
    pctrl --> ui : 400 + ErrorDTO
    ui --> actor : muestra TODOS los errores sin tapar campos ni botones y vuelve al paso 2
  else datos válidos
    gestor -> pdao : buscarCoincidencias(idFactor, nombre, enunciado, null)
    activate pdao
    pdao -> bd : SELECT Pregunta (activas del factor con ese nombre o ese enunciado)
    pdao --> gestor : List<Pregunta>
    deactivate pdao
    alt 2.C: la pregunta ya existe para esa competencia y factor (lista no vacía)
      gestor -> gestor : armar mensaje según coincida el nombre, el enunciado o ambos (obs. 1)
      gestor --> pctrl : PreguntaDuplicadaException("Ya existe una pregunta con el nombre <Nombre>\n[y/o] con ese enunciado para la competencia y el factor")
      pctrl --> ui : 409 + ErrorDTO
      ui --> actor : avisa cuál es la validación no superada y vuelve al paso 2
    else no existe
      gestor -> fdao : buscarPorId(idFactor)
      activate fdao
      fdao -> bd : SELECT Factor
      fdao --> gestor : f
      deactivate fdao
      gestor -> odao : buscarPorId(idOpcionRespuesta)
      activate odao
      odao -> bd : SELECT Opcion_Respuesta, Item_Opcion
      odao --> gestor : op
      deactivate odao
      gestor -> "p:Pregunta" ** : <<create>> Pregunta(nombre, enunciado, descripcion, f, op)
      loop por cada PonderacionDTO de datos.ponderaciones
        gestor -> op : getItem(idItem)
        activate op
        op --> gestor : item: ItemOpcionRespuesta
        deactivate op
        gestor -> "p:Pregunta" : agregarPonderacion(item, ponderacion)
      end
      group Transacción
        gestor -> pdao : guardar(p)
        activate pdao
        pdao -> bd : INSERT Pregunta
        bd --> pdao : OK
        loop por cada PonderacionPreguntaOpcion de p
          pdao -> bd : INSERT pondera
          bd --> pdao : OK
        end
        pdao --> gestor : true
        deactivate pdao
      end
      gestor --> pctrl : true
      deactivate gestor
      pctrl --> ui : true
      deactivate pctrl
      ui --> actor : "La Pregunta <Nombre Pregunta> se ha creado correctamente\n¿Desea cargar otra?" con los botones "SI" y "NO" [pasos 3 y 4]
      alt paso 5: el actor elige "NO"
        actor -> ui : presiona "NO"
        ui --> actor : el CU termina y vuelve a CU020 [paso 6]
      else 5.A: el actor elige "SI"
        actor -> ui : presiona "SI"
        ui -> ui : blanquear campos (vuelve al paso 1)
      end
    end
  end
end
deactivate ui

note over gestor, bd
  Los datos literales se convierten a MAYÚSCULAS en la pantalla y el gestor los vuelve a normalizar.
  La ponderación usa BigDecimal para que la suma igual a 10 sea exacta.
end note
@enduml
