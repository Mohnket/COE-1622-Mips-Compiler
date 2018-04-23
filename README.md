Author: Tyler Mohnke

While I have an amount of confidence that the compiler rejects invalid MiniJava programs, I do not give the level of 
error reporting granularity that is required. To give the granulairty neccessary would require a non-trivial redisign
which I simply do not have the time for.

Run from the project directory with:
java -cp .;compiler\java-cup-11a.jar compiler.Main <input_file>