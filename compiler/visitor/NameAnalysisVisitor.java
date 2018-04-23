package compiler.visitor;

import compiler.syntaxtree.*;
import compiler.symtable.SymbolTable;

import java.util.LinkedList;

public class NameAnalysisVisitor implements Visitor
{
    private SymbolTable m_SymbolTable;
    private LinkedList<String> m_Scope;
    
    public boolean m_NameCollision;
    
    public NameAnalysisVisitor(SymbolTable symbolTable)
    {
        m_SymbolTable = symbolTable;
        m_Scope = new LinkedList<String>();
        m_NameCollision = false;
    }
        
    public void error(String id, int line, int column)
    {
        m_NameCollision = true;
        System.out.println(String.format("Multiply defined identifier %s at line %d, character %d", id, line, column));
    }
    
    // MainClass m;
    // ClassDeclList cl;
    public void visit(Program n)
    {
        n.m.accept(this);
        for(int i = 0; i < n.cl.size(); i++)
        {
            n.cl.elementAt(i).accept(this);
        }
    }
    
    // Identifier i1,i2;
    // Statement s;
    public void visit(MainClass n)
    {
        // Main class special class
        m_SymbolTable.put(n.i1.s, (ProgramSymbol)(new ClassDeclSimple(n.i1, null, null, 0, 0)));
        String argument = n.i1.s + "::" + n.i2.s;
        m_SymbolTable.put(argument, new Formal(new IntegerType(0,0), n.i2, 0, 0));
        
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
    }
    
    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public void visit(ClassDeclSimple n)
    {
        m_Scope.add(n.i.s);
        
        String newName = m_SymbolTable.makeFullName(m_Scope.toArray(new String[0]));
        n.i.s = newName;
        boolean success = m_SymbolTable.put(n.i.s, (ProgramSymbol)n);
        if(success == false)
        {
            error(n.i.s, n.m_Row, n.m_Column);
        }
        
        n.i.accept(this);
        for ( int i = 0; i < n.vl.size(); i++ ) {
                n.vl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.ml.size(); i++ ) {
                n.ml.elementAt(i).accept(this);
        }
        
        m_Scope.removeLast();
    }
 
    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public void visit(ClassDeclExtends n)
    {
        m_Scope.add(n.i.s);
        m_SymbolTable.putDerived(n.j.s, n.i.s);
        
        String newName = m_SymbolTable.makeFullName(m_Scope.toArray(new String[0]));
        n.i.s = newName;
        boolean success = m_SymbolTable.put(n.i.s, (ProgramSymbol)n);
        if(success == false)
        {
            error(n.i.s, n.m_Row, n.m_Column);
        }
        
        n.i.accept(this);
        n.j.accept(this);
        for ( int i = 0; i < n.vl.size(); i++ ) {
                n.vl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.ml.size(); i++ ) {
                n.ml.elementAt(i).accept(this);
        }
        
        m_Scope.removeLast();
    }

    // Type t;
    // Identifier i;
    public void visit(VarDecl n)
    {
        m_Scope.add(n.i.s);
        
        String newName = m_SymbolTable.makeFullName(m_Scope.toArray(new String[0]));
        n.i.s = newName;
        boolean success = m_SymbolTable.put(n.i.s, (ProgramSymbol)n);
        if(success == false)
        {
            error(n.i.s, n.m_Row, n.m_Column);
        }
        
        n.t.accept(this);
        n.i.accept(this);
        
        m_Scope.removeLast();
    }

    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDeclList vl;
    // StatementList sl;
    // Exp e;
    public void visit(MethodDecl n)
    {
        m_Scope.add(n.i.s);
        
        String newName = m_SymbolTable.makeFullName(m_Scope.toArray(new String[0]));
        n.i.s = newName;
        boolean success = m_SymbolTable.put(n.i.s, (ProgramSymbol)n);
        if(success == false)
        {
            error(n.i.s, n.m_Row, n.m_Column);
        }
        
        n.t.accept(this);
        n.i.accept(this);
        for ( int i = 0; i < n.fl.size(); i++ ) {
                n.fl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.vl.size(); i++ ) {
                n.vl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.sl.size(); i++ ) {
                n.sl.elementAt(i).accept(this);
        }
        n.e.accept(this);
        
        m_Scope.removeLast();
    }

    // Type t;
    // Identifier i;
    public void visit(Formal n) {
        m_Scope.add(n.i.s);
        
        String newName = m_SymbolTable.makeFullName(m_Scope.toArray(new String[0]));
        n.i.s = newName;
        boolean success = m_SymbolTable.put(n.i.s, (ProgramSymbol)n);
        if(success == false)
        {
            error(n.i.s, n.m_Row, n.m_Column);
        }
        
        n.t.accept(this);
        n.i.accept(this);
        
        m_Scope.removeLast();
    }

    public void visit(IntArrayType n) {
    }

    public void visit(BooleanType n) {
    }

    public void visit(IntegerType n) {
    }

    // String s;
    public void visit(IdentifierType n) {
    }

    // StatementList sl;
    public void visit(Block n) {
        for ( int i = 0; i < n.sl.size(); i++ ) {
                n.sl.elementAt(i).accept(this);
        }
    }

    // Exp e;
    // Statement s1,s2;
    public void visit(If n) {
        n.e.accept(this);
        n.s1.accept(this);
        n.s2.accept(this);
    }

    // Exp e;
    // Statement s;
    public void visit(While n) {
        n.e.accept(this);
        n.s.accept(this);
    }

    // Exp e;
    public void visit(Print n) {
        n.e.accept(this);
    }
    
    // Identifier i;
    // Exp e;
    public void visit(Assign n) {
        n.i.accept(this);
        n.e.accept(this);
    }

    // Identifier i;
    // Exp e1,e2;
    public void visit(ArrayAssign n) {
        n.i.accept(this);
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // Exp e1,e2;
    public void visit(And n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // Exp e1,e2;
    public void visit(LessThan n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // Exp e1,e2;
    public void visit(Plus n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // Exp e1,e2;
    public void visit(Minus n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // Exp e1,e2;
    public void visit(Times n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // Exp e1,e2;
    public void visit(ArrayLookup n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // Exp e;
    public void visit(ArrayLength n) {
        n.e.accept(this);
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public void visit(Call n) {
        n.e.accept(this);
        n.i.accept(this);
        for ( int i = 0; i < n.el.size(); i++ ) {
                n.el.elementAt(i).accept(this);
        }
    }

    // int i;
    public void visit(IntegerLiteral n) {
    }

    public void visit(True n) {
    }

    public void visit(False n) {
    }

    // String s;
    public void visit(IdentifierExp n) {
    }

    public void visit(This n) {
    }

    // Exp e;
    public void visit(NewArray n) {
        n.e.accept(this);
    }

    // Identifier i;
    public void visit(NewObject n) {
    }

    // Exp e;
    public void visit(Not n) {
        n.e.accept(this);
    }

    // String s;
    public void visit(Identifier n) {
    }
}
