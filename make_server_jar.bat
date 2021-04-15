javac --module-path Addons\JavaFX\javafx-sdk-11.0.2\lib --add-modules=javafx.controls -cp . GameServer.java
javac -g --module-path Addons\JavaFX\javafx-sdk-11.0.2\lib --add-modules=javafx.controls GameServer.java *.java
jar -cmf maniServer.txt GameServer.jar *.class
jar -tvf GameServer.jar
pause