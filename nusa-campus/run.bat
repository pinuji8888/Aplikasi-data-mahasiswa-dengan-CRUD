@echo off
cd /d "%~dp0"
if not exist target\nusa-campus.jar (
  echo Build terlebih dahulu: mvn clean package dengan JDK 11 atau lebih baru.
  pause
  exit /b 1
)
if defined JAVA_HOME (
  "%JAVA_HOME%\bin\java.exe" -jar target\nusa-campus.jar %*
) else (
  java -jar target\nusa-campus.jar %*
)
if errorlevel 1 pause
