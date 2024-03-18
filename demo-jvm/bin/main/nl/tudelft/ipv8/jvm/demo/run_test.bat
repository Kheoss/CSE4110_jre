@echo off
setlocal

:: Check if a command-line argument is provided
if "%~1"=="" (
    echo Usage: %0 [number_of_instances]
    exit /b 1
)

:: Set the number of instances to the first command-line argument
set /a "num_instances=%~1"

:: Loop to start the command multiple times
for /L %%i in (1,1,%num_instances%) do (
    start /B cmd /c gradlew :demo-jvm:run
)

endlocal
