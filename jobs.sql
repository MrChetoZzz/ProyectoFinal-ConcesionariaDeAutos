USE msdb;
GO

-- Job 1: Respaldo FULL diario (a las 2:00 AM)
EXEC dbo.sp_add_job @job_name=N'Backup Full ConcesionariaTec';
EXEC sp_add_jobstep @job_name=N'Backup Full ConcesionariaTec',
    @step_name=N'Full Backup',
    @command=N'BACKUP DATABASE [ConcesionariaTec] 
        TO DISK = N''/var/opt/mssql/backups/ConcesionariaTec_Full.bak'' 
        WITH INIT, STATS = 10;',
    @database_name=N'ConcesionariaTec',
    @subsystem=N'TSQL';
EXEC sp_add_schedule @schedule_name=N'Daily 2AM',
    @freq_type=4, @freq_interval=1, @active_start_time=20000;
EXEC sp_attach_schedule @job_name=N'Backup Full ConcesionariaTec', @schedule_name=N'Daily 2AM';
EXEC sp_add_jobserver @job_name=N'Backup Full ConcesionariaTec';
GO

-- Job 2: Respaldo de LOG cada 2 horas
EXEC dbo.sp_add_job @job_name = N'Backup Log ConcesionariaTec', @enabled = 1;
EXEC dbo.sp_add_jobstep @job_name = N'Backup Log ConcesionariaTec',
    @step_name = N'Backup Log Step',
    @subsystem = N'TSQL',
    @command = N'BACKUP LOG [ConcesionariaTec] TO DISK = N''/var/opt/mssql/backups/ConcesionariaTec_Log_'' + FORMAT(GETDATE(), ''yyyyMMdd_HHmm'') + ''.trn'' WITH COMPRESSION, STATS = 10;',
    @database_name = N'ConcesionariaTec';
EXEC dbo.sp_add_schedule @schedule_name = N'LogBackup Cada 2 horas',
    @freq_type = 4, @freq_interval = 1, @freq_subday_type = 8, @freq_subday_interval = 2, @active_start_time = 0;
EXEC dbo.sp_attach_schedule @job_name = N'Backup Log ConcesionariaTec', @schedule_name = N'LogBackup Cada 2 horas';
EXEC dbo.sp_add_jobserver @job_name = N'Backup Log ConcesionariaTec';
GO

-- Job 3: Mantenimiento de Índices
EXEC dbo.sp_add_job @job_name = N'Mantenimiento Indices ConcesionariaTec', @enabled = 1;
EXEC dbo.sp_add_jobstep @job_name = N'Mantenimiento Indices ConcesionariaTec',
    @step_name = N'Index Maintenance',
    @subsystem = N'TSQL',
    @command = N'
        DECLARE @sql NVARCHAR(MAX) = '''';
        SELECT @sql += ''ALTER INDEX '' + QUOTENAME(i.name) + '' ON '' + QUOTENAME(SCHEMA_NAME(o.schema_id)) + ''.'' + QUOTENAME(o.name) + '' REORGANIZE; ''
        FROM sys.dm_db_index_physical_stats(DB_ID(N''ConcesionariaTec''), NULL, NULL, NULL, ''LIMITED'') ps
        JOIN sys.indexes i ON ps.object_id = i.object_id AND ps.index_id = i.index_id
        JOIN sys.objects o ON i.object_id = o.object_id
        WHERE ps.avg_fragmentation_in_percent > 5 AND ps.avg_fragmentation_in_percent <= 30 AND i.index_id > 0 AND o.is_ms_shipped = 0;
        SELECT @sql += ''ALTER INDEX '' + QUOTENAME(i.name) + '' ON '' + QUOTENAME(SCHEMA_NAME(o.schema_id)) + ''.'' + QUOTENAME(o.name) + '' REBUILD; ''
        FROM sys.dm_db_index_physical_stats(DB_ID(N''ConcesionariaTec''), NULL, NULL, NULL, ''LIMITED'') ps
        JOIN sys.indexes i ON ps.object_id = i.object_id AND ps.index_id = i.index_id
        JOIN sys.objects o ON i.object_id = o.object_id
        WHERE ps.avg_fragmentation_in_percent > 30 AND i.index_id > 0 AND o.is_ms_shipped = 0;
        IF @sql <> '''' EXEC sp_executesql @sql;
    ',
    @database_name = N'ConcesionariaTec';
EXEC dbo.sp_add_schedule @schedule_name = N'Sunday 3AM', @freq_type = 8, @freq_interval = 1, @freq_recurrence_factor = 1, @active_start_time = 30000;
EXEC dbo.sp_attach_schedule @job_name = N'Mantenimiento Indices ConcesionariaTec', @schedule_name = N'Sunday 3AM';
EXEC dbo.sp_add_jobserver @job_name = N'Mantenimiento Indices ConcesionariaTec';
GO

-- Job 4: Replicación de Datos (Snapshot)
EXEC dbo.sp_add_job @job_name = N'Replicacion Semanal Concesionaria', @enabled = 1;
EXEC dbo.sp_add_jobstep @job_name = N'Replicacion Semanal Concesionaria',
    @step_name = N'Sincronizar Replica',
    @subsystem = N'TSQL',
    @command = N'
        BACKUP DATABASE [ConcesionariaTec] TO DISK = N''/var/opt/mssql/backups/ReplicaSnapshot.bak'' WITH INIT, COPY_ONLY, COMPRESSION;
        RESTORE DATABASE [ConcesionariaTec_Replica] FROM DISK = N''/var/opt/mssql/backups/ReplicaSnapshot.bak''
        WITH MOVE N''ConcesionariaTec'' TO N''/var/opt/mssql/data/ConcesionariaTec_Replica.mdf'',
             MOVE N''ConcesionariaTec_log'' TO N''/var/opt/mssql/data/ConcesionariaTec_Replica_log.ldf'', REPLACE, RECOVERY;
    ',
    @database_name = N'master';
EXEC dbo.sp_add_schedule @schedule_name = N'Viernes Madrugada', @freq_type = 8, @freq_interval = 32, @freq_recurrence_factor = 1, @active_start_time = 20000;
EXEC dbo.sp_attach_schedule @job_name = N'Replicacion Semanal Concesionaria', @schedule_name = N'Viernes Madrugada';
EXEC dbo.sp_add_jobserver @job_name = N'Replicacion Semanal Concesionaria';
GO