# PowerShell script to run server and multiple clients
$numClients = 4  # Change this number to run more or fewer clients

Write-Host "Starting server..."
Start-Process powershell -ArgumentList "-NoExit -Command java -cp bin DataOnServer.Server"

# Wait a bit for server to start
Start-Sleep -Seconds 2

Write-Host "Starting $numClients clients..."
for ($i = 1; $i -le $numClients; $i++) {
    Write-Host "Starting client $i..."
    Start-Process powershell -ArgumentList "-NoExit -Command java -cp bin DataOnServer.Client"
    Start-Sleep -Seconds 1
} 