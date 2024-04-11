@echo off
setlocal EnableDelayedExpansion

:: Check if the correct number of arguments is passed
if "%~1"=="" (
    echo Usage: %0 [number of instances]
    exit /b
)

:: Start multiple instances of the Java application
for /L %%i in (1,1,%1) do (
    start /B java -jar KubernetsTest/demo-jvm-all.jar
    :: Introduce a small delay to ensure the process has started
    ping 127.0.0.1 -n 2 > nul
)

