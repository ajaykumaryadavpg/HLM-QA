#!/bin/sh
export JAVA_HOME=/Users/ajaykumar.yadav/tools/jdk17/Contents/Home
cd /Users/ajaykumar.yadav/novus-main
$JAVA_HOME/bin/java -classpath /Users/ajaykumar.yadav/tools/maven/boot/plexus-classworlds-2.7.0.jar -Dclassworlds.conf=/Users/ajaykumar.yadav/tools/maven/bin/m2.conf -Dmaven.home=/Users/ajaykumar.yadav/tools/maven -Dmaven.multiModuleProjectDirectory=/Users/ajaykumar.yadav/novus-main org.codehaus.plexus.classworlds.launcher.Launcher clean test -pl novus-example-tests -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml
