REM This script performs the jni4net wrapper generation for KinectHandler and SpeechRecognition.

REM It needs to be started from the folder it is located in (jni4net_generation) and in the Visual Studio Command Prompt (Start -> VisualStudio -> Visual Studio Tools).

REM The ouput (dlls and jars) will be located in the lib folder.

del /s /f /q .\work\*

copy ..\bin\Debug\MicrosoftKinectWrapper.dll .\work\

proxygen .\work\MicrosoftKinectWrapper.dll -wd work

cd work

call build.cmd

copy *.jar ..\lib\
copy *.dll ..\lib\

copy *.jar ..\..\..\..\org.jnect.core\lib\
copy *.dll ..\..\..\..\org.jnect.core\lib\

cd ..

copy *.jar .\lib\
copy *.dll .\lib\

copy *.jar .\..\..\..\org.jnect.core\lib\
copy *.dll .\..\..\..\org.jnect.core\lib\

echo.
echo Generation process terminated. Results can be found in the folder lib. 
echo Press any key, to exit...


pause > NUL