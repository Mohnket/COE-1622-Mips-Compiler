java -jar .\compiler\java-cup-11a.jar -destdir compiler -parser Parser .\compiler\Parser.cup
call jflex .\compiler\Lexer.flex
javac -classpath .;.\compiler\java-cup-11a.jar .\compiler\*.java