-- Capit@l Humano: creacion de tablas (sale del DER)
-- Lo corre el contenedor de PostgreSQL la primera vez (docker-compose) o a mano con psql.

SET TIME ZONE 'America/Argentina/Buenos_Aires';

-- ---------- usuarios (herencia: usuario -> consultor / candidato) ----------
CREATE TABLE usuario (
    id   BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('CONSULTOR', 'CANDIDATO'))
);

CREATE TABLE consultor (
    id       BIGINT PRIMARY KEY REFERENCES usuario (id),
    username VARCHAR(15) NOT NULL UNIQUE
);

CREATE TABLE candidato (
    id               BIGINT PRIMARY KEY REFERENCES usuario (id),
    nro_candidato    BIGINT       NOT NULL UNIQUE,
    apellido         VARCHAR(100) NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    tipo_documento   VARCHAR(3)   NOT NULL CHECK (tipo_documento IN ('DNI', 'LE', 'LC', 'PP')),
    nro_documento    VARCHAR(20)  NOT NULL,
    fecha_nacimiento DATE,
    genero           CHAR(1) CHECK (genero IN ('H', 'M')),
    nacionalidad     VARCHAR(100),
    email            VARCHAR(100),
    escolaridad      VARCHAR(100),
    fecha_baja       TIMESTAMP,
    UNIQUE (tipo_documento, nro_documento)
);

-- ---------- puestos y competencias ----------
CREATE TABLE empresa (
    id     BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE puesto (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(20)  NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    id_empresa  BIGINT       NOT NULL REFERENCES empresa (id),
    fecha_baja  TIMESTAMP
);

CREATE TABLE competencia (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(20)  NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    fecha_baja  TIMESTAMP
);

CREATE TABLE requiere (
    id_puesto      BIGINT        NOT NULL REFERENCES puesto (id),
    id_competencia BIGINT        NOT NULL REFERENCES competencia (id),
    ponderacion    NUMERIC(4, 2) NOT NULL CHECK (ponderacion BETWEEN 0 AND 10),
    PRIMARY KEY (id_puesto, id_competencia)
);

CREATE TABLE factor (
    id             BIGSERIAL PRIMARY KEY,
    codigo         VARCHAR(20)  NOT NULL UNIQUE,
    nombre         VARCHAR(100) NOT NULL,
    descripcion    VARCHAR(500),
    nro_orden      INTEGER      NOT NULL CHECK (nro_orden > 0),
    id_competencia BIGINT       NOT NULL REFERENCES competencia (id),
    fecha_baja     TIMESTAMP,
    UNIQUE (id_competencia, nombre),
    UNIQUE (id_competencia, nro_orden)
);

-- ---------- opciones de respuesta y preguntas ----------
CREATE TABLE opcion_respuesta (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    fecha_baja  TIMESTAMP
);

CREATE TABLE item_opcion (
    id                  BIGSERIAL PRIMARY KEY,
    id_opcion_respuesta BIGINT       NOT NULL REFERENCES opcion_respuesta (id),
    orden               INTEGER      NOT NULL CHECK (orden > 0),
    descripcion         VARCHAR(500) NOT NULL,
    UNIQUE (id_opcion_respuesta, orden),
    UNIQUE (id_opcion_respuesta, descripcion)
);

CREATE TABLE pregunta (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    enunciado           VARCHAR(500) NOT NULL,
    descripcion         VARCHAR(500),
    id_factor           BIGINT       NOT NULL REFERENCES factor (id),
    id_opcion_respuesta BIGINT       NOT NULL REFERENCES opcion_respuesta (id),
    fecha_baja          TIMESTAMP
);

-- una pregunta dada de baja no cuenta para los duplicados
CREATE UNIQUE INDEX ux_pregunta_factor_nombre ON pregunta (id_factor, nombre) WHERE fecha_baja IS NULL;
CREATE UNIQUE INDEX ux_pregunta_factor_enunciado ON pregunta (id_factor, enunciado) WHERE fecha_baja IS NULL;

CREATE TABLE pondera (
    id_pregunta BIGINT        NOT NULL REFERENCES pregunta (id),
    id_item     BIGINT        NOT NULL REFERENCES item_opcion (id),
    ponderacion NUMERIC(4, 2) NOT NULL CHECK (ponderacion >= 0),
    PRIMARY KEY (id_pregunta, id_item)
);

-- ---------- evaluaciones y copia del modelo ----------
CREATE TABLE evaluacion (
    id             BIGSERIAL PRIMARY KEY,
    fecha_creacion TIMESTAMP NOT NULL,
    id_puesto      BIGINT    NOT NULL REFERENCES puesto (id),
    id_consultor   BIGINT    NOT NULL REFERENCES consultor (id)
);

CREATE TABLE competencia_evaluada (
    id                    BIGSERIAL PRIMARY KEY,
    id_evaluacion         BIGINT        NOT NULL REFERENCES evaluacion (id),
    id_competencia_origen BIGINT        NOT NULL REFERENCES competencia (id),
    codigo                VARCHAR(20)   NOT NULL,
    nombre                VARCHAR(100)  NOT NULL,
    ponderacion_minima    NUMERIC(4, 2) NOT NULL
);

CREATE TABLE factor_evaluado (
    id                      BIGSERIAL PRIMARY KEY,
    id_competencia_evaluada BIGINT       NOT NULL REFERENCES competencia_evaluada (id),
    id_factor_origen        BIGINT       NOT NULL REFERENCES factor (id),
    codigo                  VARCHAR(20)  NOT NULL,
    nombre                  VARCHAR(100) NOT NULL,
    nro_orden               INTEGER      NOT NULL
);

CREATE TABLE pregunta_evaluada (
    id                 BIGSERIAL PRIMARY KEY,
    id_factor_evaluado BIGINT       NOT NULL REFERENCES factor_evaluado (id),
    id_pregunta_origen BIGINT       NOT NULL REFERENCES pregunta (id),
    nombre             VARCHAR(100) NOT NULL,
    enunciado          VARCHAR(500) NOT NULL
);

CREATE TABLE opcion_evaluada (
    id                   BIGSERIAL PRIMARY KEY,
    id_pregunta_evaluada BIGINT        NOT NULL REFERENCES pregunta_evaluada (id),
    orden                INTEGER       NOT NULL,
    descripcion          VARCHAR(500)  NOT NULL,
    ponderacion          NUMERIC(4, 2) NOT NULL
);

-- ---------- cuestionarios ----------
CREATE TABLE cuestionario (
    id                  BIGSERIAL PRIMARY KEY,
    id_evaluacion       BIGINT      NOT NULL REFERENCES evaluacion (id),
    id_candidato        BIGINT      NOT NULL REFERENCES candidato (id),
    clave               VARCHAR(8)  NOT NULL,
    estado              VARCHAR(20) NOT NULL
        CHECK (estado IN ('ACTIVO', 'EN_PROCESO', 'COMPLETO', 'INCOMPLETO', 'SIN_CONTESTAR')),
    fecha_creacion      TIMESTAMP   NOT NULL,
    fecha_inicio        TIMESTAMP,
    fecha_fin           TIMESTAMP,
    ultimo_ingreso      TIMESTAMP,
    fecha_cambio_estado TIMESTAMP   NOT NULL,
    cantidad_accesos    INTEGER     NOT NULL DEFAULT 0,
    preguntas_por_bloque INTEGER    NOT NULL,
    tiempo_maximo_min   INTEGER     NOT NULL,
    vigencia_dias       INTEGER     NOT NULL,
    max_accesos         INTEGER     NOT NULL,
    UNIQUE (id_evaluacion, id_candidato)
);

CREATE INDEX ix_cuestionario_estado ON cuestionario (estado);

CREATE TABLE bloque_cuestionario (
    id_cuestionario BIGINT  NOT NULL REFERENCES cuestionario (id),
    nro_bloque      INTEGER NOT NULL,
    respondido      BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id_cuestionario, nro_bloque)
);

CREATE TABLE asigna (
    id_cuestionario      BIGINT  NOT NULL,
    nro_bloque           INTEGER NOT NULL,
    id_pregunta_evaluada BIGINT  NOT NULL REFERENCES pregunta_evaluada (id),
    orden                INTEGER NOT NULL,
    PRIMARY KEY (id_cuestionario, nro_bloque, id_pregunta_evaluada),
    FOREIGN KEY (id_cuestionario, nro_bloque) REFERENCES bloque_cuestionario (id_cuestionario, nro_bloque)
);

CREATE TABLE responde (
    id_cuestionario      BIGINT    NOT NULL,
    nro_bloque           INTEGER   NOT NULL,
    id_pregunta_evaluada BIGINT    NOT NULL REFERENCES pregunta_evaluada (id),
    id_opcion_evaluada   BIGINT    NOT NULL REFERENCES opcion_evaluada (id),
    fecha_respuesta      TIMESTAMP NOT NULL,
    PRIMARY KEY (id_cuestionario, nro_bloque, id_pregunta_evaluada),
    FOREIGN KEY (id_cuestionario, nro_bloque) REFERENCES bloque_cuestionario (id_cuestionario, nro_bloque)
);

CREATE TABLE obtiene_resultado (
    id_cuestionario         BIGINT        NOT NULL REFERENCES cuestionario (id),
    id_competencia_evaluada BIGINT        NOT NULL REFERENCES competencia_evaluada (id),
    puntaje                 NUMERIC(5, 2) NOT NULL,
    PRIMARY KEY (id_cuestionario, id_competencia_evaluada)
);

CREATE TABLE resultado_factor (
    id_cuestionario    BIGINT        NOT NULL REFERENCES cuestionario (id),
    id_factor_evaluado BIGINT        NOT NULL REFERENCES factor_evaluado (id),
    puntaje            NUMERIC(5, 2) NOT NULL,
    PRIMARY KEY (id_cuestionario, id_factor_evaluado)
);

-- ---------- auditoria y ejecuciones ----------
CREATE TABLE registro_auditoria (
    id                 BIGSERIAL PRIMARY KEY,
    fecha_hora         TIMESTAMP   NOT NULL,
    accion             VARCHAR(20) NOT NULL,
    entidad_afectada   VARCHAR(50) NOT NULL,
    id_objeto_afectado BIGINT      NOT NULL,
    id_consultor       BIGINT      NOT NULL REFERENCES consultor (id)
);

CREATE TABLE registro_ejecucion (
    id         BIGSERIAL PRIMARY KEY,
    fecha_hora TIMESTAMP   NOT NULL,
    accion     VARCHAR(10) NOT NULL CHECK (accion IN ('INICIO', 'FIN'))
);
