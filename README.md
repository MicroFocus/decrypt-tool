# decrypt-tool 

Decrypt tool is used in Open Deduplication (ODD). Tools in Micro-Services is upgraded to java 17 except this project.

Decrypt tool is compiled with Java 11 because of the dependency on ODD Java upgrade.Hence, decrypt-tool is no longer part of nightly build.
The compiled jar file(i.e. converter-tool.jar) is stored under folder "\src\Micro-Services\tools\decrypt-tool\staging" for packaging.

## Current version

Current converter-tool.jar file is from DP release 11.03.

## How to update converter-tool.jar 

Steps:
1. Make the needed changes in the project and compile in Java 11.
2. Compiled jar file needs to signed.
3. Copy the signed jar file to src\Micro-Services\tools\decrypt-tool\staging.

## Compiling the decrypt-tool
### System requirement          
    1. Updated Linux EL8+
    2. JAVA/JDK 17
    3. Good processing compute resources
    
### Compile            

```
    cd /decrypt-tool/
    mvn -s ~/.m2/settings.xml clean -X --debug
    mvn -s ~/.m2/settings.xml versions:set -DnewVersion=1.0.0 -X --debug
    mvn -s ~/.m2/settings.xml package -X --debug
```
