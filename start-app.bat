@echo off
echo ==========================================
echo   Payroll Balance Reconciliation System
echo ==========================================
echo.

echo Starting Backend (Spring Boot)...
start "Backend - Spring Boot" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo Waiting for backend to initialize...
timeout /t 10 /nobreak >nul

echo Starting Frontend (React + Vite)...
start "Frontend - React" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo ==========================================
echo   Both services are starting!
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:5173
echo ==========================================
echo.
pause
