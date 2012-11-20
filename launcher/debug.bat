cd ../jars
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Xincgc -Xms1024M -Xmx1024M -cp "../bin/minecraft:../lib:../lib/*:../jars/bin/minecraft.jar:../jars/bin/jinput.jar:../jars/bin/lwjgl.jar:../jars/bin/lwjgl_util.jar" -Djava.library.path="../jars/bin/natives" Start
