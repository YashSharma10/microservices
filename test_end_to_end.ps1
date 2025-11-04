# End-to-End Test Script for Carbon Credit Microservices
# This script tests the complete flow of the microservices application

$baseUrl = "http://localhost:8080"
$eurekaUrl = "http://localhost:8761"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Carbon Credit Microservices E2E Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Function to make HTTP requests
function Invoke-ApiCall {
    param(
        [string]$Method,
        [string]$Uri,
        [string]$Body = $null,
        [hashtable]$Headers = @{},
        [string]$Description
    )
    
    Write-Host "`n--- $Description ---" -ForegroundColor Yellow
    Write-Host "Request: $Method $Uri" -ForegroundColor Gray
    
    try {
        $params = @{
            Method = $Method
            Uri = $Uri
            Headers = $Headers + @{"Content-Type" = "application/json"}
            ErrorAction = "Stop"
        }
        
        if ($Body) {
            $params.Body = $Body
            Write-Host "Body: $Body" -ForegroundColor Gray
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Green
        return $response
    }
    catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }
        return $null
    }
}

# Test 1: Check Eureka Dashboard
Write-Host "`n[1] Checking Eureka Service Discovery..." -ForegroundColor Cyan
try {
    $eureka = Invoke-WebRequest -Uri $eurekaUrl -UseBasicParsing
    if ($eureka.StatusCode -eq 200) {
        Write-Host "✓ Eureka is running on $eurekaUrl" -ForegroundColor Green
    }
}
catch {
    Write-Host "✗ Eureka is not accessible" -ForegroundColor Red
}

# Test 2: User Registration
Write-Host "`n[2] Testing Authentication Service - User Registration..." -ForegroundColor Cyan
$signupBody = @{
    username = "testuser1"
    password = "password123"
} | ConvertTo-Json

$signup = Invoke-ApiCall -Method POST -Uri "$baseUrl/auth/signup" -Body $signupBody -Description "Register new user"

# Test 3: User Login
Write-Host "`n[3] Testing Authentication Service - User Login..." -ForegroundColor Cyan
$loginBody = @{
    username = "testuser1"
    password = "password123"
} | ConvertTo-Json

$login = Invoke-ApiCall -Method POST -Uri "$baseUrl/auth/login" -Body $loginBody -Description "Login user"

$token = $null
if ($login -and $login.token) {
    $token = $login.token
    Write-Host "✓ JWT Token received: $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Green
}

# Test 4: Create User Profile
Write-Host "`n[4] Testing User Service - Create User Profile..." -ForegroundColor Cyan
$userBody = @{
    name = "Test User One"
    credits = 1000.0
    balance = 5000.0
} | ConvertTo-Json

$headers = @{}
if ($token) {
    $headers["Authorization"] = "Bearer $token"
}

$user = Invoke-ApiCall -Method POST -Uri "$baseUrl/users" -Body $userBody -Headers $headers -Description "Create user profile"

$userId = $null
if ($user -and $user.id) {
    $userId = $user.id
    Write-Host "✓ User created with ID: $userId" -ForegroundColor Green
}

# Test 5: Create another user for trading
Write-Host "`n[5] Creating second user for trading..." -ForegroundColor Cyan
$user2Body = @{
    name = "Test User Two"
    credits = 500.0
    balance = 3000.0
} | ConvertTo-Json

$user2 = Invoke-ApiCall -Method POST -Uri "$baseUrl/users" -Body $user2Body -Headers $headers -Description "Create second user"

$userId2 = $null
if ($user2 -and $user2.id) {
    $userId2 = $user2.id
    Write-Host "✓ Second user created with ID: $userId2" -ForegroundColor Green
}

# Test 6: Get User Details
if ($userId) {
    Write-Host "`n[6] Testing User Service - Get User Details..." -ForegroundColor Cyan
    $userDetails = Invoke-ApiCall -Method GET -Uri "$baseUrl/users/$userId" -Headers $headers -Description "Get user details"
}

# Test 7: Create Carbon Credit
Write-Host "`n[7] Testing Carbon Service - Create Carbon Credit..." -ForegroundColor Cyan
if ($userId) {
    $carbonBody = @{
        name = "Solar Energy Credits"
        supply = 100.0
        ownerId = $userId
        price = 50.0
    } | ConvertTo-Json

    $carbon = Invoke-ApiCall -Method POST -Uri "$baseUrl/carbon" -Body $carbonBody -Headers $headers -Description "Create carbon credit"

    $carbonId = $null
    if ($carbon -and $carbon.id) {
        $carbonId = $carbon.id
        Write-Host "✓ Carbon credit created with ID: $carbonId" -ForegroundColor Green
    }
}

# Test 8: Get All Carbon Credits
Write-Host "`n[8] Testing Carbon Service - Get All Carbon Credits..." -ForegroundColor Cyan
$allCarbons = Invoke-ApiCall -Method GET -Uri "$baseUrl/carbon" -Headers $headers -Description "Get all carbon credits"

# Test 9: Update Carbon Credit
if ($carbonId) {
    Write-Host "`n[9] Testing Carbon Service - Update Carbon Credit..." -ForegroundColor Cyan
    $updateBody = @{
        name = "Solar Energy Credits - Updated"
        supply = 150.0
        ownerId = $userId
        price = 55.0
    } | ConvertTo-Json

    $updated = Invoke-ApiCall -Method PUT -Uri "$baseUrl/carbon/$carbonId" -Body $updateBody -Headers $headers -Description "Update carbon credit"
}

# Test 10: Add Credits to User
if ($userId) {
    Write-Host "`n[10] Testing User Service - Add Credits..." -ForegroundColor Cyan
    $addCreditsBody = @{
        amount = 200.0
    } | ConvertTo-Json

    $creditsAdded = Invoke-ApiCall -Method POST -Uri "$baseUrl/users/$userId/addCredits" -Body $addCreditsBody -Headers $headers -Description "Add credits to user"
}

# Test 11: Create a Trade
if ($userId -and $userId2) {
    Write-Host "`n[11] Testing Trade Service - Create Trade..." -ForegroundColor Cyan
    $tradeBody = @{
        fromUserId = $userId
        toUserId = $userId2
        amount = 50.0
    } | ConvertTo-Json

    $trade = Invoke-ApiCall -Method POST -Uri "$baseUrl/trades" -Body $tradeBody -Headers $headers -Description "Create trade transaction"

    $tradeId = $null
    if ($trade -and $trade.id) {
        $tradeId = $trade.id
        Write-Host "✓ Trade created with ID: $tradeId" -ForegroundColor Green
    }
}

# Test 12: Get All Trades
Write-Host "`n[12] Testing Trade Service - Get All Trades..." -ForegroundColor Cyan
$allTrades = Invoke-ApiCall -Method GET -Uri "$baseUrl/trades" -Headers $headers -Description "Get all trades"

# Test 13: Get Trade by ID
if ($tradeId) {
    Write-Host "`n[13] Testing Trade Service - Get Trade by ID..." -ForegroundColor Cyan
    $tradeDetails = Invoke-ApiCall -Method GET -Uri "$baseUrl/trades/$tradeId" -Headers $headers -Description "Get trade details"
}

# Test 14: Carbon Trade (transfer carbon credits)
if ($userId -and $userId2 -and $carbonId) {
    Write-Host "`n[14] Testing Trade Service - Carbon Credit Transfer..." -ForegroundColor Cyan
    $carbonTradeBody = @{
        fromUserId = $userId
        toUserId = $userId2
        carbonId = $carbonId
        quantity = 25.0
    } | ConvertTo-Json

    $carbonTrade = Invoke-ApiCall -Method POST -Uri "$baseUrl/trades/carbon" -Body $carbonTradeBody -Headers $headers -Description "Transfer carbon credits"
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl" -ForegroundColor White
Write-Host "Eureka URL: $eurekaUrl" -ForegroundColor White
if ($token) {
    Write-Host "✓ Authentication: Success" -ForegroundColor Green
} else {
    Write-Host "✗ Authentication: Failed" -ForegroundColor Red
}
if ($userId) {
    Write-Host "✓ User Service: Working (User ID: $userId)" -ForegroundColor Green
} else {
    Write-Host "✗ User Service: Failed" -ForegroundColor Red
}
if ($carbonId) {
    Write-Host "✓ Carbon Service: Working (Carbon ID: $carbonId)" -ForegroundColor Green
} else {
    Write-Host "✗ Carbon Service: Failed" -ForegroundColor Red
}
if ($tradeId) {
    Write-Host "✓ Trade Service: Working (Trade ID: $tradeId)" -ForegroundColor Green
} else {
    Write-Host "✗ Trade Service: Failed" -ForegroundColor Red
}

Write-Host "`n========================================`n" -ForegroundColor Cyan
Write-Host "Test completed!" -ForegroundColor Green
Write-Host "You can access Eureka Dashboard at: $eurekaUrl" -ForegroundColor Yellow
Write-Host "All services are accessible through API Gateway at: $baseUrl" -ForegroundColor Yellow
