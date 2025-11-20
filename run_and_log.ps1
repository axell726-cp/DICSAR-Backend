# Script para ejecutar Maven y capturar logs
cd "c:\Users\matia\Documents\DICSAR-Backend"

# Ejecutar Maven y capturar salida completa
$output = mvn spring-boot:run 2>&1

# Guardar en archivo
$output | Out-File -Encoding UTF8 "spring_boot_output.log"

# Mostrar las últimas líneas
Write-Host "=== ÚLTIMAS 50 LÍNEAS ===" -ForegroundColor Yellow
$output | Select-Object -Last 50 | ForEach-Object { Write-Host $_ }

# Mostrar líneas con ERROR o Exception
Write-Host "`n=== ERRORES ENCONTRADOS ===" -ForegroundColor Red
$output | Where-Object { $_ -match 'ERROR|Exception|error' } | ForEach-Object { Write-Host $_ }
