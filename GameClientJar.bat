javac -g --module-path Addons\JavaFX\javafx-sdk-11.0.2\lib --add-modules=javafx.controls GameClient.java *.java
jar -cmf mani.txt GameClient.jar *.class *.png
jar -tvf GameClient.jar
pause