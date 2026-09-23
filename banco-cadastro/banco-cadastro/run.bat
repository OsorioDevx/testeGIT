@echo off
REM Compila e executa o projeto (Windows)
cd /d "%~dp0"
if exist out rmdir /s /q out
REM -sourcepath src faz o javac achar e compilar sozinho todas as classes usadas pelo Main
javac -encoding UTF-8 -d out -sourcepath src src\br\com\banco\Main.java
if errorlevel 1 (
    echo.
    echo Erro na compilacao. Verifique se o JDK 17 ou superior esta instalado.
    pause
    exit /b 1
)
java -cp out br.com.banco.Main
