-- =============================================================================
-- 0. CREACIÓN DE LA BASE DE DATOS
-- =============================================================================
USE master;
GO

IF NOT EXISTS (SELECT name FROM master.sys.databases WHERE name = N'ConcesionariaTec')
BEGIN
    CREATE DATABASE [ConcesionariaTec];
END
GO

USE [ConcesionariaTec];
GO

-- =============================================================================
-- 1. ESTRUCTURA (Tablas y Relaciones)
-- =============================================================================
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Persona](
	[IdPersona] [int] IDENTITY(1,1) NOT NULL,
	[Nombre] [nvarchar](150) NOT NULL,
	[FechaNacimiento] [date] NULL,
	[Domicilio] [nvarchar](255) NULL,
	[CurpCifrada] [varbinary](256) NULL,
	[CurpHash] [varbinary](32) NULL,
	[FechaCreacion] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
	[FechaModificacion] [datetime2](0) NULL,
PRIMARY KEY CLUSTERED ([IdPersona] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Cliente](
	[IdCliente] [int] IDENTITY(1,1) NOT NULL,
	[IdPersona] [int] NOT NULL,
	[EstaActivo] [bit] NOT NULL DEFAULT ((1)),
	[FechaIngreso] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
	[FechaModificacion] [datetime2](0) NULL,
PRIMARY KEY CLUSTERED ([IdCliente] ASC),
CONSTRAINT [UQ_Cliente_IdPersona] UNIQUE NONCLUSTERED ([IdPersona] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[EspecializacionMecanico](
	[IdEspecializacionMecanico] [smallint] IDENTITY(1,1) NOT NULL,
	[Especializacion] [nvarchar](30) NOT NULL,
	[Descripcion] [nvarchar](400) NULL,
PRIMARY KEY CLUSTERED ([IdEspecializacionMecanico] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Mecanico](
	[IdMecanico] [int] IDENTITY(1,1) NOT NULL,
	[IdPersona] [int] NOT NULL,
	[IdEspecializacionMecanico] [smallint] NOT NULL,
	[EstaActivo] [bit] NOT NULL DEFAULT ((1)),
	[FechaIngreso] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
	[FechaModificacion] [datetime2](0) NULL,
PRIMARY KEY CLUSTERED ([IdMecanico] ASC),
CONSTRAINT [UQ_Mecanico_IdPersona] UNIQUE NONCLUSTERED ([IdPersona] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[VehiculoCondicion](
	[IdVehiculoCondicion] [tinyint] IDENTITY(1,1) NOT NULL,
	[DescripcionEs] [nvarchar](50) NOT NULL,
	[DescripcionEn] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED ([IdVehiculoCondicion] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Vehiculo](
	[IdVehiculo] [int] IDENTITY(1,1) NOT NULL,
	[Marca] [nvarchar](50) NOT NULL,
	[Modelo] [nvarchar](50) NOT NULL,
	[AnioModelo] [smallint] NOT NULL,
	[Placas] [nvarchar](15) NULL,
	[NumeroSerie] [nvarchar](17) NULL,
	[Costo] [decimal](14, 2) NULL,
	[IdVehiculoCondicion] [tinyint] NOT NULL,
	[FechaRegistro] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
PRIMARY KEY CLUSTERED ([IdVehiculo] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[TipoPago](
	[IdTipoPago] [tinyint] IDENTITY(1,1) NOT NULL,
	[TipoPago] [nvarchar](40) NOT NULL,
PRIMARY KEY CLUSTERED ([IdTipoPago] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[RolUsuario](
	[IdRolUsuario] [smallint] IDENTITY(1,1) NOT NULL,
	[RolNombreEs] [nvarchar](25) NOT NULL,
	[RolNombreEn] [nvarchar](25) NOT NULL,
	[DescripcionEs] [nvarchar](100) NULL,
	[DescripcionEn] [nvarchar](100) NULL,
PRIMARY KEY CLUSTERED ([IdRolUsuario] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Usuario](
	[IdUsuario] [int] IDENTITY(1,1) NOT NULL,
	[IdPersona] [int] NOT NULL,
	[IdRolUsuario] [smallint] NOT NULL,
	[Email] [nvarchar](254) NOT NULL,
	[NombreUsuario] [nvarchar](50) NULL,
	[PasswordHash] [varchar](255) NOT NULL,
	[EstaActivo] [bit] NOT NULL DEFAULT ((1)),
	[EmailVerificado] [bit] NOT NULL DEFAULT ((0)),
	[IntentosFallidos] [smallint] NOT NULL DEFAULT ((0)),
	[BloqueadoHasta] [datetime2](0) NULL,
	[UltimoLogin] [datetime2](0) NULL,
	[FechaCreacion] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
	[FechaModificacion] [datetime2](0) NULL,
PRIMARY KEY CLUSTERED ([IdUsuario] ASC),
CONSTRAINT [UQ_Usuario_Email] UNIQUE NONCLUSTERED ([Email] ASC),
CONSTRAINT [UQ_Usuario_IdPersona] UNIQUE NONCLUSTERED ([IdPersona] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[UsuarioSesion](
	[IdUsuarioSesion] [bigint] IDENTITY(1,1) NOT NULL,
	[IdUsuario] [int] NOT NULL,
	[RefreshTokenHash] [char](64) NOT NULL,
	[JwtId] [uniqueidentifier] NOT NULL,
	[FechaCreacion] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
	[FechaExpiracion] [datetime2](0) NOT NULL,
	[FechaRevocacion] [datetime2](0) NULL,
	[IpCreacion] [varchar](45) NULL,
	[UserAgent] [nvarchar](300) NULL,
PRIMARY KEY CLUSTERED ([IdUsuarioSesion] ASC),
CONSTRAINT [UQ_UsuarioSesion_JwtId] UNIQUE NONCLUSTERED ([JwtId] ASC),
CONSTRAINT [UQ_UsuarioSesion_RefreshTokenHash] UNIQUE NONCLUSTERED ([RefreshTokenHash] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[VentaEstado](
	[IdVentaEstado] [tinyint] IDENTITY(1,1) NOT NULL,
	[DescripcionEs] [nvarchar](30) NOT NULL,
	[DescripcionEn] [nvarchar](30) NOT NULL,
PRIMARY KEY CLUSTERED ([IdVentaEstado] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Venta](
	[IdVenta] [int] IDENTITY(1,1) NOT NULL,
	[IdUsuario] [int] NOT NULL,
	[IdCliente] [int] NOT NULL,
	[IdVehiculo] [int] NOT NULL,
	[IdVentaEstado] [tinyint] NOT NULL,
	[CostoTotal] [decimal](14, 2) NOT NULL,
	[FechaIngreso] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
PRIMARY KEY CLUSTERED ([IdVenta] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[MovimientoFinanciero](
	[IdMovimientoFinanciero] [int] IDENTITY(1,1) NOT NULL,
	[IdVenta] [int] NOT NULL,
	[IdTipoPago] [tinyint] NOT NULL,
	[Monto] [decimal](14, 2) NOT NULL,
	[FechaIngreso] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
PRIMARY KEY CLUSTERED ([IdMovimientoFinanciero] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[ReparacionEstado](
	[IdReparacionEstado] [tinyint] IDENTITY(1,1) NOT NULL,
	[DescripcionEs] [nvarchar](50) NOT NULL,
	[DescripcionEn] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED ([IdReparacionEstado] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[VehiculoReparacion](
	[IdVehiculoReparacion] [int] IDENTITY(1,1) NOT NULL,
	[IdVehiculo] [int] NOT NULL,
	[IdMecanico] [int] NOT NULL,
	[IdCliente] [int] NOT NULL,
	[IdReparacionEstado] [tinyint] NOT NULL,
	[FechaIngreso] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
	[FechaSalida] [datetime2](0) NULL,
	[DescripcionProblema] [nvarchar](max) NOT NULL,
	[CostoEstimado] [decimal](14, 2) NULL,
	[CostoFinal] [decimal](14, 2) NULL,
PRIMARY KEY CLUSTERED ([IdVehiculoReparacion] ASC)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [dbo].[VehiculoDiagnostico](
	[IdVehiculoDiagnostico] [int] IDENTITY(1,1) NOT NULL,
	[IdVehiculoReparacion] [int] NOT NULL,
	[FechaIngreso] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
	[Descripcion] [nvarchar](max) NOT NULL,
PRIMARY KEY CLUSTERED ([IdVehiculoDiagnostico] ASC)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

-- LLAVES FORÁNEAS Y CHECKS
ALTER TABLE [dbo].[Cliente] WITH CHECK ADD CONSTRAINT [FK_Cliente_Persona] FOREIGN KEY([IdPersona]) REFERENCES [dbo].[Persona] ([IdPersona]);
ALTER TABLE [dbo].[Mecanico] WITH CHECK ADD CONSTRAINT [FK_Mecanico_Especializacion] FOREIGN KEY([IdEspecializacionMecanico]) REFERENCES [dbo].[EspecializacionMecanico] ([IdEspecializacionMecanico]);
ALTER TABLE [dbo].[Mecanico] WITH CHECK ADD CONSTRAINT [FK_Mecanico_Persona] FOREIGN KEY([IdPersona]) REFERENCES [dbo].[Persona] ([IdPersona]);
ALTER TABLE [dbo].[Vehiculo] WITH CHECK ADD CONSTRAINT [FK_Vehiculo_Condicion] FOREIGN KEY([IdVehiculoCondicion]) REFERENCES [dbo].[VehiculoCondicion] ([IdVehiculoCondicion]);
ALTER TABLE [dbo].[Usuario] WITH CHECK ADD CONSTRAINT [FK_Usuario_Persona] FOREIGN KEY([IdPersona]) REFERENCES [dbo].[Persona] ([IdPersona]);
ALTER TABLE [dbo].[Usuario] WITH CHECK ADD CONSTRAINT [FK_Usuario_RolUsuario] FOREIGN KEY([IdRolUsuario]) REFERENCES [dbo].[RolUsuario] ([IdRolUsuario]);
ALTER TABLE [dbo].[UsuarioSesion] WITH CHECK ADD CONSTRAINT [FK_UsuarioSesion_Usuario] FOREIGN KEY([IdUsuario]) REFERENCES [dbo].[Usuario] ([IdUsuario]);
ALTER TABLE [dbo].[Venta] WITH CHECK ADD CONSTRAINT [FK_Venta_Cliente] FOREIGN KEY([IdCliente]) REFERENCES [dbo].[Cliente] ([IdCliente]);
ALTER TABLE [dbo].[Venta] WITH CHECK ADD CONSTRAINT [FK_Venta_Estado] FOREIGN KEY([IdVentaEstado]) REFERENCES [dbo].[VentaEstado] ([IdVentaEstado]);
ALTER TABLE [dbo].[Venta] WITH CHECK ADD CONSTRAINT [FK_Venta_Usuario] FOREIGN KEY([IdUsuario]) REFERENCES [dbo].[Usuario] ([IdUsuario]);
ALTER TABLE [dbo].[Venta] WITH CHECK ADD CONSTRAINT [FK_Venta_Vehiculo] FOREIGN KEY([IdVehiculo]) REFERENCES [dbo].[Vehiculo] ([IdVehiculo]);
ALTER TABLE [dbo].[MovimientoFinanciero] WITH CHECK ADD CONSTRAINT [FK_MovimientoFinanciero_TipoPago] FOREIGN KEY([IdTipoPago]) REFERENCES [dbo].[TipoPago] ([IdTipoPago]);
ALTER TABLE [dbo].[MovimientoFinanciero] WITH CHECK ADD CONSTRAINT [FK_MovimientoFinanciero_Venta] FOREIGN KEY([IdVenta]) REFERENCES [dbo].[Venta] ([IdVenta]);
ALTER TABLE [dbo].[VehiculoReparacion] WITH CHECK ADD CONSTRAINT [FK_VehiculoReparacion_Cliente] FOREIGN KEY([IdCliente]) REFERENCES [dbo].[Cliente] ([IdCliente]);
ALTER TABLE [dbo].[VehiculoReparacion] WITH CHECK ADD CONSTRAINT [FK_VehiculoReparacion_Estado] FOREIGN KEY([IdReparacionEstado]) REFERENCES [dbo].[ReparacionEstado] ([IdReparacionEstado]);
ALTER TABLE [dbo].[VehiculoReparacion] WITH CHECK ADD CONSTRAINT [FK_VehiculoReparacion_Mecanico] FOREIGN KEY([IdMecanico]) REFERENCES [dbo].[Mecanico] ([IdMecanico]);
ALTER TABLE [dbo].[VehiculoReparacion] WITH CHECK ADD CONSTRAINT [FK_VehiculoReparacion_Vehiculo] FOREIGN KEY([IdVehiculo]) REFERENCES [dbo].[Vehiculo] ([IdVehiculo]);
ALTER TABLE [dbo].[VehiculoDiagnostico] WITH CHECK ADD CONSTRAINT [FK_VehiculoDiagnostico_Reparacion] FOREIGN KEY([IdVehiculoReparacion]) REFERENCES [dbo].[VehiculoReparacion] ([IdVehiculoReparacion]);

ALTER TABLE [dbo].[MovimientoFinanciero] WITH CHECK ADD CONSTRAINT [CK_MovimientoFinanciero_Monto] CHECK (([Monto]>=(0)));
ALTER TABLE [dbo].[Usuario] WITH CHECK ADD CONSTRAINT [CK_Usuario_IntentosFallidos] CHECK (([IntentosFallidos]>=(0)));
ALTER TABLE [dbo].[Vehiculo] WITH CHECK ADD CONSTRAINT [CK_Vehiculo_AnioModelo] CHECK (([AnioModelo]>=(1900) AND [AnioModelo]<=(2100)));
ALTER TABLE [dbo].[Vehiculo] WITH CHECK ADD CONSTRAINT [CK_Vehiculo_Costo] CHECK (([Costo] IS NULL OR [Costo]>=(0)));
ALTER TABLE [dbo].[VehiculoReparacion] WITH CHECK ADD CONSTRAINT [CK_VehiculoReparacion_CostoEstimado] CHECK (([CostoEstimado] IS NULL OR [CostoEstimado]>=(0)));
ALTER TABLE [dbo].[VehiculoReparacion] WITH CHECK ADD CONSTRAINT [CK_VehiculoReparacion_CostoFinal] CHECK (([CostoFinal] IS NULL OR [CostoFinal]>=(0)));
ALTER TABLE [dbo].[VehiculoReparacion] WITH CHECK ADD CONSTRAINT [CK_VehiculoReparacion_Fechas] CHECK (([FechaSalida] IS NULL OR [FechaSalida]>=[FechaIngreso]));
ALTER TABLE [dbo].[Venta] WITH CHECK ADD CONSTRAINT [CK_Venta_CostoTotal] CHECK (([CostoTotal]>=(0)));
GO

-- FIX: VehiculoImagen se crea aquí (Sección 1) para que el GRANT de la Sección 2 pueda ejecutarse correctamente.
-- Si se crea después del RBAC, el GRANT falla silenciosamente porque la tabla aún no existe.
CREATE TABLE [dbo].[VehiculoImagen](
    [IdVehiculoImagen] [int] IDENTITY(1,1) NOT NULL,
    [IdVehiculo] [int] NOT NULL,
    [RutaImagen] [nvarchar](255) NOT NULL,
    [EsPrincipal] [bit] NOT NULL DEFAULT 0,
    [FechaCreacion] [datetime2](0) NOT NULL DEFAULT (sysutcdatetime()),
PRIMARY KEY CLUSTERED ([IdVehiculoImagen] ASC),
CONSTRAINT [FK_VehiculoImagen_Vehiculo] FOREIGN KEY([IdVehiculo]) REFERENCES [dbo].[Vehiculo] ([IdVehiculo])
)
GO

-- =============================================================================
-- 2. SEGURIDAD Y RBAC (Corregido para Docker)
-- =============================================================================
USE master;
GO

-- Login del servidor
IF NOT EXISTS (
    SELECT 1
    FROM sys.server_principals
    WHERE name = N'Login_AppConcesionaria'
)
BEGIN
    CREATE LOGIN Login_AppConcesionaria
    WITH PASSWORD = 'App6535vs8.';
END
GO

USE ConcesionariaTec;
GO

-- Usuario dentro de la base
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE name = N'Usuario_AppConcesionaria'
)
BEGIN
    CREATE USER Usuario_AppConcesionaria
    FOR LOGIN Login_AppConcesionaria;
END
GO

-- Por si ya existe pero quedó desalineado
ALTER USER Usuario_AppConcesionaria
WITH LOGIN = Login_AppConcesionaria;
GO

-- Rol para la app
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE type = 'R' AND name = N'Rol_AppConcesionaria'
)
BEGIN
    CREATE ROLE Rol_AppConcesionaria;
END
GO

ALTER ROLE Rol_AppConcesionaria ADD MEMBER Usuario_AppConcesionaria;
GO

-- Permisos para el esquema completo dbo
GRANT SELECT, INSERT, UPDATE, DELETE
ON SCHEMA::dbo
TO Rol_AppConcesionaria;
GO

-- Evita que la app borre tablas completas accidentalmente
-- (Esto no bloquea el DELETE de filas, solo el DROP de tablas)
DENY TAKE OWNERSHIP ON SCHEMA::dbo TO Rol_AppConcesionaria;

-- Adicional: Evitar que el usuario ejecute comandos a nivel base de datos
DENY CREATE TABLE TO Rol_AppConcesionaria;
DENY CREATE PROCEDURE TO Rol_AppConcesionaria;
DENY CREATE VIEW TO Rol_AppConcesionaria;
DENY ALTER ON SCHEMA::dbo TO Rol_AppConcesionaria;
GO


-- Jr Developer

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'Login_DiegoM')
BEGIN
    CREATE LOGIN [Login_DiegoM] WITH PASSWORD = 'Diego6535vs1.';
END
GO

-- Developers

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'Login_CarlosS')
BEGIN
    CREATE LOGIN [Login_CarlosS] WITH PASSWORD = 'Carlos6535vs1.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'Login_VictorH')
BEGIN
    CREATE LOGIN [Login_VictorH] WITH PASSWORD = 'Victor6535vs1.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'Login_EmmanuelS')
BEGIN
    CREATE LOGIN [Login_EmmanuelS] WITH PASSWORD = 'Emmanuelvs1.';
END

GO

-- DBA

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = N'Login_CesarV')
BEGIN
    CREATE LOGIN [Login_CesarV] WITH PASSWORD = 'CesarDBAvs1.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'User_DiegoM')
BEGIN
    CREATE USER [User_DiegoM] FOR LOGIN [Login_DiegoM];
END
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'User_CarlosS')
BEGIN
    CREATE USER [User_CarlosS] FOR LOGIN [Login_CarlosS];
END
GO


IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'User_VictorH')
BEGIN

    CREATE USER [User_VictorH] FOR LOGIN [Login_VictorH];
END
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'User_EmmanuelS')
BEGIN
    CREATE USER [User_EmmanuelS] FOR LOGIN [Login_EmmanuelS];
END
GO


-- Cesar Vazquez es el DBA. Le damos sysadmin para control total.
ALTER SERVER ROLE [sysadmin] ADD MEMBER [Login_CesarV];
GO



-- =============================================
-- ROL 2: JR DEVELOPER (Diego Molina)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'Rol_JrDeveloper')
BEGIN
    CREATE ROLE Rol_JrDeveloper;
END
GO

-- Acceso base: solo lectura en todo el esquema
GRANT SELECT ON SCHEMA::dbo TO Rol_JrDeveloper;

-- Restricciones explícitas: no puede leer datos sensibles de estas tablas

DENY SELECT ON dbo.Persona TO Rol_JrDeveloper;
DENY SELECT ON dbo.Usuario TO Rol_JrDeveloper;
DENY SELECT ON dbo.Venta TO Rol_JrDeveloper;
DENY SELECT ON dbo.MovimientoFinanciero TO Rol_JrDeveloper;

-- Al solo otorgar SELECT y negar explícitamente, no podrá modificar datos ni estructuras.
ALTER ROLE Rol_JrDeveloper ADD MEMBER [User_DiegoM];
GO

-- =============================================
-- ROL 3: DEVELOPER (Carlos, Víctor, Emmanuel)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'Rol_Developer')
BEGIN
    CREATE ROLE Rol_Developer;
END
GO

-- CRUD completo en todas las tablas para pruebas y desarrollo
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO Rol_Developer;

-- Restricciones explícitas de integridad financiera:
-- Pueden ver las ventas y movimientos, pero NO pueden crearlos, modificarlos ni borrarlos.
DENY INSERT, UPDATE, DELETE ON dbo.Venta TO Rol_Developer;
DENY INSERT, UPDATE, DELETE ON dbo.MovimientoFinanciero TO Rol_Developer;

-- Asignar los tres desarrolladores a este rol
ALTER ROLE Rol_Developer ADD MEMBER [User_CarlosS];
ALTER ROLE Rol_Developer ADD MEMBER [User_VictorH];
ALTER ROLE Rol_Developer ADD MEMBER [User_EmmanuelS];
GO



-- =============================================================================
-- 3. DATOS DE PRUEBA (Catálogos y Registros)
-- =============================================================================
-- VehiculoCondicion
SET IDENTITY_INSERT dbo.VehiculoCondicion ON;
INSERT INTO dbo.VehiculoCondicion (IdVehiculoCondicion, DescripcionEs, DescripcionEn) VALUES
(1, N'Nuevo', N'New'), (2, N'Usado', N'Used');
SET IDENTITY_INSERT dbo.VehiculoCondicion OFF;

-- ReparacionEstado
SET IDENTITY_INSERT dbo.ReparacionEstado ON;
INSERT INTO dbo.ReparacionEstado (IdReparacionEstado, DescripcionEs, DescripcionEn) VALUES
(1, N'Pendiente', N'Pending'), (2, N'En progreso', N'In progress'),
(3, N'Completada', N'Completed'), (4, N'Cancelada', N'Cancelled');
SET IDENTITY_INSERT dbo.ReparacionEstado OFF;

-- VentaEstado
SET IDENTITY_INSERT dbo.VentaEstado ON;
INSERT INTO dbo.VentaEstado (IdVentaEstado, DescripcionEs, DescripcionEn) VALUES
(1, N'Pendiente', N'Pending'), (2, N'Completada', N'Completed'), (3, N'Cancelada', N'Cancelled');
SET IDENTITY_INSERT dbo.VentaEstado OFF;

-- TipoPago
SET IDENTITY_INSERT dbo.TipoPago ON;
INSERT INTO dbo.TipoPago (IdTipoPago, TipoPago) VALUES
(1, N'Efectivo'), (2, N'Tarjeta de crédito'), (3, N'Tarjeta de débito'),
(4, N'Transferencia bancaria'), (5, N'Financiamiento');
SET IDENTITY_INSERT dbo.TipoPago OFF;

-- EspecializacionMecanico
SET IDENTITY_INSERT dbo.EspecializacionMecanico ON;
INSERT INTO dbo.EspecializacionMecanico (IdEspecializacionMecanico, Especializacion, Descripcion) VALUES
(1, N'Motor', N'Especialista en reparación de motores de combustión interna.'),
(2, N'Transmisión', N'Diagnóstico y reparación de cajas de cambio automáticas y manuales.'),
(3, N'Eléctrico', N'Sistemas eléctricos y electrónicos del automóvil.'),
(4, N'Suspensión y dirección', N'Alineación, amortiguadores y sistemas de dirección asistida.'),
(5, N'Carrocería y pintura', N'Reparación de chasis, soldadura y pintura automotriz.');
SET IDENTITY_INSERT dbo.EspecializacionMecanico OFF;

-- RolUsuario
SET IDENTITY_INSERT dbo.RolUsuario ON;
INSERT INTO dbo.RolUsuario (IdRolUsuario, RolNombreEs, RolNombreEn, DescripcionEs, DescripcionEn) VALUES
(1, N'Administrador', N'Administrator', N'Control total del sistema y gestión de usuarios.', N'Full system control and user management.'),
(2, N'Vendedor', N'Salesperson', N'Gestión de ventas y clientes.', N'Sales and customer management.'),
(3, N'Mecánico', N'Mechanic', N'Acceso a módulos de taller y reparaciones.', N'Workshop and repair module access.');
SET IDENTITY_INSERT dbo.RolUsuario OFF;

-- Personas (Clientes 1-10)
INSERT INTO dbo.Persona (Nombre, FechaNacimiento, Domicilio) VALUES
(N'María Guadalupe Hernández López', '1985-03-15', N'Av. Hidalgo 123, Col. Centro, Nuevo Laredo, Tamps.'),
(N'Juan Carlos Martínez Ruiz', '1990-07-22', N'Calle Juárez 456, Col. Madero, Nuevo Laredo, Tamps.'),
(N'Ana Patricia González Flores', '1978-11-30', N'Blvd. Colosio 789, Col. Las Torres, Nuevo Laredo, Tamps.'),
(N'Roberto Carlos Sánchez Díaz', '1995-01-05', N'Calle Morelos 234, Col. Guerrero, Nuevo Laredo, Tamps.'),
(N'Laura Elena Ramírez Torres', '1988-09-10', N'Av. Reforma 567, Col. Victoria, Nuevo Laredo, Tamps.'),
(N'Miguel Ángel Vázquez Castro', '1982-04-20', N'Calle Mina 890, Col. Aduana, Nuevo Laredo, Tamps.'),
(N'Sofía Alejandra Morales Vargas', '1998-12-12', N'Blvd. Lázaro Cárdenas 345, Col. Mirador, Nuevo Laredo, Tamps.'),
(N'Pedro Luis Ortega Jiménez', '1975-06-18', N'Calle Allende 678, Col. Hidalgo, Nuevo Laredo, Tamps.'),
(N'Gabriela Ivonne Ríos Mendoza', '1992-02-28', N'Av. Universidad 901, Col. Tecnológico, Nuevo Laredo, Tamps.'),
(N'Fernando Javier Cruz Navarro', '1987-10-08', N'Calle Guerrero 112, Col. Juárez, Nuevo Laredo, Tamps.');

-- Personas (Mecánicos 11-15)
INSERT INTO dbo.Persona (Nombre, FechaNacimiento, Domicilio) VALUES
(N'José Alfredo Paredes Luna', '1980-05-12', N'Calle Roble 45, Col. Jardín, Nuevo Laredo, Tamps.'),
(N'Francisco Javier Tovar García', '1983-08-25', N'Av. Las Torres 78, Col. Campestre, Nuevo Laredo, Tamps.'),
(N'Carlos Eduardo Muñoz Herrera', '1991-11-03', N'Calle Cedro 12, Col. La Fe, Nuevo Laredo, Tamps.'),
(N'Luis Enrique Castro Delgado', '1986-09-14', N'Calle Olmo 90, Col. Los Fresnos, Nuevo Laredo, Tamps.'),
(N'Javier Alejandro Núñez Rojas', '1994-04-07', N'Calle Pino 34, Col. Las Alamedas, Nuevo Laredo, Tamps.');

-- Personas (Usuarios 16-20)
INSERT INTO dbo.Persona (Nombre, FechaNacimiento, Domicilio) VALUES
(N'Diego Molina Castillo', '1990-01-15', N'Calle 5 de Febrero 100, Col. Centro, Nuevo Laredo, Tamps.'),
(N'Carlos Sánchez Vázquez', '1988-06-20', N'Av. Guerrero 200, Col. Madero, Nuevo Laredo, Tamps.'),
(N'Víctor Hugo Pérez López', '1992-12-01', N'Calle Zaragoza 300, Col. Victoria, Nuevo Laredo, Tamps.'),
(N'Emmanuel Suárez Ríos', '1995-03-18', N'Blvd. Hidalgo 400, Col. Guerrero, Nuevo Laredo, Tamps.'),
(N'César Vázquez González', '1985-07-30', N'Calle Independencia 500, Col. Aduana, Nuevo Laredo, Tamps.');

-- Clientes
INSERT INTO dbo.Cliente (IdPersona) VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10);

-- Completar campos editables de clientes para validar los formularios
UPDATE dbo.Persona SET Nombre = N'Maria Guadalupe Hernandez Lopez', Domicilio = N'Calle: Hidalgo | Num. Ext.: 123 | Colonia: Centro | Tel.: 8671112233', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'HELM850315MTSRPR01')), CurpHash = HASHBYTES('SHA2_256', 'HELM850315MTSRPR01') WHERE IdPersona = 1;
UPDATE dbo.Persona SET Nombre = N'Juan Carlos Martinez Ruiz', Domicilio = N'Calle: Juarez | Num. Ext.: 456 | Colonia: Madero | Tel.: 8672223344', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'MARJ900722HTSRZN02')), CurpHash = HASHBYTES('SHA2_256', 'MARJ900722HTSRZN02') WHERE IdPersona = 2;
UPDATE dbo.Persona SET Nombre = N'Ana Patricia Gonzalez Flores', Domicilio = N'Calle: Colosio | Num. Ext.: 789 | Colonia: Las Torres | Tel.: 8673334455', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'GOFA781130MTSNLR03')), CurpHash = HASHBYTES('SHA2_256', 'GOFA781130MTSNLR03') WHERE IdPersona = 3;
UPDATE dbo.Persona SET Nombre = N'Roberto Carlos Sanchez Diaz', Domicilio = N'Calle: Morelos | Num. Ext.: 234 | Colonia: Guerrero | Tel.: 8674445566', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'SADR950105HTSNZB04')), CurpHash = HASHBYTES('SHA2_256', 'SADR950105HTSNZB04') WHERE IdPersona = 4;
UPDATE dbo.Persona SET Nombre = N'Laura Elena Ramirez Torres', Domicilio = N'Calle: Reforma | Num. Ext.: 567 | Colonia: Victoria | Tel.: 8675556677', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'RATL880910MTSMRR05')), CurpHash = HASHBYTES('SHA2_256', 'RATL880910MTSMRR05') WHERE IdPersona = 5;
UPDATE dbo.Persona SET Nombre = N'Miguel Angel Vazquez Castro', Domicilio = N'Calle: Mina | Num. Ext.: 890 | Colonia: Aduana | Tel.: 8676667788', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'VACM820420HTSZSG06')), CurpHash = HASHBYTES('SHA2_256', 'VACM820420HTSZSG06') WHERE IdPersona = 6;
UPDATE dbo.Persona SET Nombre = N'Sofia Alejandra Morales Vargas', Domicilio = N'Calle: Lazaro Cardenas | Num. Ext.: 345 | Colonia: Mirador | Tel.: 8677778899', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'MOVS981212MTSRRF07')), CurpHash = HASHBYTES('SHA2_256', 'MOVS981212MTSRRF07') WHERE IdPersona = 7;
UPDATE dbo.Persona SET Nombre = N'Pedro Luis Ortega Jimenez', Domicilio = N'Calle: Allende | Num. Ext.: 678 | Colonia: Hidalgo | Tel.: 8678889900', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'OEJP750618HTSRMD08')), CurpHash = HASHBYTES('SHA2_256', 'OEJP750618HTSRMD08') WHERE IdPersona = 8;
UPDATE dbo.Persona SET Nombre = N'Gabriela Ivonne Rios Mendoza', Domicilio = N'Calle: Universidad | Num. Ext.: 901 | Colonia: Tecnologico | Tel.: 8679990011', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'RIMG920228MTSNSB09')), CurpHash = HASHBYTES('SHA2_256', 'RIMG920228MTSNSB09') WHERE IdPersona = 9;
UPDATE dbo.Persona SET Nombre = N'Fernando Javier Cruz Navarro', Domicilio = N'Calle: Guerrero | Num. Ext.: 112 | Colonia: Juarez | Tel.: 8671011121', CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), 'CUNF871008HTSRVR10')), CurpHash = HASHBYTES('SHA2_256', 'CUNF871008HTSRVR10') WHERE IdPersona = 10;

-- Mecánicos
INSERT INTO dbo.Mecanico (IdPersona, IdEspecializacionMecanico) VALUES
(11, 1), (12, 2), (13, 3), (14, 4), (15, 5);

-- Usuarios
INSERT INTO dbo.Usuario (IdPersona, IdRolUsuario, Email, NombreUsuario, PasswordHash, EstaActivo, EmailVerificado) VALUES
(16, 2, N'diego.molina@concesionaria.com', N'diego.m', N'$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01', 1, 1),
(17, 2, N'carlos.sanchez@concesionaria.com', N'carlos.s', N'$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ02', 1, 1),
(18, 2, N'victor.perez@concesionaria.com', N'victor.p', N'$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ03', 1, 1),
(19, 2, N'emmanuel.suarez@concesionaria.com', N'emmanuel.s', N'$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ04', 1, 1),
(20, 1, N'cesar.vazquez@concesionaria.com', N'cesar.v', N'$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ05', 1, 1);

-- Vehículos
INSERT INTO dbo.Vehiculo (Marca, Modelo, AnioModelo, Placas, NumeroSerie, Costo, IdVehiculoCondicion) VALUES
(N'Nissan', N'Versa', 2025, N'ABC-1234', N'3N1CN7AP2HL123456', 295000.00, 1),
(N'Nissan', N'Sentra', 2024, N'DEF-5678', N'3N1AB7AP2HL654321', 420000.00, 1),
(N'Nissan', N'X-Trail', 2023, N'GHI-9012', N'3N1CD7AP2HL789012', 580000.00, 2),
(N'Toyota', N'Corolla', 2025, N'JKL-3456', N'JTDBR22E2H1234567', 450000.00, 1),
(N'Toyota', N'RAV4', 2024, N'MNO-7890', N'JTMBD31V1H1234567', 720000.00, 1),
(N'Toyota', N'Hilux', 2023, N'PQR-1234', N'MR0FZ29G1H1234567', 850000.00, 2),
(N'Honda', N'Civic', 2025, N'STU-5678', N'19XFC2F55H1234567', 480000.00, 1),
(N'Honda', N'CR-V', 2024, N'VWX-9012', N'2HKRM4H56H1234567', 690000.00, 1),
(N'Dodge', N'Attitude', 2025, N'YZA-3456', N'3C3CFFAR4H1234567', 310000.00, 1),
(N'Dodge', N'Journey', 2024, N'BCD-7890', N'3C4PDCAB2H1234567', 550000.00, 2),
(N'Ford', N'Escape', 2025, N'EFG-1234', N'1FMCU9J92H1234567', 670000.00, 1),
(N'Ford', N'Explorer', 2024, N'HIJ-5678', N'1FM5K8D82H1234567', 920000.00, 1);

-- Unidades adicionales para que el catalogo por marca muestre stock variado
INSERT INTO dbo.Vehiculo (Marca, Modelo, AnioModelo, Placas, NumeroSerie, Costo, IdVehiculoCondicion) VALUES
(N'Nissan', N'Versa', 2025, N'NVS-1001', N'NVERSA0000001001', 295000.00, 1),
(N'Nissan', N'Versa', 2025, N'NVS-1002', N'NVERSA0000001002', 295000.00, 1),
(N'Nissan', N'Sentra', 2024, N'NST-2001', N'NSENTRA000002001', 420000.00, 1),
(N'Nissan', N'X-Trail', 2023, N'NXT-3001', N'NXTRAIL000003001', 580000.00, 2),
(N'Nissan', N'X-Trail', 2023, N'NXT-3002', N'NXTRAIL000003002', 580000.00, 2),
(N'Toyota', N'Corolla', 2025, N'TCR-4001', N'TCOROLLA0004001', 450000.00, 1),
(N'Toyota', N'Corolla', 2025, N'TCR-4002', N'TCOROLLA0004002', 450000.00, 1),
(N'Toyota', N'RAV4', 2024, N'TRV-5001', N'TRAV40000005001', 720000.00, 1),
(N'Toyota', N'Hilux', 2023, N'THX-6001', N'THILUX000006001', 850000.00, 2),
(N'Honda', N'Civic', 2025, N'HCV-7001', N'HCIVIC000007001', 480000.00, 1),
(N'Honda', N'Civic', 2025, N'HCV-7002', N'HCIVIC000007002', 480000.00, 1),
(N'Honda', N'Civic', 2025, N'HCV-7003', N'HCIVIC000007003', 480000.00, 1),
(N'Honda', N'CR-V', 2024, N'HCR-8001', N'HCRV00000008001', 690000.00, 1),
(N'Dodge', N'Attitude', 2025, N'DAT-9001', N'DATTITUDE009001', 310000.00, 1),
(N'Dodge', N'Attitude', 2025, N'DAT-9002', N'DATTITUDE009002', 310000.00, 1),
(N'Dodge', N'Journey', 2024, N'DJR-9101', N'DJOURNEY009101', 550000.00, 2),
(N'Ford', N'Escape', 2025, N'FES-9201', N'FESCAPE00009201', 670000.00, 1),
(N'Ford', N'Escape', 2025, N'FES-9202', N'FESCAPE00009202', 670000.00, 1),
(N'Ford', N'Escape', 2025, N'FES-9203', N'FESCAPE00009203', 670000.00, 1),
(N'Ford', N'Explorer', 2024, N'FEX-9301', N'FEXPLORER009301', 920000.00, 1);

-- Ventas (Se ajustaron los IdUsuario del 1 al 4)
INSERT INTO dbo.Venta (IdUsuario, IdCliente, IdVehiculo, IdVentaEstado, CostoTotal, FechaIngreso) VALUES
(1, 1, 1, 2, 295000.00, '2026-02-01'),
(2, 2, 4, 2, 450000.00, '2026-02-15'),
(3, 3, 7, 2, 480000.00, '2026-03-01'),
(4, 4, 2, 2, 420000.00, '2026-03-10'),
(1, 5, 5, 2, 720000.00, '2026-03-20'),
(2, 6, 9, 2, 310000.00, '2026-04-05'),
(3, 7, 11, 2, 670000.00, '2026-04-10'),
(4, 8, 3, 1, 580000.00, '2026-05-01'),
(1, 9, 6, 1, 850000.00, '2026-05-10'),
(2, 10, 12, 1, 920000.00, '2026-05-15');

-- Movimientos Financieros (Estos estaban bien, se quedan igual)
INSERT INTO dbo.MovimientoFinanciero (IdVenta, IdTipoPago, Monto, FechaIngreso) VALUES 
(1, 1, 295000.00, '2026-02-01'),
(2, 2, 200000.00, '2026-02-15'), (2, 1, 250000.00, '2026-02-15'),
(3, 5, 480000.00, '2026-03-01'),
(4, 4, 420000.00, '2026-03-10'),
(5, 3, 720000.00, '2026-03-20'),
(6, 1, 310000.00, '2026-04-05'),
(7, 2, 300000.00, '2026-04-10'), (7, 4, 370000.00, '2026-04-10');

-- Reparaciones (Se ajustaron los IdMecanico del 1 al 5)
INSERT INTO dbo.VehiculoReparacion (IdVehiculo, IdMecanico, IdCliente, IdReparacionEstado, FechaIngreso, FechaSalida, DescripcionProblema, CostoEstimado, CostoFinal) VALUES
(2, 1, 2, 3, '2026-03-01', '2026-03-05', N'Ruido en el motor al acelerar, posible problema de bujías.', 1200.00, 1500.00),
(5, 2, 5, 3, '2026-03-10', '2026-03-12', N'Dificultad para cambiar de 2ª a 3ª marcha.', 3500.00, 3200.00),
(7, 3, 3, 2, '2026-04-01', NULL, N'El aire acondicionado no enfría y testigo de batería encendido.', 2500.00, NULL),
(9, 4, 6, 1, '2026-04-20', NULL, N'Vibración al frenar y volante descentrado.', 1800.00, NULL),
(11, 5, 7, 2, '2026-05-01', NULL, N'Golpe en puerta trasera derecha, necesita enderezado y pintura.', 5000.00, NULL),
(1, 1, 1, 3, '2026-02-15', '2026-02-16', N'Cambio de aceite y filtro, revisión general.', 800.00, 800.00);

-- Diagnósticos (Estos estaban bien, se quedan igual)
INSERT INTO dbo.VehiculoDiagnostico (IdVehiculoReparacion, FechaIngreso, Descripcion) VALUES
(1, '2026-03-01', N'Se detectan bujías desgastadas y bobina de encendido con falla intermitente.'),
(2, '2026-03-10', N'Sincronizadores de 2ª y 3ª desgastados. Se recomienda cambio de kit de sincronización.'),
(3, '2026-04-01', N'Compresor del A/C sin carga de refrigerante; alternador con diodo rectificador dañado.'),
(6, '2026-02-15', N'Cambio de aceite programado. Filtro de aire limpio. Niveles correctos.');

-- Sesiones (Se ajustó el IdUsuario a 5, que corresponde a César)
INSERT INTO dbo.UsuarioSesion (IdUsuario, RefreshTokenHash, JwtId, FechaExpiracion, IpCreacion, UserAgent) VALUES
(5, 'ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789', NEWID(), '2026-05-27 08:00:00', '192.168.1.100', N'Mozilla/5.0 Windows'),
(5, '1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF', NEWID(), '2026-05-28 09:30:00', '192.168.1.101', N'PostmanRuntime/7.32.0');
GO

-- Insertar imágenes principales para los 12 vehículos de prueba
INSERT INTO dbo.VehiculoImagen (IdVehiculo, RutaImagen, EsPrincipal) VALUES
(1, '/uploads/vehiculos/nissan-versa.jpg', 1),
(2, '/uploads/vehiculos/nissan-sentra.jpg', 1),
(3, '/uploads/vehiculos/nissan-xtrail.jpg', 1),
(4, '/uploads/vehiculos/toyota-corolla.jpg', 1),
(5, '/uploads/vehiculos/toyota-rav4.jpg', 1),
(6, '/uploads/vehiculos/toyota-hilux.jpg', 1),
(7, '/uploads/vehiculos/honda-civic.jpg', 1),
(8, '/uploads/vehiculos/honda-crv.jpg', 1),
(9, '/uploads/vehiculos/dodge-attitude.jpg', 1),
(10, '/uploads/vehiculos/dodge-journey.jpg', 1),
(11, '/uploads/vehiculos/ford-escape.jpg', 1),
(12, '/uploads/vehiculos/ford-explorer.jpg', 1);
GO
