 Sistema de Gestión de Concesionaria

 Reglas de Colaboración (DevOps)
Para mantener la integridad del código, todos los miembros deben seguir estas normas:

Prohibido hacer push directo a las ramas main o develop.

Todo cambio debe realizarse mediante un Pull Request (PR).

El PR solo será aprobado por el encargado de DevOps una vez que se verifique que el código compila y cumple con los estándares.

📝 Convención de Commits
Seguimos el estándar de Conventional Commits. Cada mensaje debe tener el formato: tipo: descripción breve.

Ejemplos:

feat: Agregar una nueva funcionalidad (ej: feat: módulo de registro de clientes).

fix: Corrección de errores (ej: fix: validar stock negativo).

docs: Cambios en la documentación.

refactor: Cambios en el código que no corrigen errores ni añaden funciones.

test: Agregar o modificar pruebas.

ci: Configuración de integración continua/workflows.

Ejemplo correcto: git commit -m "feat: agregar filtro por marca en inventario"

------Instrucciones para el Flujo de Trabajo en GitHub------

Cada vez que tengan una tarea asignada, sigan estos pasos:

1. Preparar su entorno (Solo al iniciar)
Si es la primera vez que clonan el repo:

Bash

git clone https://github.com/MrChetoZzz/Concesionaria-ABD.git

git checkout develop

-------------------------------------------------------------------

2. Crear una rama para su tarea (Cada vez que inicien algo nuevo)
Siempre partan desde develop para tener la versión más reciente del código:

Bash

git checkout develop

git pull origin develop

git checkout -b feature/nombre-de-su-tarea

(Ejemplo: git checkout -b feature/login-usuario)

------------------------------------------------------------------

3. Realizar los cambios y hacer el commit
Una vez terminen su código, guárdenlo siguiendo la convención: tipo: descripcion.

Bash
git add .

git commit -m "feat: agregar validacion de contrasena en login"

Tipos permitidos: feat, fix, docs, refactor, test, ci.

-------------------------------------------------------------------

4. Subir los cambios y crear el Pull Request
Suban su rama al servidor:

Bash

git push origin feature/nombre-de-su-tarea

Después de esto, entren a GitHub.

Verán un botón amarillo que dice "Compare & pull request".

Ábranlo, asegúrense de que el base sea develop y el compare sea su rama feature.

Asígnenme como revisor para que yo pueda validar y aprobar su trabajo.

⚠️ Notas importantes:
Nunca hagan git push directo a develop o main.

Si necesitan actualizar su rama con lo que otros han subido a develop, usen: git pull origin develop.

Si tienen dudas con los comandos o surge un conflicto, avísenme antes de intentar resolverlo para evitar errores.
