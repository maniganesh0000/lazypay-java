# Pay-Later Service API End-to-End Test Script
Write-Host "Starting Pay-Later Service API End-to-End Testing..." -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green

# Wait for application to start
Write-Host "Waiting for application to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Test 1: Health Check
Write-Host "`n1. Testing Health Endpoint..." -ForegroundColor Cyan
try {
    $healthResponse = Invoke-WebRequest -Uri "http://localhost:8080/test/health" -UseBasicParsing
    Write-Host "✅ Health Check: $($healthResponse.StatusCode) - $($healthResponse.Content)" -ForegroundColor Green
} catch {
    Write-Host "❌ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Create Test User
Write-Host "`n2. Testing User Creation..." -ForegroundColor Cyan
$userPayload = @{
    name = "Test User API"
    email = "test.api@example.com"
    creditLimit = "1500.00"
} | ConvertTo-Json

try {
    $userResponse = Invoke-WebRequest -Uri "http://localhost:8080/users" -Method POST -Body $userPayload -ContentType "application/json" -UseBasicParsing
    Write-Host "✅ User Creation: $($userResponse.StatusCode)" -ForegroundColor Green
    $userData = $userResponse.Content | ConvertFrom-Json
    $userApiKey = $userData.data.apiKey
    Write-Host "   User API Key: $userApiKey" -ForegroundColor Gray
} catch {
    Write-Host "❌ User Creation Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Create Test Merchant
Write-Host "`n3. Testing Merchant Creation..." -ForegroundColor Cyan
$merchantPayload = @{
    name = "Test Store API"
    feePercentage = "2.5"
} | ConvertTo-Json

try {
    $merchantResponse = Invoke-WebRequest -Uri "http://localhost:8080/merchants" -Method POST -Body $merchantPayload -ContentType "application/json" -UseBasicParsing
    Write-Host "✅ Merchant Creation: $($merchantResponse.StatusCode)" -ForegroundColor Green
    $merchantData = $merchantResponse.Content | ConvertFrom-Json
    $merchantApiKey = $merchantData.data.apiKey
    Write-Host "   Merchant API Key: $merchantApiKey" -ForegroundColor Gray
} catch {
    Write-Host "❌ Merchant Creation Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Create Transaction
Write-Host "`n4. Testing Transaction Creation..." -ForegroundColor Cyan
$transactionPayload = @{
    userName = "Test User API"
    merchantName = "Test Store API"
    amount = "500.00"
} | ConvertTo-Json

try {
    $transactionResponse = Invoke-WebRequest -Uri "http://localhost:8080/transactions" -Method POST -Body $transactionPayload -ContentType "application/json" -Headers @{"X-API-KEY" = $userApiKey} -UseBasicParsing
    Write-Host "✅ Transaction Creation: $($transactionResponse.StatusCode)" -ForegroundColor Green
    $transactionData = $transactionResponse.Content | ConvertFrom-Json
    Write-Host "   Transaction Amount: $($transactionData.data.amount)" -ForegroundColor Gray
    Write-Host "   Transaction Status: $($transactionData.data.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Transaction Creation Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Check User Dues
Write-Host "`n5. Testing User Dues Report..." -ForegroundColor Cyan
try {
    $duesResponse = Invoke-WebRequest -Uri "http://localhost:8080/reports/dues/Test User API" -Headers @{"X-API-KEY" = $userApiKey} -UseBasicParsing
    Write-Host "✅ Dues Report: $($duesResponse.StatusCode)" -ForegroundColor Green
    $duesData = $duesResponse.Content | ConvertFrom-Json
    Write-Host "   Current Dues: $($duesData.data)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Dues Report Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Check Merchant Fee Collection
Write-Host "`n6. Testing Merchant Fee Report..." -ForegroundColor Cyan
try {
    $feeResponse = Invoke-WebRequest -Uri "http://localhost:8080/reports/fee/Test Store API" -Headers @{"X-API-KEY" = $merchantApiKey} -UseBasicParsing
    Write-Host "✅ Fee Report: $($feeResponse.StatusCode)" -ForegroundColor Green
    $feeData = $feeResponse.Content | ConvertFrom-Json
    Write-Host "   Total Fees Collected: $($feeData.data)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Fee Report Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Create Payback
Write-Host "`n7. Testing Payback Creation..." -ForegroundColor Cyan
$paybackPayload = @{
    userName = "Test User API"
    amount = "200.00"
} | ConvertTo-Json

try {
    $paybackResponse = Invoke-WebRequest -Uri "http://localhost:8080/paybacks" -Method POST -Body $paybackPayload -ContentType "application/json" -Headers @{"X-API-KEY" = $userApiKey} -UseBasicParsing
    Write-Host "✅ Payback Creation: $($paybackResponse.StatusCode)" -ForegroundColor Green
    $paybackData = $paybackResponse.Content | ConvertFrom-Json
    Write-Host "   Payback Amount: $($paybackData.data.amount)" -ForegroundColor Gray
    Write-Host "   Remaining Dues: $($paybackData.data.remainingDues)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Payback Creation Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Final Dues Check
Write-Host "`n8. Testing Final Dues Report..." -ForegroundColor Cyan
try {
    $finalDuesResponse = Invoke-WebRequest -Uri "http://localhost:8080/reports/dues/Test User API" -Headers @{"X-API-KEY" = $userApiKey} -UseBasicParsing
    Write-Host "✅ Final Dues Report: $($finalDuesResponse.StatusCode)" -ForegroundColor Green
    $finalDuesData = $finalDuesResponse.Content | ConvertFrom-Json
    Write-Host "   Final Dues: $($finalDuesData.data)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Final Dues Report Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 9: Total Dues Report
Write-Host "`n9. Testing Total Dues Report..." -ForegroundColor Cyan
try {
    $totalDuesResponse = Invoke-WebRequest -Uri "http://localhost:8080/reports/total-dues" -Headers @{"X-API-KEY" = $userApiKey} -UseBasicParsing
    Write-Host "✅ Total Dues Report: $($totalDuesResponse.StatusCode)" -ForegroundColor Green
    $totalDuesData = $totalDuesResponse.Content | ConvertFrom-Json
    Write-Host "   Total Dues: $($totalDuesData.data.totalDues)" -ForegroundColor Gray
    Write-Host "   User Count: $($totalDuesData.data.userCount)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Total Dues Report Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 10: Test Authentication (Missing API Key)
Write-Host "`n10. Testing Authentication (Missing API Key)..." -ForegroundColor Cyan
try {
    $authResponse = Invoke-WebRequest -Uri "http://localhost:8080/users/test.api@example.com" -UseBasicParsing
    Write-Host "❌ Authentication Test Failed: Should have been rejected" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Authentication Test: Correctly rejected (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "❌ Authentication Test: Unexpected error - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n==================================================" -ForegroundColor Green
Write-Host "End-to-End API Testing Complete!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
