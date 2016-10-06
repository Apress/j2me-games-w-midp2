# This script builds and preverifies the code 
# for the example games from the book.

# reset this variable to the path to the correct javac
# command on your system:
JAVA4_HOME=/usr/java/j2sdk1.4.0_01/bin
# reset this variable to the corresct path to the WTK2.0
# directory of the WTK2.0 toolkit that you downloaded:
WTK2_HOME=../../../../WTK2.0

echo "clear directories"
# it's better not to leave old class files lying 
# around where they might accidentally get picked up 
# and create errors...
rm ../tmpclasses/net/frog_parrot/*/*.class
rm ../classes/net/frog_parrot/*/*.class
rm ../serverclasses/net/frog_parrot/*/*.class

echo "Compiling source files"

$JAVA4_HOME/javac -bootclasspath $WTK2_HOME/lib/midpapi.zip -d ../tmpclasses -classpath ../tmpclasses ../src/net/frog_parrot/util/*.java ../src/net/frog_parrot/dungeon/*.java ../src/net/frog_parrot/http/*.java ../src/net/frog_parrot/checkers/*.java 

$JAVA4_HOME/javac -d ../serverclasses -classpath ../../../lib/servlet.jar ../src/net/frog_parrot/server/*.java ../src/net/frog_parrot/servlet/*.java 

echo "placing servlet"
mv ../serverclasses/net/frog_parrot/servlet/*.class /home/carol/j2me/jakarta-tomcat-4.1.27/webapps/games/WEB-INF/classes/net/frog_parrot/servlet

echo "Preverifying class files"

$WTK2_HOME/bin/preverify -classpath $WTK2_HOME/lib/midpapi.zip:../tmpclasses -d ../classes ../tmpclasses

echo "Jarring preverified class files"
$JAVA4_HOME/jar cmf MANIFEST.MF book.jar -C ../classes .

echo "Updating JAR size info in JAD file..."

NB=`wc -l book.jad | awk '{print $1}'`
head --lines=$(($NB-1)) book.jad > book.jad1
echo "MIDlet-Jar-Size:" `stat -c '%s' book.jar`>> book.jad1
cp book.jad1 book.jad
