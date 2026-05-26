
**Requisitos previos:** Tener instalado Docker Desktop (abierto y corriendo en segundo plano) y Git.

#### Paso 1: Clonar el repositorio y preparar el entorno

Abre tu terminal y clona el proyecto. Una vez dentro de la carpeta raíz, necesitas crear tu archivo de variables de entorno copiando la plantilla.

**En Windows (PowerShell/CMD):**

~~~bash
# Clonar y entrar a la carpeta (cambia la URL por la de su repo)
git clone <URL_DEL_REPOSITORIO>
cd ProyectoFinal-BD

# Crear el archivo .env o colocarlo
~~~

#### Paso 2: Crear la carpeta para las imágenes físicas

El backend necesita una carpeta local para guardar y leer las fotos de los vehículos. Creadla en la raíz del proyecto:

**En Windows:**

```
mkdir uploads\vehiculos
```


> **Nota:** Aquí deberán colocar manualmente las imágenes de prueba (ej. `nissan-versa.jpg`) para que el catálogo web las pueda mostrar.

#### Paso 3: Construir y levantar los contenedores

Este comando descargará SQL Server, construirá las imágenes del backend (Spring Boot) y frontend (Vue), y dejará todo corriendo en segundo plano.

~~~bash
docker-compose up -d --build
~~~

**Ejecutar este comando dentro de la terminal de Docker para que funcione las consultas:**

~~~bash
docker exec -it concesionaria_db /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "SqlS3rv3r_2026!" -d ConcesionariaTec -C -Q "REVOKE CONTROL ON SCHEMA::dbo FROM Rol_AppConcesionaria; REVOKE ALTER ON SCHEMA::dbo FROM Rol_AppConcesionaria; GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO Rol_AppConcesionaria;"
~~~

### 🚀 ¡Listo para trabajar!

- **Frontend (Vue.js):** Disponible en [http://localhost:8081](https://www.google.com/search?q=http://localhost:8081)
    
- **Backend (Spring Boot API):** Disponible en [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080)

**Para detener el proyecto al final del día:**

```
docker-compose stop
```

**Para borrar todo y empezar desde cero (en caso de error):**

```bash
docker-compose down -v
docker-compose up --build

#Opcional
docker-compose up -d --build #Este es mas tardado ya que vuelve a iniciar todo.
```