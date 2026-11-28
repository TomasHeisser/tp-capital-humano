@startuml
skinparam style strictuml
skinparam maxMessageSize 220
skinparam sequenceMessageAlign left
skinparam ParticipantPadding 12
skinparam BoxPadding 8
autonumber

title Diagrama de Secuencia (versión final) - CU020: Gestionar Preguntas

actor "Consultor" as actor
boundary ":PantallaGestionarPreguntas" as ui
control ":CompetenciaController" as cctrl
control ":PreguntaController" as pctrl
control ":GestorCompetencias" as gcomp
control ":GestorPreguntas" as gestor
entity "p:Pregunta" as p
entity "f:Factor" as f
entity "c:Competencia" as c
participant ":CompetenciaDAO" as cdao
participant ":FactorDAO" as fdao
participant ":PreguntaDAO" as pdao
database ":BD PostgreSQL" as bd

actor -> ui : selecciona del menú la opción "Preguntas" [paso 1]
activate ui

== Carga de los combos (corrección E5: traer los DTO de competencias y factores) ==
ui -> cctrl : listarCompetencias()
activate cctrl
cctrl -> gcomp : listarCompetencias()
activate gcomp
gcomp -> cdao : buscarTodos()
activate cdao
cdao -> bd : SELECT Competencia (activas)
activate bd
bd --> cdao : filas
deactivate bd
cdao --> gcomp : List<Competencia>
deactivate cdao
loop por cada Competencia
  gcomp -> gcomp : crear CompetenciaDTO(id, codigo, nombre)
end
gcomp --> cctrl : List<CompetenciaDTO>
deactivate gcomp
cctrl --> ui : List<CompetenciaDTO>
deactivate cctrl
ui --> actor : presenta la pantalla en blanco con los criterios:\nCompetencia (combo), Factor (combo) y Nombre de Pregunta [paso 2]

opt el actor selecciona una Competencia
  actor -> ui : selecciona Competencia
  ui -> cctrl : listarFactores(idCompetencia)
  activate cctrl
  cctrl -> gcomp : listarFactores(idCompetencia)
  activate gcomp
  gcomp -> fdao : buscarPorCompetencia(idCompetencia)
  activate fdao
  fdao -> bd : SELECT Factor (activos de la competencia)
  activate bd
  bd --> fdao : filas
  deactivate bd
  fdao --> gcomp : List<Factor>
  deactivate fdao
  loop por cada Factor
    gcomp -> gcomp : crear FactorDTO
  end
  gcomp --> cctrl : List<FactorDTO>
  deactivate gcomp
  cctrl --> ui : List<FactorDTO>
  deactivate cctrl
  ui --> actor : carga el combo de Factores
end

alt 3.A: el actor presiona "Nuevo" sin haber buscado
  actor -> ui : presiona "Nuevo"
  ref over ui, gestor : CU021 - Dar de alta Pregunta
  ui -> ui : actualizarGrilla() (repite la última búsqueda, si la hubo) [3.A.2]
  ui --> actor : continúa en el paso 3
end

actor -> ui : ingresa al menos un criterio y presiona "Buscar" [paso 3]
ui -> pctrl : buscar(filtro: FiltroPreguntaDTO)
activate pctrl
pctrl -> gestor : buscarPreguntas(filtro: FiltroPreguntaDTO)
activate gestor
gestor -> gestor : validar que haya al menos un criterio
alt no se ingresó ningún criterio
  gestor --> pctrl : ValidacionException("Debe ingresar al menos un criterio de búsqueda")
  pctrl --> ui : 400 + ErrorDTO
  ui --> actor : muestra el mensaje sin tapar campos ni botones y vuelve al paso 3
else hay al menos un criterio
  gestor -> pdao : buscarPorFiltros(idCompetencia, idFactor, nombre)
  activate pdao
  pdao -> bd : SELECT Pregunta JOIN Factor (activas, nombre "contiene")
  activate bd
  bd --> pdao : filas
  deactivate bd
  pdao --> gestor : List<Pregunta>
  deactivate pdao
  loop por cada p en List<Pregunta>
    gestor -> p : getFactor()
    activate p
    p --> gestor : f
    deactivate p
    gestor -> f : getCompetencia()
    activate f
    f --> gestor : c
    deactivate f
    gestor -> c : getNombre()
    activate c
    c --> gestor : nombreCompetencia
    deactivate c
    gestor -> gestor : crear PreguntaResumenDTO(p.id, nombreCompetencia,\nf.nombre, p.nombre, p.enunciado)
  end
  gestor --> pctrl : List<PreguntaResumenDTO>
  deactivate gestor
  pctrl --> ui : List<PreguntaResumenDTO>
  deactivate pctrl
  ui --> actor : muestra la grilla: Competencia, Factor, Pregunta [paso 4]

  actor -> ui : selecciona una pregunta de la lista [paso 5]
  alt 5.A: presiona "Modificar"
    ref over ui, gestor : CU022 - Modificar Pregunta
    ui -> ui : actualizarGrilla() [5.A.2]
    ui --> actor : continúa en el paso 4
  else 5.B: presiona "Eliminar"
    ref over ui, gestor : CU023 - Dar de baja Pregunta
    ui -> ui : actualizarGrilla() [5.B.2]
    ui --> actor : continúa en el paso 4
  else presiona "Nuevo"
    ref over ui, gestor : CU021 - Dar de alta Pregunta
    ui -> ui : actualizarGrilla()
    ui --> actor : continúa en el paso 4
  end
end

opt en cualquier paso el actor presiona "Salir" (Observaciones)
  actor -> ui : presiona "Salir"
  ui --> actor : cierra la pantalla sin guardar y vuelve a la pantalla principal
end
deactivate ui

note over ui, bd
  "actualizarGrilla()" vuelve a llamar a buscar(filtro) con el último filtro usado
  (misma secuencia de los pasos 3 y 4). El criterio de búsqueda es "contiene".
end note
@enduml
