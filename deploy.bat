@echo off
setlocal

set DOCKER_USER=haowu6666
set VERSION=%1
if "%VERSION%"=="" set VERSION=latest

echo =========================================
echo   TaskFlow Docker Image Publisher
echo   User: %DOCKER_USER%
echo   Version: %VERSION%
echo =========================================

echo.
echo [1/5] Docker Hub Login...
docker login
if %errorlevel% neq 0 (echo Login failed! & exit /b 1)

echo.
echo [2/5] Building backend image...
docker build -t %DOCKER_USER%/taskflow-backend:%VERSION% -t %DOCKER_USER%/taskflow-backend:latest ./taskflow-backend
if %errorlevel% neq 0 (echo Backend build failed! & exit /b 1)

echo.
echo [3/5] Building frontend image...
docker build -t %DOCKER_USER%/taskflow-frontend:%VERSION% -t %DOCKER_USER%/taskflow-frontend:latest ./taskflow-frontend
if %errorlevel% neq 0 (echo Frontend build failed! & exit /b 1)

echo.
echo [4/5] Pushing backend image...
docker push %DOCKER_USER%/taskflow-backend:%VERSION%
docker push %DOCKER_USER%/taskflow-backend:latest
if %errorlevel% neq 0 (echo Backend push failed! & exit /b 1)

echo.
echo [5/5] Pushing frontend image...
docker push %DOCKER_USER%/taskflow-frontend:%VERSION%
docker push %DOCKER_USER%/taskflow-frontend:latest
if %errorlevel% neq 0 (echo Frontend push failed! & exit /b 1)

echo.
echo =========================================
echo   Done!
echo.
echo   Images:
echo     %DOCKER_USER%/taskflow-backend:%VERSION%
echo     %DOCKER_USER%/taskflow-frontend:%VERSION%
echo.
echo   Deploy on server:
echo     docker compose -f docker-compose.prod.yml pull
echo     docker compose -f docker-compose.prod.yml up -d
echo =========================================

endlocal
