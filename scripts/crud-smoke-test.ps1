param(
    [string]$BaseUrl = "http://localhost:8081/api"
)

$ErrorActionPreference = "Stop"
$RunId = Get-Date -Format "yyyyMMddHHmmss"
$Today = Get-Date -Format "yyyy-MM-dd"

function Write-Step([string]$Message) {
    Write-Host ""
    Write-Host "== $Message ==" -ForegroundColor Cyan
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [int[]]$ExpectedStatus = @(200)
    )

    $uri = "$BaseUrl$Path"
    $json = $null
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 8
    }

    try {
        $response = Invoke-WebRequest -Method $Method -Uri $uri -ContentType "application/json" -Body $json
        if ($ExpectedStatus -notcontains [int]$response.StatusCode) {
            throw "Expected $($ExpectedStatus -join ',') but got $($response.StatusCode) from $Method $Path"
        }
        if ([string]::IsNullOrWhiteSpace($response.Content)) {
            return $null
        }
        return $response.Content | ConvertFrom-Json
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        if ($ExpectedStatus -contains [int]$status) {
            return $null
        }
        throw
    }
}

function Assert-True([bool]$Condition, [string]$Message) {
    if (-not $Condition) {
        throw $Message
    }
    Write-Host "OK - $Message" -ForegroundColor Green
}

Write-Step "Health check"
Invoke-Api GET "/test/db-check" | Out-Null
Assert-True $true "Backend connected to SQL Server"

Write-Step "SQL injection probes"
Invoke-Api GET "/vehiculos?sort=Marca-Ascendente%3B%20DROP%20TABLE%20Vehiculo%3B--" | Out-Null
Invoke-Api GET "/vehiculos/modelos?marca=%27%20OR%201%3D1%20--" | Out-Null
Assert-True $true "Malicious query params did not break endpoints"

Write-Step "Clientes CRUD"
$clientName = "SmokeCliente$RunId"
Invoke-Api POST "/clientes" @{
    nombre = $clientName
    apellidoPaterno = "QA"
    telefono = "8671234567"
    colonia = "Centro"
    calle = "Prueba"
    numExt = "100"
} | Out-Null
$clientes = Invoke-Api GET "/clientes"
$cliente = $clientes | Where-Object { $_.nombreCompleto -like "$clientName*" } | Select-Object -First 1
Assert-True ($null -ne $cliente) "Cliente creado y listado"
Invoke-Api GET "/clientes/$($cliente.id)" | Out-Null
Invoke-Api PUT "/clientes/$($cliente.id)" @{
    nombre = "$clientName-Edit"
    apellidoPaterno = "QA"
    telefono = "8671234567"
    colonia = "Centro"
    calle = "Prueba Edit"
    numExt = "101"
} | Out-Null
Assert-True $true "Cliente obtenido y actualizado"

Write-Step "Vehiculos CRUD"
$deleteVehicle = Invoke-Api POST "/vehiculos" @{
    marca = "Toyota"
    modelo = "SmokeDelete$RunId"
    anioModelo = 2024
    costo = 123456
    condicion = "Nuevo"
    placas = "SMK$($RunId.Substring($RunId.Length - 4))"
    numeroSerie = "SMOKEDEL$($RunId.Substring($RunId.Length - 8))"
}
Assert-True ($deleteVehicle.id -gt 0) "Vehiculo creado"
Invoke-Api PUT "/vehiculos/$($deleteVehicle.id)" @{
    marca = "Toyota"
    modelo = "SmokeDeleteEdit$RunId"
    anioModelo = 2024
    costo = 130000
    condicion = "Usado"
    placas = "SMK$($RunId.Substring($RunId.Length - 4))"
    numeroSerie = "SMOKEDEL$($RunId.Substring($RunId.Length - 8))"
} | Out-Null
Invoke-Api DELETE "/vehiculos/$($deleteVehicle.id)" | Out-Null
Assert-True $true "Vehiculo actualizado y eliminado"

Write-Step "Ventas CRUD posible y taller"
$saleVehicle = Invoke-Api POST "/vehiculos" @{
    marca = "Ford"
    modelo = "SmokeSale$RunId"
    anioModelo = 2024
    costo = 234567
    condicion = "Nuevo"
    placas = "SL$($RunId.Substring($RunId.Length - 5))"
    numeroSerie = "SMOKESALE$($RunId.Substring($RunId.Length - 8))"
}
$tiposPago = Invoke-Api GET "/ventas/tipos-pago"
Assert-True (($tiposPago | Measure-Object).Count -gt 0) "Tipos de pago disponibles"
$venta = Invoke-Api POST "/ventas" @{
    idCliente = $cliente.id
    idVehiculo = $saleVehicle.id
    idTipoPago = $tiposPago[0].id
    fecha = $Today
    monto = $saleVehicle.costo
}
Assert-True ($venta.id -gt 0) "Venta creada"
Invoke-Api GET "/ventas/$($venta.id)" | Out-Null

$reparacion = Invoke-Api POST "/mecanica/reparaciones" @{
    idCliente = $cliente.id
    idVehiculo = $saleVehicle.id
    fechaIngreso = $Today
    descripcionProblema = "Revision smoke test $RunId"
    diagnosticoInicial = "Sin fallas criticas"
    costoEstimado = 1500
}
Assert-True ($reparacion.id -gt 0) "Reparacion creada para venta completada"
Invoke-Api PUT "/mecanica/reparaciones/$($reparacion.id)" @{
    descripcionProblema = "Revision smoke test editada $RunId"
    costoEstimado = 1800
} | Out-Null
Invoke-Api PATCH "/mecanica/reparaciones/$($reparacion.id)/completar" @{ costoFinal = 1800 } | Out-Null
Assert-True $true "Reparacion actualizada y completada"

Invoke-Api DELETE "/ventas/$($venta.id)" | Out-Null
Invoke-Api DELETE "/clientes/$($cliente.id)" | Out-Null
Assert-True $true "Venta cancelada y cliente desactivado"

Write-Step "Smoke test completado"
Write-Host "BaseUrl: $BaseUrl" -ForegroundColor Gray
Write-Host "RunId: $RunId" -ForegroundColor Gray
