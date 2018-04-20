CLASSPATH=./compiler/java-cup-11a.jar

source = $(wildcard ./compiler/syntaxtree/*.java)  \
		 $(wildcard ./compiler/visitor/*.java) \
		 $(wildcard ./compiler/symtable/*.java) \
		 $(wildcard ./compiler/ir/*.java) \
		 $(wildcard ./compiler/*.java)
         
main: Parser.java Lexer.java
	javac -classpath $(CLASSPATH) $(source)

Parser.java:
	java -jar ./compiler/java-cup-11a.jar -destdir ./compiler -parser Parser ./compiler/Parser.cup
    
Lexer.java:
	jflex ./compiler/Lexer.flex