# Script PowerShell para testar no Windows
$BASE_URL = "http://localhost:8083/api/productions"

Write-Host "=== TESTE COMPLETO DO PRODUCTION SERVICE ===" -ForegroundColor Green

Write-Host "1. Verificando se o serviço está rodando..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/stats/count-by-status" -Method Get
    Write-Host "✅ Serviço está rodando!" -ForegroundColor Green
} catch {
    Write-Host "❌ Serviço não está respondendo. Verifique se está rodando." -ForegroundColor Red
    exit 1
}

Write-Host "`n2. Verificando estatísticas iniciais..." -ForegroundColor Yellow
$stats = Invoke-RestMethod -Uri "$BASE_URL/stats/count-by-status" -Method Get
$stats | ConvertTo-Json

Write-Host "`n3. Listando produções existentes..." -ForegroundColor Yellow
$productions = Invoke-RestMethod -Uri "$BASE_URL" -Method Get
Write-Host "Total de produções: $($productions.content.Length)"

Write-Host "`n4. Processando mensagens da fila..." -ForegroundColor Yellow
$processResult = Invoke-RestMethod -Uri "$BASE_URL/process-queue?maxMessages=5" -Method Post
Write-Host "Resultado: $processResult"

Write-Host "`n5. Verificando estatísticas após processamento..." -ForegroundColor Yellow
$statsAfter = Invoke-RestMethod -Uri "$BASE_URL/stats/count-by-status" -Method Get
$statsAfter | ConvertTo-Json

Write-Host "`n6. Testando mudança de status..." -ForegroundColor Yellow
$productionsList = Invoke-RestMethod -Uri "$BASE_URL" -Method Get
if ($productionsList.content.Length -gt 0) {
    $productionId = $productionsList.content[0].id
    Write-Host "Alterando status da produção: $productionId"
    
    $body = @{
        status = "IN_PROGRESS"
    } | ConvertTo-Json
    
    try {
        $statusResult = Invoke-RestMethod -Uri "$BASE_URL/$productionId/status-change" -Method Post -Body $body -ContentType "application/json"
        Write-Host "Resultado: $statusResult"
    } catch {
        Write-Host "Erro ao alterar status: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "Nenhuma produção encontrada para testar mudança de status"
}

Write-Host "`n7. Estatísticas finais..." -ForegroundColor Yellow
$finalStats = Invoke-RestMethod -Uri "$BASE_URL/stats/count-by-status" -Method Get
$finalStats | ConvertTo-Json

Write-Host "`n=== TESTE COMPLETO FINALIZADO ===" -ForegroundColor Green