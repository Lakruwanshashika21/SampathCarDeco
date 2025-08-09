@echo off


echo [📦] Creating JAR file...
jar cfm SampathCarDeco.jar manifest.txt -C out/ .


if exist SampathCarDeco.jar (
    echo [✅] JAR file created: SampathCarDeco.jar
) else (
    echo [❌] Failed to create JAR file.
)

pause
