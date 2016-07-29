ECHO OFF
cls
:MENU
ECHO.
ECHO ...............................................
ECHO CCM Command Line Tool Demo Batch
ECHO ...............................................
ECHO.
ECHO 1 - Example 1
ECHO 2 - Example 2 (Warning: Processor Intensive, Long Example)
ECHO 3 - Example 3
ECHO 4 - Example 4
ECHO 5 - Real Time Example 1
ECHO 6 - Real Time Example 2
ECHO 7 - Real Time Example 3
ECHO 8 - Exit
ECHO.
SET /P M=Select an Option:
IF %M%==1 GOTO EXAMPLE1
IF %M%==2 GOTO EXAMPLE2
IF %M%==3 GOTO EXAMPLE3
IF %M%==4 GOTO EXAMPLE4
IF %M%==5 GOTO REALTIMEEXAMPLE1
IF %M%==6 GOTO REALTIMEEXAMPLE2
IF %M%==7 GOTO REALTIMEEXAMPLE3
IF %M%==8 GOTO EXIT
:EXAMPLE1
java -jar ccmcl.jar -A Examples\Example1\EXAMPLE1.txt -T 2,3,4,5,6 -p -S -H -B
GOTO MENU
:EXAMPLE2
java -jar ccmcl.jar -A Examples\Example2\apache.xml -T 2,3,4,5,6 -p -S -B
GOTO MENU
:EXAMPLE3
java -jar ccmcl.jar -A Examples\Example3\ACTSfile.txt -T 2,3,4,5 -p -S -H -B
GOTO MENU
:EXAMPLE4
java -jar ccmcl.jar -I Examples\Example4\input_test.csv -P -T 2,3,4,5 -p -S -H -B -G -m 100 -o Examples\Example4\missingCombinations.txt -a
GOTO MENU
:REALTIMEEXAMPLE1
java -jar ccmcl.jar -A "Examples\RealTime Example 1\ACTSfile.txt" -T 2,3,4,5,6 -S -B -H -p -R -e "Examples\RealTime Example 1\generate_fast.jar"
GOTO MENU
:REALTIMEEXAMPLE2
java -jar ccmcl.jar -A "Examples\RealTime Example 2\ACTSfile.txt" -S -B -H -p -T 2,3,4,5,6 -R -e "Examples\RealTime Example 2\generate_fastest.jar" 
GOTO MENU
:REALTIMEEXAMPLE3
java -jar ccmcl.jar -A "Examples\RealTime Example 3\apache.txt" -T 2 -S -B -p -R -e "Examples\RealTime Example 3\apache_simulation.jar" -d
GOTO MENU
:EXIT
echo End Demo