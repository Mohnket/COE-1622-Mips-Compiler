package compiler.visitor;

import compiler.syntaxtree.*;
import compiler.symtable.SymbolTable;

import java.util.LinkedList;

public class ClassExtendVisitor implements Visitor {

    private SymbolTable m_SymbolTable;
    private LinkedList<String> m_Scope;
    
    public boolean m_DerivedError;
    
    public ClassExtendVisitor(SymbolTable symbolTable)
    {
        m_SymbolTable = symbolTable;
        m_Scope = new LinkedList<String>();
        m_DerivedError = false;
    }

    public void extend(ClassDeclExtends derivedClass)
    {
        ProgramSymbol inheritedClass = m_SymbolTable.get(derivedClass.j.s);
        
        if(derivedClass.m_IsExtended)
        {
            return;
        }
        if(inheritedClass instanceof ClassDeclExtends)
        {
            extend((ClassDeclExtends)inheritedClass);
        }
        
        m_Scope.add(derivedClass.i.s);
        
        if(inheritedClass instanceof ClassDeclSimple)
        {
            ClassDeclSimple baseClass = (ClassDeclSimple)inheritedClass;
            
            for(int index = 0; index < baseClass.vl.size(); ++index)
            {
                VarDecl baseVar = baseClass.vl.elementAt(index);
                String[] superScope = baseVar.i.s.split("__");
                m_Scope.add(superScope[superScope.length - 1]);
                boolean success = m_SymbolTable.put(m_Scope.toArray(new String[0]), baseVar);
                if(success == false)
                {
                    m_DerivedError = true;
                    System.out.println(String.format("Redeclared base class variable %s line %d, character %d", baseVar.i.s, baseVar.m_Row, baseVar.m_Column));
                }
                
                m_Scope.removeLast();
            }
            
            for(int index = 0; index < baseClass.ml.size(); ++index)
            {
                MethodDecl baseMethod = baseClass.ml.elementAt(index);
                String[] superScope = baseMethod.i.s.split("__");
                m_Scope.add(superScope[superScope.length - 1]);
                boolean overriden = m_SymbolTable.contains(m_Scope.toArray(new String[0]));
                if(overriden == false)
                {
                    m_SymbolTable.put(m_Scope.toArray(new String[0]), baseMethod);
                }
                m_Scope.removeLast();
            }
            
            if(baseClass.m_MemberVariables.size() == 0)
            {
                for(int index = 0; index < baseClass.vl.size(); ++index)
                {
                    baseClass.m_MemberVariables.add(baseClass.vl.elementAt(index).i.s);
                    m_SymbolTable.addMemberIndex(baseClass.vl.elementAt(index).i.s, index);
                    derivedClass.m_MemberVariables.add(baseClass.m_MemberVariables.get(index));
                }
            }
        }
        else if(inheritedClass instanceof ClassDeclExtends)
        {
            ClassDeclExtends baseClass = (ClassDeclExtends)inheritedClass;
            
            for(int index = 0; index < baseClass.vl.size(); ++index)
            {
                VarDecl baseVar = baseClass.vl.elementAt(index);
                String[] superScope = baseVar.i.s.split("__");
                m_Scope.add(superScope[superScope.length - 1]);
                boolean success = m_SymbolTable.put(m_Scope.toArray(new String[0]), baseVar);
                if(success == false)
                {
                    m_DerivedError = true;
                    System.out.println(String.format("Redeclared base class variable %s line %d, character %d", baseVar.i.s, baseVar.m_Row, baseVar.m_Column));
                }
                m_Scope.removeLast();
            }
            
            for(int index = 0; index < baseClass.ml.size(); ++index)
            {
                MethodDecl baseMethod = baseClass.ml.elementAt(index);
                String[] superScope = baseMethod.i.s.split("__");
                m_Scope.add(superScope[superScope.length - 1]);
                boolean overriden = m_SymbolTable.contains(m_Scope.toArray(new String[0]));
                if(overriden == false)
                {
                    m_SymbolTable.put(m_Scope.toArray(new String[0]), baseMethod);
                }
                m_Scope.removeLast();
            }
            
            for(int index = 0; index < baseClass.m_MemberVariables.size(); ++index)
            {
                derivedClass.m_MemberVariables.add(baseClass.m_MemberVariables.get(index));
            }
        }
        
        
        
        for(int index = 0; index < derivedClass.vl.size(); ++index)
        {
            derivedClass.m_MemberVariables.add(derivedClass.vl.elementAt(index).i.s);
            m_SymbolTable.addMemberIndex(derivedClass.vl.elementAt(index).i.s, derivedClass.m_MemberVariables.size() - 1);
        }
        
        
        derivedClass.m_IsExtended = true;
        m_Scope.removeLast();
    }
    
    // MainClass m;
    // ClassDeclList cl;
    public void visit(Program n) {
        n.m.accept(this);
        for ( int i = 0; i < n.cl.size(); i++ ) {
                n.cl.elementAt(i).accept(this);
        }
    }
    
    // Identifier i1,i2;
    // Statement s;
    public void visit(MainClass n) {
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
    }
    
    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public void visit(ClassDeclSimple n) {
        n.i.accept(this);
        for ( int i = 0; i < n.vl.size(); i++ ) {
                n.vl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.ml.size(); i++ ) {
                n.ml.elementAt(i).accept(this);
        }
    }
 
    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public void visit(ClassDeclExtends n) {
        extend(n);
        
        n.i.accept(this);
        n.j.accept(this);
        for ( int i = 0; i < n.vl.size(); i++ ) {
                n.vl.elementAt(i).accept(this);
        }
        for ( int i = 0; i < n.ml.size(); i++ ) {
                n.ml.elementAt(i).accept(this);
        }
    }

    // Type t;
    // Identifier i;
    public void visit(VarDecl n) {
        n.t.accept(this);
        n.i.accept(this);
    }

    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDeclList vl;
    // StatementList sl;
    // Exp e;
    public void visit(MethodDecl n) {
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
    }

    // Type t;
    // Identifier i;
    public void visit(Formal n) {
        n.t.accept(this);
        n.i.accept(this);
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
