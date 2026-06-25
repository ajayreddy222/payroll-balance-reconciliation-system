@echo off
echo ==========================================
echo   Stopping Payroll Reconciliation System
echo ==========================================
echo.

echo Stopping Backend (port 8080)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do taskkill /F /PID %%a 2>nul

echo Stopping Frontend (port 5173)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do taskkill /F /PID %%a 2>nul

echo.
echo All services stopped.
pause
