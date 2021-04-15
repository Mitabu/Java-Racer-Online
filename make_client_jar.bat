javac --module-path Addons\JavaFX\javafx-sdk-11.0.2\lib --add-modules=javafx.controls -cp . GameClient.java
javac -g --module-path Addons\JavaFX\javafx-sdk-11.0.2\lib --add-modules=javafx.controls GameClient.java *.java
jar -cmf mani.txt GameClient.jar *.class
jar -tvf GameClient.jar
pause