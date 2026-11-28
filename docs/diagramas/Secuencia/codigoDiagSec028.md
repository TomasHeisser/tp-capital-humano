@startuml
skinparam style strictuml
skinparam maxMessageSize 230
skinparam sequenceMessageAlign left
skinparam ParticipantPadding 10
skinparam BoxPadding 8
autonumber

title Diagrama de Secuencia (versión final) - CU028: Emitir Orden de Mérito

actor "Consultor" as actor
boundary ":PantallaOrdenMerito" as ui
control ":ReporteController" as ctrl
control ":GestorReportes" as gestor
control "estrategia:ReporteOrdenMerito" as est
entity "c:Cuestionario" as c
participant ":PuestoDAO" as pdao
participant ":EvaluacionDAO" as edao
participant ":CuestionarioDAO" as qdao
database ":BD PostgreSQL" as bd
participant "Impresora" as imp

actor -> ui : selecciona del menú "Orden de Méritos" [paso 1]
activate ui
ui --> actor : pantalla en blanco con Código, Nombre del Puesto y Empresa,\ny los botones "Buscar" y "Cancelar" [paso 2]

alt 2.A: el actor presiona "Cancelar"
  actor -> ui : presiona "Cancelar"
  ui --> actor : el CU termina
else paso 3: ingresa al menos un criterio y presiona "Buscar"
  actor -> ui : ingresa criterio/s y presiona "Buscar"
  ui -> ctrl : buscarPuestos(f: FiltroPuestoDTO)
  activate ctrl
  ctrl -> gestor : buscarPuestos(f: FiltroPuestoDTO)
  activate gestor
  gestor -> gestor : validar que haya al menos un criterio
  break no se ingresó ningún criterio
    gestor --> ctrl : ValidacionException("Debe ingresar al menos un criterio de búsqueda")
    ctrl --> ui : 400 + ErrorDTO
    ui --> actor : muestra el mensaje sin tapar campos ni botones (vuelve al paso 3)
  end
  gestor -> pdao : buscarPorFiltros(codigo, nombre, empresa)
  activate pdao
  pdao -> bd : SELECT Puesto JOIN Empresa ("contiene")
  activate bd
  bd --> pdao : filas
  deactivate bd
  pdao --> gestor : List<Puesto>
  deactivate pdao
  loop por cada Puesto p
    gestor -> qdao : contarCandidatosPorPuesto(p.id)
    activate qdao
    qdao -> bd : SELECT COUNT(DISTINCT candidato) de los cuestionarios del puesto
    qdao --> gestor : cantidadCandidatos
    deactivate qdao
    gestor -> qdao : contarCompletosPorPuesto(p.id)
    activate qdao
    qdao -> bd : SELECT COUNT cuestionarios del puesto en estado COMPLETO
    qdao --> gestor : evaluacionesCompletas
    deactivate qdao
    gestor -> gestor : crear PuestoEstadisticaDTO
  end
  gestor --> ctrl : List<PuestoEstadisticaDTO>
  deactivate gestor
  ctrl --> ui : List<PuestoEstadisticaDTO>
  deactivate ctrl

  loop hasta que el puesto elegido tenga evaluaciones (6.A vuelve al paso 4)
    ui --> actor : grilla: Código, Nombre del puesto, Empresa, Candidatos,\nEvaluaciones Completas [paso 4]
    actor -> ui : selecciona un puesto y pulsa "Emitir el orden de mérito" [paso 5]
    ui -> ctrl : listarEvaluaciones(idPuesto)
    activate ctrl
    ctrl -> gestor : listarEvaluaciones(idPuesto)
    activate gestor
    gestor -> edao : buscarPorPuesto(idPuesto)
    activate edao
    edao -> bd : SELECT Evaluacion del puesto
    activate bd
    bd --> edao : filas
    deactivate bd
    edao --> gestor : List<Evaluacion>
    deactivate edao
    loop por cada Evaluacion
      gestor -> gestor : crear EvaluacionDTO(id, fechaCreacion, cantidadCandidatos)
    end
    gestor --> ctrl : List<EvaluacionDTO>
    deactivate gestor
    ctrl --> ui : List<EvaluacionDTO>
    deactivate ctrl
    alt 6.A: el puesto no posee evaluaciones (lista vacía)
      ui --> actor : "El puesto seleccionado no posee evaluaciones" (continúa en el paso 4)
    end
  end

  ui --> actor : pide elegir la/s evaluación/es (fecha de creación y cantidad de candidatos)\no la opción TODAS [paso 6]
  actor -> ui : elige evaluación/es o TODAS
  ui -> ctrl : emitir(ORDEN_MERITO, criterio: CriterioReporteDTO(idPuesto, idsEvaluaciones))
  activate ctrl
  ctrl -> gestor : emitirReporte(ORDEN_MERITO, criterio)
  activate gestor
  gestor -> gestor : estrategia = estrategias.get(ORDEN_MERITO)
  gestor -> est : generar(criterio)
  activate est
  est -> pdao : buscarPorId(idPuesto)
  activate pdao
  pdao -> bd : SELECT Puesto JOIN Empresa
  pdao --> est : puesto
  deactivate pdao
  est -> qdao : buscarPorEvaluaciones(idsEvaluaciones)
  activate qdao
  qdao -> bd : SELECT Cuestionario, Candidato, resultados (de las evaluaciones elegidas)
  activate bd
  bd --> qdao : filas
  deactivate bd
  qdao --> est : List<Cuestionario>
  deactivate qdao
  loop por cada c de la lista (una fila por cuestionario)
    est -> c : getEstado().getNombre()
    activate c
    c --> est : estado
    deactivate c
    alt estado COMPLETO
      est -> c : getResultados()
      activate c
      c --> est : List<Resultado>
      deactivate c
      est -> est : puntaje = puntajeTotal(c) (promedio de los puntajes de competencia)
      alt alcanzoMinimos(c): en cada competencia, puntaje >= ponderación mínima
        est -> est : agregar fila a ordenMerito (puntaje, fecha inicio, fecha fin, accesos)
      else no alcanzó el mínimo en alguna competencia
        est -> est : agregar fila a noAlcanzaronMinimo (puntaje, fecha inicio, fecha fin, accesos)
      end
    else otro estado (En proceso, Activo, Incompleto o Sin contestar)
      est -> est : agregar fila a restoPorEstado[estado] sin puntaje\n(fecha inicio, último ingreso, accesos; "—" si no existe)
    end
  end
  est -> est : ordenar(ordenMerito): mayor puntaje, menor tiempo (fin - inicio),\nmenos accesos, apellido y nombre
  est -> est : ordenar(noAlcanzaronMinimo) por puntaje descendente
  est -> "om:OrdenMeritoDTO" ** : <<create>> OrdenMeritoDTO(puesto, ordenMerito,\nnoAlcanzaronMinimo, restoPorEstado, ahora)
  est --> gestor : om
  deactivate est
  gestor --> ctrl : OrdenMeritoDTO
  deactivate gestor
  ctrl --> ui : OrdenMeritoDTO
  deactivate ctrl
  ui --> actor : emite el listado con encabezado (puesto, empresa, fecha): orden de mérito,\nno alcanzaron el mínimo y resto agrupado por estado [paso 7]

  actor -> ui : selecciona "Imprimir" [paso 8]
  ui -> ui : imprimir() (diálogo de impresión del navegador)
  ui -> imp : envía el listado a la impresora [paso 9]
  ui --> actor : el CU termina [paso 10]
end
deactivate ui

note over gestor, est
  Patrón Strategy: GestorReportes (contexto) elige la estrategia según el tipo de reporte;
  ReporteComparativo (CU027) es la otra estrategia. El DTO no tiene lógica: la clasificación
  y el orden los hace la estrategia.
end note
@enduml
