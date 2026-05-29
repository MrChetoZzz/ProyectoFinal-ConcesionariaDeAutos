db = db.getSiblingDB('ConcesionariaNoSql');

db.createCollection('bitacora_descarga_reportes');
db.bitacora_descarga_reportes.createIndex({ fechaHora: -1 });
db.bitacora_descarga_reportes.createIndex({ modulo: 1, 'detalles.nombreReporte': 1 });

db.createCollection('bitacora_logs');
db.bitacora_logs.createIndex({ fechaHora: -1 });
db.bitacora_logs.createIndex({ nivel: 1 });
