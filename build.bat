REM java -jar .\compiler\java-cup-11a.jar -destdir compiler -parser Parser .\compiler\Parser.cup
REM call jflex .\compiler\Lexer.flex
javac -classpath .;.\compiler\java-cup-11a.jar .\compiler\generation\*.java
javac -classpath .;.\compiler\java-cup-11a.jar .\compiler\ir\*.java
javac -classpath .;.\compiler\java-cup-11a.jar .\compiler\symtable\*.java
javac -classpath .;.\compiler\java-cup-11a.jar .\compiler\syntaxtree\*.java
javac -classpath .;.\compiler\java-cup-11a.jar .\compiler\visitor\*.java
javac -classpath .;.\compiler\java-cup-11a.jar .\compiler\*.java