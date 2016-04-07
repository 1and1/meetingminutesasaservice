@echo off
REM
REM setenv.sh: Sets the Mibble environment variables
REM

REM Set MMAAS_HOME variable
if not "%MMAAS_HOME%" == "" goto doneHome
set MMAAS_HOME=.
if exist "%MMAAS_HOME%\lib\*.jar" goto doneHome
set MMAAS_HOME=.
:doneHome

REM Set CLASSPATH variable
if exist "%MMAAS_HOME%\lib\*.jar" goto doneLib
echo Error: %MMAAS_HOME%\lib does not exist
pause
goto end
:doneLib

REM Display variables
echo Using environment variables:
echo   MMAAS_HOME = %MMAAS_HOME%
echo.

java -Dlog4j.configuration=file:%MMAAS_HOME%/config/log4j.properties -jar %MMAAS_HOME%\lib\MeetingMinutesAsAService-1.2-SNAPSHOT.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
