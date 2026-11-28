@startuml
skinparam style strictuml
skinparam maxMessageSize 230
skinparam sequenceMessageAlign left
skinparam ParticipantPadding 10
skinparam BoxPadding 8
autonumber

title Diagrama de Secuencia (versión final) - CU026: Completar Cuestionario

actor "Candidato" as actor
boundary ":PantallaCuestionario" as ui
control ":CuestionarioController" as ctrl
control ":GestorCuestionario" as gestor
entity "c:Cuestionario" as c
entity "e:EstadoCuestionario" as e
entity "b:BloqueCuestionario" as b
control ":FabricaEstadoCuestionario" as fab
participant ":RepositorioConfiguracion" as conf
participant ":CuestionarioDAO" as qdao
database ":BD PostgreSQL" as bd

actor -> ui : ingresa autenticado desde CU001 (SesionDTO con idCuestionario)
activate ui
ui -> ctrl : ingresar(idCuestionario)
activate ctrl
ctrl -> gestor : ingresar(idCuestionario)
activate gestor
gestor -> qdao : buscarPorId(idCuestionario)
activate qdao
qdao -> bd : SELECT Cuestionario, Bloque_Cuestionario, asigna
activate bd
bd --> qdao : filas
deactivate bd
qdao --> gestor : c
deactivate qdao
gestor -> c : registrarAcceso(ahora)
activate c
c -> e : registrarAcceso(c, ahora)
activate e
note right of e
  Patrón State: cada estado decide.
  Todo ingreso suma un acceso y actualiza ultimoIngreso (D14).
  EstadoActivo: si ahora - fechaCreacion > vigenciaDias
    -> c.cambiarEstado("SIN_CONTESTAR", ahora) y devuelve false.
  EstadoActivo y EstadoEnProceso: si accesos > maxAccesos
    -> c.cambiarEstado("INCOMPLETO", ahora) y devuelve false.
  EstadoEnProceso: si ahora - fechaInicio > tiempoMaximo
    -> c.cambiarEstado("INCOMPLETO", ahora) y devuelve false.
  Completo, Incompleto, SinContestar: devuelven false.
end note
e --> c : puedeContinuar: boolean
deactivate e
c --> gestor : puedeContinuar: boolean
deactivate c
gestor -> qdao : actualizar(c)
activate qdao
qdao -> bd : UPDATE Cuestionario (estado, fecha_cambio_estado, accesos, ultimo_ingreso)
activate bd
bd --> qdao : 1 fila
deactivate bd
qdao --> gestor : true
deactivate qdao

alt Precondición: se superó la cantidad máxima de accesos (pasa a Incompleto)
  gestor --> ctrl : CuestionarioDTO(estado INCOMPLETO, "Se superó la cantidad máxima de accesos permitidos")
  ctrl --> ui : CuestionarioDTO
  ui --> actor : muestra el mensaje y el CU finaliza
else 1.A: superó el tiempo máximo para completarlo (pasa a Incompleto)
  gestor --> ctrl : CuestionarioDTO(estado INCOMPLETO, "Se superó el tiempo máximo para completar el cuestionario")
  ctrl --> ui : CuestionarioDTO
  ui --> actor : avisa la situación y el CU finaliza [1.A.2, 1.A.3]
else 1.B: superó el tiempo máximo permitido en estado activo (pasa a Sin Contestar)
  gestor --> ctrl : CuestionarioDTO(estado SIN_CONTESTAR, "El cuestionario venció sin ser contestado")
  ctrl --> ui : CuestionarioDTO
  ui --> actor : avisa la situación y el CU finaliza
else 1.C: ya había ingresado antes (estado En proceso)
  gestor -> c : getBloquePendiente()
  activate c
  c --> gestor : b
  deactivate c
  gestor -> gestor : crear BloqueDTO(b) con sus preguntas en el orden guardado\n(PreguntaCuestionarioDTO con opciones, sin ponderaciones)
  gestor --> ctrl : CuestionarioDTO(estado EN_PROCESO, bloqueActual)
  ctrl --> ui : CuestionarioDTO
  ui --> actor : se posiciona en el bloque que aún no fue respondido (continúa en el paso 5)
else paso 1: primer ingreso (estado Activo)
  gestor -> conf : getInstrucciones()
  activate conf
  conf --> gestor : instrucciones
  deactivate conf
  gestor --> ctrl : CuestionarioDTO(estado ACTIVO, instrucciones)
  deactivate gestor
  ctrl --> ui : CuestionarioDTO
  deactivate ctrl
  ui --> actor : pantalla "Cuestionario - Instrucciones" con "Aceptar" y "Cancelar" [paso 1]
  alt 2.A: el actor presiona "Cancelar"
    actor -> ui : presiona "Cancelar"
    ui --> actor : el CU termina (el cuestionario sigue Activo)
  else paso 2: lee las instrucciones y presiona "Aceptar"
    actor -> ui : presiona "Aceptar"
    ui -> ctrl : comenzar(idCuestionario)
    activate ctrl
    ctrl -> gestor : comenzar(idCuestionario)
    activate gestor
    group Transacción (paso 3)
      gestor -> qdao : buscarPorId(idCuestionario)
      activate qdao
      qdao -> bd : SELECT Cuestionario y la copia del modelo de su evaluación
      qdao --> gestor : c
      deactivate qdao
      gestor -> gestor : sortearPreguntas(c.getEvaluacion()) [Observación (1)]
      activate gestor
      loop por cada CompetenciaEvaluada de la evaluación
        loop por cada FactorEvaluado de la competencia (todos tienen 2 o más preguntas)
          gestor -> gestor : tomar 2 PreguntaEvaluada al azar
        end
      end
      gestor -> gestor : mezclar todas las preguntas elegidas
      deactivate gestor
      gestor -> gestor : armarBloques(preguntas, c.getPreguntasPorBloque())
      loop por cada grupo de preguntasPorBloque preguntas
        create b
        gestor -> b : <<create>> BloqueCuestionario(nroBloque, preguntasDelBloque)
      end
      gestor -> c : iniciar(bloques, ahora)
      activate c
      c -> e : iniciar(c, ahora)
      activate e
      e -> c : cambiarEstado("EN_PROCESO", ahora)\n(fechaInicio = ahora: desde acá se mide el tiempo máximo)
      activate c
      c -> fab : crearEstado("EN_PROCESO")
      activate fab
      fab --> c : EstadoEnProceso
      deactivate fab
      deactivate c
      deactivate e
      deactivate c
      gestor -> qdao : actualizar(c)
      activate qdao
      qdao -> bd : UPDATE Cuestionario (EN_PROCESO, fecha_inicio)
      loop por cada bloque
        qdao -> bd : INSERT Bloque_Cuestionario
        qdao -> bd : INSERT asigna (pregunta y orden)
      end
      bd --> qdao : OK
      qdao --> gestor : true
      deactivate qdao
    end
    gestor --> ctrl : BloqueDTO (primer bloque)
    deactivate gestor
    ctrl --> ui : BloqueDTO
    deactivate ctrl
    ui --> actor : pantalla de Cuestionario (wizard) en el bloque no respondido [paso 4]
  end
end

== Pasos 5 a 8: respuesta de cada bloque ==
loop mientras queden bloques sin responder
  actor -> ui : contesta las preguntas y presiona "Siguiente" (o "Finalizar" en el último) [paso 5]
  ui -> ctrl : responderBloque(idCuestionario, nroBloque, respuestas: List<RespuestaDTO>)
  activate ctrl
  ctrl -> gestor : responderBloque(idCuestionario, nroBloque, respuestas)
  activate gestor
  gestor -> qdao : buscarPorId(idCuestionario)
  activate qdao
  qdao -> bd : SELECT Cuestionario
  qdao --> gestor : c
  deactivate qdao
  gestor -> c : getBloquePendiente()
  activate c
  c --> gestor : b
  deactivate c
  gestor -> gestor : validar que haya una respuesta por cada pregunta de b [paso 6]
  alt 6.A: no respondió todas las preguntas
    gestor --> ctrl : RespuestasIncompletasException(preguntas sin responder)
    ctrl --> ui : 400 + ErrorDTO
    ui --> actor : indica qué preguntas faltan, mantiene el bloque y las respuestas ya marcadas (vuelve al paso 5)
  else respondió todas
    group Transacción (pasos 7 y 8)
      loop por cada RespuestaDTO
        gestor -> b : registrarRespuesta(preguntaEvaluada, opcionEvaluada, ahora)
        activate b
        b -> b : <<create>> RespuestaCandidato; respondido = estaCompleto()
        deactivate b
      end
      alt 8.A: existen más bloques
        gestor -> qdao : actualizar(c)
        activate qdao
        qdao -> bd : INSERT responde; UPDATE Bloque_Cuestionario (respondido)
        qdao --> gestor : true
        deactivate qdao
        gestor -> c : getBloquePendiente()
        activate c
        c --> gestor : siguiente
        deactivate c
        gestor --> ctrl : CuestionarioDTO(bloqueActual = siguiente)
        ctrl --> ui : CuestionarioDTO
        ui --> actor : presenta el siguiente bloque del wizard (continúa en el paso 5) [8.A.1]
      else paso 8: era el último bloque
        gestor -> gestor : calcularResultados(c) [Observación (3)]
        activate gestor
        loop por cada CompetenciaEvaluada
          loop por cada FactorEvaluado de la competencia
            gestor -> gestor : total del factor = suma de las ponderaciones de las\nopciones elegidas en sus preguntas / 2
            gestor -> "rf:ResultadoFactor" ** : <<create>> ResultadoFactor(factorEvaluado, total)
          end
          gestor -> gestor : total de la competencia = suma de los totales de los factores /\ncantidad de factores evaluados
          gestor -> "r:Resultado" ** : <<create>> Resultado(competenciaEvaluada, total, resultadosFactor)
        end
        deactivate gestor
        gestor -> c : completar(resultados, ahora)
        activate c
        c -> e : completar(c, ahora)
        activate e
        e -> c : cambiarEstado("COMPLETO", ahora) (fechaFin = ahora)
        activate c
        c -> fab : crearEstado("COMPLETO")
        activate fab
        fab --> c : EstadoCompleto
        deactivate fab
        deactivate c
        deactivate e
        deactivate c
        gestor -> qdao : actualizar(c)
        activate qdao
        qdao -> bd : INSERT responde; UPDATE Bloque_Cuestionario
        qdao -> bd : INSERT obtiene_resultado; INSERT resultado_factor
        qdao -> bd : UPDATE Cuestionario (COMPLETO, fecha_fin)
        bd --> qdao : OK
        qdao --> gestor : true
        deactivate qdao
        gestor --> ctrl : CuestionarioDTO(estado COMPLETO, "El Cuestionario ha sido completado con éxito")
        deactivate gestor
        ctrl --> ui : CuestionarioDTO
        deactivate ctrl
        ui --> actor : "El Cuestionario ha sido completado con éxito"; el CU termina [pasos 8 y 9]
      end
    end
  end
end
deactivate ui

note over ui, bd
  No hay botón "Anterior": un bloque respondido no se puede volver a ver ni modificar.
  Los parámetros (preguntas por bloque, tiempo máximo, vigencia y máximo de accesos) se copiaron
  al cuestionario desde el RepositorioConfiguracion cuando se creó en CU025.
end note
@enduml
