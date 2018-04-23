package compiler;

import java_cup.runtime.*;
import java.io.FileReader;
import compiler.syntaxtree.Program;
import compiler.visitor.NameAnalysisVisitor;
import compiler.visitor.TypeCheckVisitor;
import compiler.visitor.IrVisitor;
import compiler.visitor.ClassExtendVisitor;
import compiler.symtable.SymbolTable;
import compiler.generation.*;

import java.io.PrintWriter;

public class Main
{
    public static void main(String... args) throws Exception
    {
        // Create a parser
        FileReader fileReader = new FileReader(args[0]);
        Scanner lexer = new Lexer(fileReader);
        Parser javaParser = new Parser(lexer);
        
        Program program = (Program)javaParser.parse().value;
        if(javaParser.errorOccured)
        {
            System.exit(1);
        }
        
        SymbolTable symbolTable = new SymbolTable();
        NameAnalysisVisitor nameVisitor = new NameAnalysisVisitor(symbolTable);
        nameVisitor.visit(program);
        // symbolTable.print();
        
        if(nameVisitor.m_NameCollision)
        {
            System.exit(1);
        }
        
        ClassExtendVisitor extedVisitor = new ClassExtendVisitor(symbolTable);
        extedVisitor.visit(program);
        
        if(extedVisitor.m_DerivedError)
        {
            System.exit(1);
        }
        
        TypeCheckVisitor typeVisitor = new TypeCheckVisitor(symbolTable);
        typeVisitor.visit(program);
        
        if(typeVisitor.m_TypeError)
        {
            System.exit(1);
        }
        
        IrVisitor irVisitor = new IrVisitor(symbolTable);
        irVisitor.visit(program);
        
        // System.err.println(irVisitor.m_IR.toString());
        
        Generation mipsGenerator = new Generation(irVisitor.m_IR, symbolTable);
        
        PrintWriter writer = new PrintWriter(args[1]);
        writer.write(mipsGenerator.generate());
        writer.close();
        // mipsGenerator.generate();
        // System.out.println(mipsGenerator.generate());
    }
}