### Bitácora de descargar reportes

~~~JSON
{  "_id": ObjectId("60b9..."),  "fechaHora": ISODate("2024-05-20T14:30:00Z"),  "idUsuario": 15,   "modulo": "FINANZAS",   "ip": "192.168.1.50",  "correo": "cesar8vs@gmail.com",  "detalles": {  "idReporte": "484",      "nombreReporte": "VentasMensuales",    "formato": "PDF",  "filtrosUsados": {    "parFechaInicial": "2026-01-01",    "parFechaFinal": "2026-02-01"  }  }}
~~~

### Bitácora de logs

```
{  "nivel": "ERROR",  "mensaje": "Error al insertar en VehiculoReparacion",  "codigoError": 547,  "stackTrace": "aaaaaaaaaa...",  "contexto": { "url": "/reparacion/nuevo", "navegador": "Chrome" }}
