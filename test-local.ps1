# Script para testar localmente antes do commit
Write-Host "Testing production service locally..." -ForegroundColor Green

# Limpar e compilar
Write-Host "1. Cleaning and compiling..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

# Executar testes
Write-Host "2. Running tests..." -ForegroundColor Yellow
mvn test

if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed!" -ForegroundColor Red
    exit 1
}

# Gerar relatório de cobertura
Write-Host "3. Generating coverage report..." -ForegroundColor Yellow
mvn jacoco:report

# Verificar se os arquivos de relatório foram gerados
$testReports = Get-ChildItem -Path "target/surefire-reports" -Filter "*.xml" -ErrorAction SilentlyContinue
$coverageReport = Test-Path "target/site/jacoco/jacoco.xml"

Write-Host "4. Verification:" -ForegroundColor Yellow
Write-Host "   Test reports: $($testReports.Count) files found" -ForegroundColor Cyan
Write-Host "   Coverage report: $(if($coverageReport) {'✓ Found'} else {'✗ Not found'})" -ForegroundColor Cyan

if ($testReports.Count -gt 0 -and $coverageReport) {
    Write-Host "✅ All checks passed! Ready for commit." -ForegroundColor Green
} else {
    Write-Host "❌ Some checks failed. Please review the output above." -ForegroundColor Red
    exit 1
}

Write-Host "You can now commit and push your changes!" -ForegroundColor Green