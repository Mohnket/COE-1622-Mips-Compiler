package compiler.visitor;

import compiler.syntaxtree.*;
import compiler.symtable.SymbolTable;

import java.util.LinkedList;

public class TypeCheckVisitor implements TypeVisitor
{
    private SymbolTable m_SymbolTable;
    private LinkedList<String> m_Scope;
    
    public boolean m_TypeError;
    
    public TypeCheckVisitor(SymbolTable symbolTable)
    {
        m_SymbolTable = symbolTable;
        m_Scope = new LinkedList<String>();
        m_TypeError = false;
    }
    
    public void error(String id, int line, int column)
    {
        m_TypeError = true;
        System.out.println(String.format("Use of undefined identifier %s at line %d, character %d", id, line, column));
    }

    // MainClass m;
    // ClassDeclList cl;
    public Type visit(Program n)
    {
        n.m.accept(this);
        for ( int i = 0; i < n.cl.size(); i++ ) {
                n.cl.elementAt(i).accept(this);
        }
        return null;
    }
    
    // Identifier i1,i2;
    // Statement s;
    public Type visit(MainClass n) {
        m_Scope.add(n.i1.s);
        
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
        
        m_Scope.removeLast();
        return null;
    }
    
    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Type visit(ClassDeclSimple n)
    {
        m_Scope.add(n.i.s);
        
        n.i.accept(this);
        for ( int i = 0; i < n.vl.size(); i++ ) {
                n.vl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.ml.size(); i++ ) {
                n.ml.elementAt(i).accept(this);
        }
        
        m_Scope.removeLast();
        return null;
    }
 
    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Type visit(ClassDeclExtends n) {
        m_Scope.add(n.i.s);
        
        n.i.accept(this);
        n.j.accept(this);
        for ( int i = 0; i < n.vl.size(); i++ ) {
                n.vl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.ml.size(); i++ ) {
                n.ml.elementAt(i).accept(this);
        }
        
        m_Scope.removeLast();
        return null;
    }

    // Type t;
    // Identifier i;
    public Type visit(VarDecl n) {
        n.t.accept(this);
        n.i.accept(this);
        return null;
    }

    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDeclList vl;
    // StatementList sl;
    // Exp e;
    public Type visit(MethodDecl n) {
        m_Scope.add(n.i.s);
        
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
        return null;
    }

    // Type t;
    // Identifier i;
    public Type visit(Formal n) {
        n.t.accept(this);
        n.i.accept(this);
        return n.t;
    }

    public Type visit(IntArrayType n) {
        return n;
    }

    public Type visit(BooleanType n) {
        return n;
    }

    public Type visit(IntegerType n) {
        return n;
    }

    // String s;
    public Type visit(IdentifierType n) {
        return n;
    }

    // StatementList sl;
    public Type visit(Block n) {
        for ( int i = 0; i < n.sl.size(); i++ ) {
                n.sl.elementAt(i).accept(this);
        }
        return null;
    }

    // Exp e;
    // Statement s1,s2;
    public Type visit(If n) {
        Type type = n.e.accept(this);
        
        if(!(type instanceof BooleanType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non-boolean expression used as the condition of if statement at line %d, character %d", n.m_Row, n.m_Column));
        }    
        
        n.s1.accept(this);
        n.s2.accept(this);
        return null;
    }

    // Exp e;
    // Statement s;
    public Type visit(While n) {
        Type type = n.e.accept(this);
        
        if(!(type instanceof BooleanType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non-boolean expression used as the condition of while statement at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        n.e.accept(this);
        n.s.accept(this);
        return null;
    }

    // Exp e;
    public Type visit(Print n) {
        Type type = n.e.accept(this);
        
        if(!(type instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Print only takes an int", n.m_Row, n.m_Column));
        }
        
        return null;
    }
    
    // Identifier i;
    // Exp e;
    public Type visit(Assign n) {
        Type left = n.i.accept(this);
        Type right = n.e.accept(this);
        
        // System.out.println(left);
        // System.out.println(right);
        
        if(!(left.getClass().getName().equals(right.getClass().getName())))
        {
            m_TypeError = true;
            System.out.println(String.format("Type mismatch during assignment at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        if((left instanceof IdentifierType) && (right instanceof IdentifierType) && !((IdentifierType)left).s.equals(((IdentifierType)right).s))
        {
            m_TypeError = true;
            System.out.println(String.format("Type mismatch during assignment at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return null;
    }

    // Identifier i;
    // Exp e1,e2;
    public Type visit(ArrayAssign n) {
        Type type = n.i.accept(this);
        
        if(!(type instanceof IntArrayType))
        {
            m_TypeError = true;
            
            String trueType = null;
            if(type instanceof IdentifierType)
            {
                trueType = ((IdentifierType)type).s;
            }
            else
            {
                trueType = type.getClass().getName();
            }
            
            System.out.println(String.format("Invalid l-value, %s is a %s, at line %d, character %d",n.i.s, trueType, n.m_Row, n.m_Column));
        }
        

        type = n.e1.accept(this);
        if(!(type instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non int index at line %s", n.m_Row));
        }
        
        type = n.e2.accept(this);
        if(!(type instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Type mismatch during assignment at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return null;
    }

    // Exp e1,e2;
    public Type visit(And n) {
        Type left = n.e1.accept(this);
        Type right = n.e2.accept(this);
        
        if(!(left instanceof BooleanType) || !(right instanceof BooleanType))
        {
            m_TypeError = true;
            System.out.println(String.format("Attempt to use boolean operator && on non-boolean operands at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return new BooleanType(0,0);
    }

    // Exp e1,e2;
    public Type visit(LessThan n) {
        Type left = n.e1.accept(this);
        Type right = n.e2.accept(this);
        
        if(!(left instanceof IntegerType) || !(right instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non-integer operand for operator < at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return new BooleanType(0,0);
    }

    // Exp e1,e2;
    public Type visit(Plus n) {
        Type left = n.e1.accept(this);
        Type right = n.e2.accept(this);
        
        if(!(left instanceof IntegerType) || !(right instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non-integer operand for operator + at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return new IntegerType(0,0);
    }

    // Exp e1,e2;
    public Type visit(Minus n) {
        Type left = n.e1.accept(this);
        Type right = n.e2.accept(this);
        
        if(!(left instanceof IntegerType) || !(right instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non-integer operand for operator - at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return new IntegerType(0,0);
    }

    // Exp e1,e2;
    public Type visit(Times n) {
        Type left = n.e1.accept(this);
        Type right = n.e2.accept(this);
        
        if(!(left instanceof IntegerType) || !(right instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non-integer operand for operator * at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return new IntegerType(0,0);
    }

    // Exp e1,e2;
    public Type visit(ArrayLookup n) {
        Type identifier = n.e1.accept(this);
        Type index = n.e2.accept(this);
        
        if(!(identifier instanceof IntArrayType))
        {
            m_TypeError = true;
            System.out.println(String.format("Type is not an array at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        if(!(index instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Array is not indexed by an integer at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return new IntegerType(0,0);
    }

    // Exp e;
    public Type visit(ArrayLength n) {
        Type identifier = n.e.accept(this);
        if(!(identifier instanceof IntArrayType))
        {
            m_TypeError = true;
            System.out.println(String.format("Length property only applies to arrays, line %d, character %d", n.m_Row, n.m_Column));
        }
        return new IntegerType(0,0);
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public Type visit(Call n) {
        Type identifier = n.e.accept(this);
        String functionName = n.i.s;
        
        if(!(identifier instanceof IdentifierType))
        {
            m_TypeError = true;
            System.out.println(String.format("Non-class operand for operator . at line %d, character %d", n.m_Row, n.m_Column));
        }
        else
        {
            String className = ((IdentifierType)identifier).s;
            if(m_SymbolTable.get(className) == null)
            {
                m_TypeError = true;
                System.out.println(String.format("Class %s not declared at line %d, character %d", className, n.m_Row, n.m_Column));
                return null;
            }
            
            functionName = m_SymbolTable.getFullName(new String[] {className, functionName});
            if(functionName == null)
            {
                m_TypeError = true;
                System.out.println(String.format("Attempt to call a non-method at line %d, character %d", n.m_Row, n.m_Column));
                return null;
            }
            
            MethodDecl function = (MethodDecl)m_SymbolTable.get(functionName);
            n.i.s = function.i.s;
            
            // System.out.println(functionName);
            // System.out.println(function.t);
            if(function.fl.size() != n.el.size())
            {
                m_TypeError = true;
                System.out.println(String.format("Call of method %s does not match its declared number of arguments at line %d, character %d", functionName, n.m_Row, n.m_Column));
                return function.t;
            }
            else
            {
                for ( int i = 0; i < n.el.size(); i++ )
                {
                    Type given = n.el.elementAt(i).accept(this);
                    Type required = function.fl.elementAt(i).accept(this);
                    
                    String givenType = given.getClass().getName();
                    String requiredType = required.getClass().getName();
                    // System.out.println(givenType);
                    // System.out.println(requiredType);
                    
                    if(!(givenType.equals(requiredType)))
                    {
                        m_TypeError = true;
                        System.out.println(String.format("Call of method %s does not match its declared signature at line %d, character %d", functionName, n.m_Row, n.m_Column));
                        return function.t;
                    }
                    
                    if((given instanceof IdentifierType) && (required instanceof IdentifierType) && !(m_SymbolTable.isDerived(((IdentifierType)required).s, ((IdentifierType)given).s)))
                    {
                        m_TypeError = true;
                        System.out.println(String.format("Call of method %s does not match its declared signature at line %d, character %d", functionName, n.m_Row, n.m_Column));
                        return function.t;
                    }
                }
            }
            
            return function.t;
        }
        
        return null;
    }

    // int i;
    public Type visit(IntegerLiteral n) {
        return new IntegerType(0,0);
    }

    public Type visit(True n) {
        return new BooleanType(0,0);
    }

    public Type visit(False n) {
        return new BooleanType(0,0);
    }

    // String s;
    public Type visit(IdentifierExp n) {
        m_Scope.add(n.s);
        String newName = m_SymbolTable.getFullName(m_Scope.toArray(new String[0]));
        // System.out.println(m_Scope);
        m_Scope.removeLast();
        
        if(newName == null)
        {
            error(n.s, n.m_Row, n.m_Column);
            return new IdentifierType(n.s, 0, 0);
        }
        
        n.s = newName;
        ProgramSymbol symbol = m_SymbolTable.get(n.s);
        
        // System.out.println(newName);
        // System.out.println(symbol);
        
        if(symbol instanceof BooleanType)
        {
            return new BooleanType(0,0);
        }
        else if(symbol instanceof IntegerType)
        {
            return new IntegerType(0,0);
        }
        else if(symbol instanceof IntArrayType)
        {
            return new IntArrayType(0,0);
        }
        else if(symbol instanceof ClassDeclSimple)
        {
            return new IdentifierType(((ClassDeclSimple)symbol).i.s, 0, 0);
        }
        else if(symbol instanceof ClassDeclExtends)
        {
            return new IdentifierType(((ClassDeclExtends)symbol).i.s, 0, 0);
        }
        else if(symbol instanceof Formal)
        {
            return ((Formal)symbol).t;
        }
        else if(symbol instanceof VarDecl)
        {
            return ((VarDecl)symbol).t;
        }
        else if(symbol instanceof MethodDecl)
        {
            return ((MethodDecl)symbol).t;
        }
        else
        {
            // Shouldn't occur
            System.out.println("Shouldnt occur exp");
            return new IdentifierType(n.s, 0, 0);
        }
    }

    public Type visit(This n) {
        return new IdentifierType(m_Scope.get(0), 0, 0);
    }

    // Exp e;
    public Type visit(NewArray n) {
        Type type = n.e.accept(this);
        
        if(!(type instanceof IntegerType))
        {
            m_TypeError = true;
            System.out.println(String.format("Array length not an integer, line %d, character %d", n.m_Row, n.m_Column));
        }
        return new IntArrayType(0, 0);
    }

    // Identifier i;
    public Type visit(NewObject n) {
        return new IdentifierType(n.i.s, 0, 0);
    }

    // Exp e;
    public Type visit(Not n) {
        Type type = n.e.accept(this);
        
        if(!(type instanceof BooleanType))
        {
            m_TypeError = true;
            System.out.println(String.format("Attempt to use boolean operator ! on non-boolean operands at line %d, character %d", n.m_Row, n.m_Column));
        }
        
        return new BooleanType(0,0);
    }

    // String s;
    public Type visit(Identifier n) {
        m_Scope.add(n.s);
        String newName = m_SymbolTable.getFullName(m_Scope.toArray(new String[0]));
        // System.out.println(m_Scope);
        m_Scope.removeLast();
        
        if(newName == null)
        {
            error(n.s, 0, 0);
            return new IdentifierType(n.s, 0, 0);
        }
        
        n.s = newName;
        ProgramSymbol symbol = m_SymbolTable.get(n.s);
        
        // System.out.println(newName);
        // System.out.println(symbol);
        
        if(symbol instanceof BooleanType)
        {
            return new BooleanType(0,0);
        }
        else if(symbol instanceof IntegerType)
        {
            return new IntegerType(0,0);
        }
        else if(symbol instanceof IntArrayType)
        {
            return new IntArrayType(0,0);
        }
        else if(symbol instanceof ClassDeclSimple)
        {
            return new IdentifierType(((ClassDeclSimple)symbol).i.s, 0, 0);
        }
        else if(symbol instanceof ClassDeclExtends)
        {
            return new IdentifierType(((ClassDeclExtends)symbol).i.s, 0, 0);
        }
        else if(symbol instanceof Formal)
        {
            return ((Formal)symbol).t;
        }
        else if(symbol instanceof VarDecl)
        {
            return ((VarDecl)symbol).t;
        }
        else if(symbol instanceof MethodDecl)
        {
            return ((MethodDecl)symbol).t;
        }
        else
        {
            // Shouldn't occur
            System.out.println("Shouldnt occur id ");
            return new IdentifierType(n.s, 0, 0);
        }
    }
}
