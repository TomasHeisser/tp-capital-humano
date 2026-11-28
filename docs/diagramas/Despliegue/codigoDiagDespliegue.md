@startuml
skinparam nodesep 70
skinparam ranksep 80
skinparam shadowing false
skinparam defaultFontSize 12

title Diagrama de Despliegue (versión final) - Sistema Capit@l Humano - Grupo 32

node "PC del Consultor" as pc_consultor <<device>> {
  node "Navegador web" as nav_consultor <<executionEnvironment>> {
    artifact "Páginas del consultor\n(Next.js: gestión, evaluación y reportes)" as art_ui_consultor
  }
  artifact "candidatos.csv\n(entrada CU006)" as art_csv
  artifact "aaaa-mm-dd-hh-mm-NombreFunción.xlsx\n(claves de CU025)" as art_xlsx
}

node "PC del Candidato\n(remota o en la consultora)" as pc_candidato <<device>> {
  node "Navegador web " as nav_candidato <<executionEnvironment>> {
    artifact "Páginas del candidato\n(Next.js: login y cuestionario CU026)" as art_ui_candidato
  }
}

node "Impresora" as impresora <<device>>

node "Servidor de Aplicaciones" as srv_app <<device>> {
  node "Node.js 22 LTS" as env_node <<executionEnvironment>> {
    artifact "capital-humano-frontend\n(Next.js)" as art_front
  }
  node "JVM (Java 25)" as env_jvm <<executionEnvironment>> {
    artifact "capital-humano-backend.jar\n(Spring Boot con Tomcat embebido:\nAPI REST, gestores, DAO JPA,\ntarea programada CU032)" as art_jar
    artifact "application.properties\n(repositorio de configuraciones:\ninstrucciones y parámetros del cuestionario)" as art_props
  }
}

node "Servidor de Base de Datos" as srv_bd <<device>> {
  node "Docker" as env_docker <<executionEnvironment>> {
    node "PostgreSQL 16" as env_pg <<executionEnvironment>> {
      artifact "capital_humano\n(esquema + datos iniciales:\nempresas y consultores)" as art_db
    }
  }
}

node "Servidor LDAP de la organización" as srv_ldap <<device>> {
  artifact "directorio de consultores\n(usuario hasta 15 y contraseña hasta 20 caracteres)" as art_ldap
}

node "Servidor de IA (Google Gemini)" as srv_ia <<device>> {
  artifact "API generativa\n(CU033)" as art_ia
}

node "Servidor del sistema de\nGestión de Candidatos y CV" as srv_cv <<device>> {
  artifact "sistema de Candidatos y CV\n(origen del .csv de CU006)" as art_cv
}

pc_consultor "1..*" -down- "1" srv_app : <<HTTPS>>
pc_candidato "1..*" -down- "1" srv_app : <<HTTPS>>
env_node "1" -right- "1" env_jvm : <<HTTP/REST JSON>>
pc_consultor "1..*" -left- "1" impresora : <<USB / IPP>>\nimpresión CU027 y CU028
pc_consultor "1..*" -left- "1" srv_cv : <<SMB>>\ncarpeta compartida (supuesto)
srv_app "1" -down- "1" srv_bd : <<TCP/IP - JDBC>>
srv_app "1" -down- "1" srv_ldap : <<LDAP>>\nCU001
srv_app "1" -down- "1" srv_ia : <<HTTPS - REST/JSON>>\nCU033
impresora -[hidden]down- srv_cv

note bottom of srv_cv
  Supuesto: el consultor obtiene el .csv del sistema de
  Candidatos y CV desde una carpeta compartida y lo sube
  con el selector de archivos del navegador (CU006).
end note

note bottom of srv_ldap
  En desarrollo y en el coloquio se usa un LDAP embebido
  (Spring LDAP con un archivo .ldif) que cumple el mismo rol.
end note
@enduml
