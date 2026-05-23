🚗 Sistema de Gestión de Concesionaria (Concesionaria-ABD)
Bienvenido al repositorio oficial del sistema de gestión de nuestra concesionaria. Este proyecto centraliza el control de inventario, ventas y clientes utilizando arquitecturas modernas de bases de datos relacionales y NoSQL.


🛠 Reglas de Colaboración (DevOps)
Para mantener la integridad del código, todos los miembros deben seguir estas normas:

Prohibido hacer push directo a las ramas main o develop.

Todo cambio debe realizarse mediante un Pull Request (PR).

El PR solo será aprobado por el encargado de DevOps una vez que se verifique que el código compila y cumple con los estándares.

📝 Convención de Commits
Seguimos el estándar de Conventional Commits. Cada mensaje debe tener el formato: tipo: descripción breve.

feat: Agregar una nueva funcionalidad (ej: feat: módulo de registro de clientes).

fix: Corrección de errores (ej: fix: validar stock negativo).

docs: Cambios en la documentación.

refactor: Cambios en el código que no corrigen errores ni añaden funciones.

test: Agregar o modificar pruebas.

ci: Configuración de integración continua/workflows.

Ejemplo correcto: git commit -m "feat: agregar filtro por marca en inventario"

🌿 Flujo de Trabajo
Utilizamos una estrategia de ramas estricta:

main: Rama de producción (código estable y listo para entrega).

develop: Rama de integración (donde se une el trabajo de todos).

feature/*: Ramas individuales para cada tarea (ej: feature/login, feature/reportes).

Ciclo de vida de una tarea:

Crear rama desde develop: git checkout -b feature/nombre-tarea

Trabajar y hacer commits.

Subir rama: git push origin feature/nombre-tarea

Crear Pull Request hacia develop en GitHub.
