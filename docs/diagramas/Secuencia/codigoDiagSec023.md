@startuml
skinparam style strictuml
skinparam maxMessageSize 220
skinparam sequenceMessageAlign left
skinparam ParticipantPadding 14
skinparam BoxPadding 8
autonumber

title Diagrama de Secuencia (versión final) - CU023: Dar de baja una Pregunta

actor "Consultor" as actor
boundary ":PantallaBajaPregunta" as ui
control ":PreguntaController" as pctrl
control ":GestorPreguntas" as gestor
entity "p:Pregunta" as p
control ":GestorAuditoria" as gaud
entity "r:RegistroAuditoria" as reg
participant ":PreguntaDAO" as pdao
participant ":ConsultorDAO" as condao
participant ":RegistroAuditoriaDAO" as adao
database ":BD PostgreSQL" as bd

actor -> ui : presiona "Eliminar" sobre la pregunta seleccionada en CU020 [paso 1]
activate ui
ui --> actor : "Los datos de la pregunta <Nombre Pregunta> serán eliminados del sistema"\ncon los botones "ELIMINAR" y "CANCELAR" [paso 2]

alt 3.A: el actor presiona "CANCELAR"
  actor -> ui : presiona "CANCELAR"
  ui --> actor : cierra el mensaje sin cambios; el CU termina [3.A.1]
else paso 3: el actor presiona "ELIMINAR"
  actor -> ui : presiona "ELIMINAR"
  ui -> pctrl : darDeBaja(idPregunta)
  activate pctrl
  pctrl -> pctrl : obtener el usuario autenticado del token JWT
  pctrl -> gestor : darDeBajaPregunta(idPregunta, username)
  activate gestor
  group Transacción (baja lógica + registro de auditoría)
    gestor -> pdao : buscarPorId(idPregunta)
    activate pdao
    pdao -> bd : SELECT Pregunta
    activate bd
    bd --> pdao : fila
    deactivate bd
    pdao --> gestor : p
    deactivate pdao
    gestor -> condao : buscarPorUsername(username)
    activate condao
    condao -> bd : SELECT Consultor
    activate bd
    bd --> condao : fila
    deactivate bd
    condao --> gestor : consultor
    deactivate condao
    gestor -> p : darDeBaja(ahora)
    activate p
    p -> p : fechaBaja = ahora
    deactivate p
    gestor -> pdao : actualizar(p)
    activate pdao
    pdao -> bd : UPDATE Pregunta (fecha_baja)
    activate bd
    bd --> pdao : 1 fila
    deactivate bd
    pdao --> gestor : true
    deactivate pdao
    gestor -> gaud : registrarBaja("BAJA", "PREGUNTA", p.id, consultor)
    activate gaud
    create reg
    gaud -> reg : <<create>> RegistroAuditoria(ahora, "BAJA", "PREGUNTA", p.id, consultor)
    gaud -> adao : guardar(r)
    activate adao
    adao -> bd : INSERT Registro_Auditoria
    activate bd
    bd --> adao : OK
    deactivate bd
    adao --> gaud : true
    deactivate adao
    gaud --> gestor : true
    deactivate gaud
  end
  gestor --> pctrl : true
  deactivate gestor
  pctrl --> ui : true
  deactivate pctrl
  ui --> actor : "Los datos de la pregunta <Nombre Pregunta> han sido eliminados del sistema." [paso 3]
  actor -> ui : presiona cualquier tecla [paso 4]
  ui --> actor : cierra el cartel; CU020 actualiza la grilla y el CU termina [paso 5]
end
deactivate ui

note over gestor, bd
  Requerimiento especial: el registro de auditoría guarda el consultor (objeto, corrección E2),
  la fecha y la hora. La pregunta no se borra físicamente: los cuestionarios ya generados
  usan su copia (PreguntaEvaluada), así que no se ven afectados.
end note
@enduml
