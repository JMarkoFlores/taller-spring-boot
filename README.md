# API REST AWS

Proyecto Spring Boot para gestión de API REST.

---

## Requisitos Previos

Antes de arrancar el proyecto en tu entorno local, asegúrate de contar con:

- **Java JDK 21** (o versión compatible configurada en `pom.xml`).
- **PostgreSQL** instalado y en ejecución en tu equipo.
- Base de datos **`api-rest-aws`** creada en PostgreSQL (si aún no existe):
  ```sql
  CREATE DATABASE "api-rest-aws";
  ```

---

## Configuración Local

El proyecto utiliza `spring.config.import=optional:file:./config-local.properties` en `src/main/resources/application.properties` para cargar automáticamente las variables de entorno de la base de datos desde un archivo local fuera del control de versiones. Esto significa que **no es necesario ejecutar comandos `set` ni definir variables de entorno en la consola cada vez que inicias la aplicación**.

Para configurar tus credenciales locales:

1. Abre una consola (por ejemplo, Windows CMD) en la raíz del proyecto (donde se encuentra `pom.xml`).
2. Copia la plantilla de configuración ejecutando una sola vez:

```cmd
copy config-local.properties.example config-local.properties
```

3. Abre el archivo `config-local.properties` recién creado y coloca tus valores locales:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=api-rest-aws
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_contrasena
```

> **Nota:** El archivo `config-local.properties` está incluido en `.gitignore` y nunca debe subirse al repositorio Git.

---

## Ejecución del Proyecto

Una vez configurado `config-local.properties`, arranca la aplicación desde la carpeta que contiene `pom.xml` ejecutando:

En Windows CMD / PowerShell:
```cmd
mvnw spring-boot:run
```

O si usas Linux / macOS:
```bash
./mvnw spring-boot:run
```
