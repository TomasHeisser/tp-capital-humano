@startuml
skinparam style strictuml
skinparam maxMessageSize 230
skinparam sequenceMessageAlign left
skinparam ParticipantPadding 8
skinparam BoxPadding 8
autonumber

title Diagrama de Secuencia (versión final) - CU025: Evaluar a Candidatos

actor "Consultor" as actor
boundary ":PantallaEvaluarCandidatos" as ui
control ":EvaluacionController" as ectrl
control ":PuestoController" as pctrl
control ":GestorEvaluaciones" as gestor
control ":GestorPuestos" as gpue
entity "p:Puesto" as p
entity "f:Factor" as f
entity "ev:Evaluacion" as ev
entity "ce:CompetenciaEvaluada" as ce
entity "fe:FactorEvaluado" as fe
entity "c:Cuestionario" as c
participant ":RepositorioConfiguracion" as conf
participant ":ExportadorClaves\n(ExportadorExcel)" as exp
participant ":CandidatoDAO" as cdao
participant ":CuestionarioDAO" as qdao
participant ":PuestoDAO" as pdao
participant ":ConsultorDAO" as condao
participant ":EvaluacionDAO" as edao
database ":BD PostgreSQL" as bd

actor -> ui : selecciona del menú "Evaluar Candidatos" [paso 1]
activate ui
ui --> actor : pantalla de Búsqueda de Candidatos: Apellido, Nombre, Número de Candidato [paso 2]

== Pasos 3 a 5: búsqueda y selección de candidatos ==
loop mientras el actor busque, agregue o quite candidatos
  actor -> ui : completa el/los filtros y presiona "Buscar" [paso 3]
  ui -> ectrl : buscarCandidatos(f: FiltroCandidatoDTO)
  activate ectrl
  ectrl -> gestor : buscarCandidatos(f: FiltroCandidatoDTO)
  activate gestor
  gestor -> gestor : validar que haya al menos un filtro
  break no se completó ningún filtro
    gestor --> ectrl : ValidacionException("Debe completar al menos un filtro")
    ectrl --> ui : 400 + ErrorDTO
    ui --> actor : muestra el mensaje sin tapar campos ni botones (vuelve al paso 3)
  end
  gestor -> cdao : buscarPorFiltros(apellido, nombre, nroCandidato)
  activate cdao
  cdao -> bd : SELECT Candidato (sin baja, "contiene")
  activate bd
  bd --> cdao : filas
  deactivate bd
  cdao --> gestor : List<Candidato>
  deactivate cdao
  loop por cada Candidato
    gestor -> gestor : crear CandidatoGestionDTO
  end
  gestor --> ectrl : List<CandidatoGestionDTO>
  deactivate gestor
  ectrl --> ui : List<CandidatoGestionDTO>
  deactivate ectrl
  ui --> actor : completa la grilla de candidatos

  actor -> ui : selecciona candidato/s y presiona "Agregar" [paso 4]
  alt 4.A: no se seleccionaron candidatos
    ui --> actor : "Se debe seleccionar uno o más candidatos" (vuelve al paso 4)
  else hay candidatos seleccionados
    ui --> actor : agrega los candidatos a la grilla "Candidatos a evaluar" [paso 5]
  end
  opt 5.A: selecciona candidatos de "Candidatos a evaluar" y presiona "Quitar"
    ui --> actor : quita los candidatos de la grilla (vuelve al paso 4)
  end
end

== Paso 6: control de cuestionarios activos (corrección E5: loop por candidato) ==
actor -> ui : presiona "Siguiente" [paso 6]
ui -> ectrl : verificarActivos(idsCandidatos: List<Long>)
activate ectrl
ectrl -> gestor : verificarCuestionariosActivos(ids: List<Long>)
activate gestor
loop por cada idCandidato de ids
  gestor -> qdao : buscarActivoOEnProceso(idCandidato)
  activate qdao
  qdao -> bd : SELECT Cuestionario (estado ACTIVO o EN_PROCESO del candidato)
  activate bd
  bd --> qdao : fila o vacío
  deactivate bd
  qdao --> gestor : Optional<Cuestionario>
  deactivate qdao
  opt el candidato tiene un cuestionario activo o en proceso
    gestor -> gestor : agregar CandidatoGestionDTO a conActivos
  end
end
gestor --> ectrl : conActivos: List<CandidatoGestionDTO>
deactivate gestor
ectrl --> ui : conActivos: List<CandidatoGestionDTO>
deactivate ectrl

alt 6.A: conActivos no está vacía
  ui --> actor : indica quiénes poseen cuestionarios activos o en proceso y que no pueden\nser evaluados nuevamente (vuelve al paso 4)
else ningún candidato tiene cuestionarios activos
  ui --> actor : pantalla de Evaluación de Candidatos [paso 7]

  == Paso 8: búsqueda del puesto (corrección E5: buscar y traer PuestoDTO) ==
  actor -> ui : ingresa criterios del puesto y presiona "Buscar"
  ui -> pctrl : buscar(filtro: FiltroPuestoDTO)
  activate pctrl
  pctrl -> gpue : buscarPuestos(filtro: FiltroPuestoDTO)
  activate gpue
  gpue -> pdao : buscarPorFiltros(codigo, nombre, empresa)
  activate pdao
  pdao -> bd : SELECT Puesto JOIN Empresa (activos)
  activate bd
  bd --> pdao : filas
  deactivate bd
  pdao --> gpue : List<Puesto>
  deactivate pdao
  loop por cada Puesto
    gpue -> gpue : crear PuestoDTO(id, codigo, nombre, empresa)
  end
  gpue --> pctrl : List<PuestoDTO>
  deactivate gpue
  pctrl --> ui : List<PuestoDTO>
  deactivate pctrl
  ui --> actor : muestra nombre del puesto y nombre de la empresa
  actor -> ui : selecciona la función o puesto [paso 8]

  ui -> ectrl : validarPuesto(idPuesto)
  activate ectrl
  ectrl -> gestor : validarPuesto(idPuesto)
  activate gestor
  gestor -> pdao : buscarPorId(idPuesto)
  activate pdao
  pdao -> bd : SELECT Puesto, Competencia_Requerida, Factor, Pregunta
  activate bd
  bd --> pdao : filas
  deactivate bd
  pdao --> gestor : p
  deactivate pdao
  gestor -> p : getCompetenciasRequeridas()
  activate p
  p --> gestor : List<CompetenciaRequerida>
  deactivate p
  loop por cada CompetenciaRequerida cr
    loop por cada f de cr.getCompetencia().getFactores()
      gestor -> f : getPreguntasActivas()
      activate f
      f --> gestor : List<Pregunta>
      deactivate f
      gestor -> gestor : contar factores con 2 o más preguntas activas
    end
    opt la competencia no tiene ningún factor evaluable
      gestor -> gestor : agregar el nombre a noEvaluables
    end
  end
  alt 9.A: noEvaluables no está vacía
    gestor --> ectrl : CompetenciasNoEvaluablesException(noEvaluables)
    ectrl --> ui : 422 + ErrorDTO
    ui --> actor : muestra los nombres de las competencias que no pueden ser evaluadas
    actor -> ui : presiona "Aceptar"
    ui --> actor : el CU finaliza y vuelve a la pantalla principal [9.A.3]
  else todas las competencias son evaluables
    gestor --> ectrl : List<CompetenciaRequeridaDTO>
    deactivate gestor
    ectrl --> ui : List<CompetenciaRequeridaDTO>
    deactivate ectrl
    ui --> actor : muestra a modo informativo las competencias y su ponderación requerida [paso 9]

    actor -> ui : presiona "Siguiente" [paso 10]
    alt 10.A: no se seleccionó ninguna función
      ui --> actor : "Se debe seleccionar una función" (vuelve al paso 8)
    else función seleccionada
      ui -> ectrl : generarClaves(idsCandidatos)
      activate ectrl
      ectrl -> gestor : generarClaves(ids: List<Long>)
      activate gestor
      loop por cada idCandidato
        gestor -> cdao : buscarPorId(idCandidato)
        activate cdao
        cdao -> bd : SELECT Candidato
        cdao --> gestor : candidato
        deactivate cdao
        gestor -> gestor : generarClave() (8 caracteres con letras y dígitos)
        gestor -> gestor : crear CandidatoClaveDTO(candidato, clave)
      end
      gestor --> ectrl : List<CandidatoClaveDTO>
      deactivate gestor
      ectrl --> ui : List<CandidatoClaveDTO>
      deactivate ectrl
      ui --> actor : lista de Candidatos a evaluar: Apellido, Nombre, Tipo documento,\nNro documento, Clave de ingreso [paso 11]

      alt 12.A: el actor presiona "Cancelar"
        actor -> ui : presiona "Cancelar"
        ui --> actor : vuelve a la pantalla principal y el CU finaliza
      else paso 12: el actor presiona "Finalizar"
        actor -> ui : presiona "Finalizar"
        ui -> ectrl : registrar(datos: NuevaEvaluacionDTO) (las mismas claves mostradas)
        activate ectrl
        ectrl -> gestor : registrarEvaluacion(datos: NuevaEvaluacionDTO, username)
        activate gestor
        group Transacción (evaluación + copia del modelo + N cuestionarios)
          gestor -> pdao : buscarPorId(datos.idPuesto)
          activate pdao
          pdao -> bd : SELECT Puesto
          pdao --> gestor : p
          deactivate pdao
          gestor -> condao : buscarPorUsername(username)
          activate condao
          condao -> bd : SELECT Consultor
          condao --> gestor : consultor
          deactivate condao
          create ev
          gestor -> ev : <<create>> Evaluacion(ahora, p, consultor)

          note over gestor, fe : copiarModelo(p): requerimiento especial "mismo modelo"
          loop por cada CompetenciaRequerida cr de p
            create ce
            gestor -> ce : <<create>> CompetenciaEvaluada(cr.getCompetencia(), cr.getPonderacion())
            loop por cada f de la competencia con 2 o más preguntas activas
              create fe
              gestor -> fe : <<create>> FactorEvaluado(f)
              loop por cada pregunta activa pr de f
                gestor -> fe : agregarPregunta(new PreguntaEvaluada(pr))\n(copia enunciado, opciones y ponderaciones)
              end
              gestor -> ce : agregarFactor(fe)
            end
            gestor -> ev : agregarCompetenciaEvaluada(ce)
          end

          gestor -> conf : getPreguntasPorBloque(), getTiempoMaximoMinutos(),\ngetVigenciaDias(), getMaxAccesos()
          activate conf
          conf --> gestor : parámetros
          deactivate conf
          loop por cada CandidatoClaveDTO de datos.candidatos
            gestor -> cdao : buscarPorId(idCandidato)
            activate cdao
            cdao -> bd : SELECT Candidato
            cdao --> gestor : candidato
            deactivate cdao
            create c
            gestor -> c : <<create>> Cuestionario(candidato, clave, parámetros, ahora)\n(estado inicial: Activo)
            gestor -> ev : agregarCuestionario(c)
          end
          gestor -> edao : guardar(ev)
          activate edao
          edao -> bd : INSERT Evaluacion
          edao -> bd : INSERT Competencia_Evaluada, Factor_Evaluado,\nPregunta_Evaluada, Opcion_Evaluada
          loop por cada Cuestionario de ev
            edao -> bd : INSERT Cuestionario (estado ACTIVO, clave)
          end
          bd --> edao : OK
          edao --> gestor : true
          deactivate edao
        end
        gestor --> ectrl : EvaluacionDTO
        deactivate gestor
        ectrl --> ui : EvaluacionDTO
        deactivate ectrl
        ui --> actor : "¿Desea exportar la lista de candidatos con sus claves a un archivo Excel?" [paso 13]

        loop mientras el actor cancele la elección de carpeta (16.A vuelve al paso 14)
          alt 14.A: no desea exportar
            actor -> ui : elige "NO"
            ui --> actor : el CU finaliza [14.A.1]
          else paso 14: desea exportar
            actor -> ui : elige "SI"
            ui -> ectrl : exportarClaves(idEvaluacion)
            activate ectrl
            ectrl -> gestor : exportarClaves(idEvaluacion)
            activate gestor
            gestor -> edao : buscarPorId(idEvaluacion)
            activate edao
            edao -> bd : SELECT Evaluacion, Cuestionario, Candidato
            edao --> gestor : ev
            deactivate edao
            loop por cada Cuestionario de ev
              gestor -> gestor : crear CandidatoClaveDTO(candidato, clave)
            end
            gestor -> exp : exportar(nombrePuesto, fechaCreacion, claves: List<CandidatoClaveDTO>)
            activate exp
            exp -> exp : nombreArchivo() = aaaa-mm-dd-hh-mm-NombreFunción.xlsx
            exp --> gestor : ArchivoDTO
            deactivate exp
            gestor --> ectrl : ArchivoDTO
            deactivate gestor
            ectrl --> ui : archivo (descarga)
            deactivate ectrl
            ui --> actor : el navegador muestra el diálogo para elegir la carpeta [paso 15]
            alt 16.A: el actor presiona "Cancelar"
              actor -> ui : presiona "Cancelar"
              ui --> actor : vuelve a preguntar si desea exportar (paso 14)
            else paso 16: elige la carpeta y presiona "Guardar"
              actor -> ui : elige la carpeta y presiona "Guardar"
              ui --> actor : se guarda el archivo [paso 17]
            end
          end
        end
        ui --> actor : el CU finaliza y vuelve a la pantalla principal [paso 18]
      end
    end
  end
end
deactivate ui
@enduml
