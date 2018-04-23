package compiler.visitor;

import compiler.syntaxtree.*;
import compiler.symtable.SymbolTable;
import compiler.ir.*;

import java.util.HashMap;
import java.util.TreeMap;

public class IrVisitor implements Visitor {

    public IR m_IR;

    private SymbolTable m_SymbolTable;
    private HashMap<String, BasicBlock> m_ControlGraph;
    
    private BasicBlock m_CurrentBlock;

    private static final String LABEL = "_label_";
    private static final String VAR = "_var_";
    private int labelGen;
    private int varGen;
    
    public IrVisitor(SymbolTable symbolTable)
    {
        m_SymbolTable = symbolTable;
        m_ControlGraph = new HashMap<String, BasicBlock>();
        m_IR = new IR();
    }
    
    public String lastResult()
    {
        return m_CurrentBlock.m_Block.get(m_CurrentBlock.m_Block.size() - 1).m_Result;
    }
    
    // MainClass m;
    // ClassDeclList cl;
    public void visit(Program n)
    {
        n.m.accept(this);
        
        for ( int i = 0; i < n.cl.size(); i++ ) {
                n.cl.elementAt(i).accept(this);
        }
    }
    
    // Identifier i1,i2;
    // Statement s;
    public void visit(MainClass n)
    {
        m_CurrentBlock = new BasicBlock("main");
        m_ControlGraph.put(m_CurrentBlock.m_Label, m_CurrentBlock);
        
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
        
        m_IR.addFunction("main", m_ControlGraph);
        m_ControlGraph = new HashMap<String, BasicBlock>();
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
    public void visit(MethodDecl n)
    {
        m_CurrentBlock = new BasicBlock(n.i.s);
        m_ControlGraph.put(m_CurrentBlock.m_Label, m_CurrentBlock);
        
        for ( int i = 0; i < n.sl.size(); i++ )
        {
                n.sl.elementAt(i).accept(this);
        }
        n.e.accept(this);
        m_CurrentBlock.addQuad(new Return(lastResult(), null, null));
        
        m_IR.addFunction(n.i.s, m_ControlGraph);
        m_ControlGraph = new HashMap<String, BasicBlock>();
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
    public void visit(If n)
    {
        n.e.accept(this);
        
        BasicBlock ifTrue = new BasicBlock(LABEL + labelGen++);
        BasicBlock ifFalse = new BasicBlock(LABEL + labelGen++);
        BasicBlock nextBlock = new BasicBlock(LABEL + labelGen++);
        m_ControlGraph.put(ifTrue.m_Label, ifTrue);
        m_ControlGraph.put(ifFalse.m_Label, ifFalse);
        m_ControlGraph.put(nextBlock.m_Label, nextBlock);
        
        m_CurrentBlock.addQuad(new ConditionalJump(lastResult(), null, ifFalse.m_Label));
        m_CurrentBlock.m_FallThrough = ifTrue.m_Label;
        ifTrue.m_HasParent = true;
        m_CurrentBlock = ifTrue;
        
        n.s1.accept(this);
        
        m_CurrentBlock.addQuad(new UnconditionalJump(null, null, nextBlock.m_Label));
        m_CurrentBlock = ifFalse;
        
        n.s2.accept(this);
        m_CurrentBlock.m_FallThrough = nextBlock.m_Label;
        nextBlock.m_HasParent = true;
        m_CurrentBlock = nextBlock;
    }

    // Exp e;
    // Statement s;
    public void visit(While n) {
        BasicBlock expressionCheck = new BasicBlock(LABEL + labelGen++);
        BasicBlock whileBlock = new BasicBlock(LABEL + labelGen++);
        BasicBlock nextBlock = new BasicBlock(LABEL + labelGen++);
        m_ControlGraph.put(expressionCheck.m_Label, expressionCheck);
        m_ControlGraph.put(whileBlock.m_Label, whileBlock);
        m_ControlGraph.put(nextBlock.m_Label, nextBlock);
        
        m_CurrentBlock.m_FallThrough = expressionCheck.m_Label;
        expressionCheck.m_HasParent = true;
        m_CurrentBlock = expressionCheck;
        
        n.e.accept(this);
        
        m_CurrentBlock.addQuad(new ConditionalJump(lastResult(), null, nextBlock.m_Label));
        m_CurrentBlock.m_FallThrough = whileBlock.m_Label;
        whileBlock.m_HasParent = true;
        m_CurrentBlock = whileBlock;
        
        n.s.accept(this);
        
        m_CurrentBlock.addQuad(new UnconditionalJump(null, null, expressionCheck.m_Label));
        m_CurrentBlock = nextBlock;
    }

    // Exp e;
    public void visit(Print n) {
        n.e.accept(this);
        
        m_CurrentBlock.addQuad(new IrPrint(lastResult(), null, null));
    }
    
    // Identifier i;
    // Exp e;
    public void visit(Assign n) {
        n.i.accept(this);
        n.e.accept(this);
        m_CurrentBlock.addQuad(new Copy(lastResult(), null, n.i.s));
    }

    // Identifier i;
    // Exp e1,e2;
    public void visit(ArrayAssign n) {
        n.i.accept(this);
        n.e1.accept(this);
        String index = lastResult();
        n.e2.accept(this);
        m_CurrentBlock.addQuad(new Copy(index, lastResult(), n.i.s));
    }

    // Exp e1,e2;
    public void visit(And n) {
        n.e1.accept(this);
        String left = lastResult();
        n.e2.accept(this);
        m_CurrentBlock.addQuad(new BinaryOp(left, lastResult(), VAR + varGen++, BinaryOp.AND));
    }

    // Exp e1,e2;
    public void visit(LessThan n) {
        n.e1.accept(this);
        String left = lastResult();
        n.e2.accept(this);
        m_CurrentBlock.addQuad(new BinaryOp(left, lastResult(), VAR + varGen++, BinaryOp.LESS));
    }

    // Exp e1,e2;
    public void visit(Plus n) {
        n.e1.accept(this);
        String left = lastResult();
        n.e2.accept(this);
        m_CurrentBlock.addQuad(new BinaryOp(left, lastResult(), VAR + varGen++, BinaryOp.PLUS));
    }

    // Exp e1,e2;
    public void visit(Minus n) {
        n.e1.accept(this);
        String left = lastResult();
        n.e2.accept(this);
        m_CurrentBlock.addQuad(new BinaryOp(left, lastResult(), VAR + varGen++, BinaryOp.MINUS));
    }

    // Exp e1,e2;
    public void visit(Times n) {
        n.e1.accept(this);
        String left = lastResult();
        n.e2.accept(this);
        m_CurrentBlock.addQuad(new BinaryOp(left, lastResult(), VAR + varGen++, BinaryOp.TIMES));
    }

    // Exp e1,e2;
    public void visit(ArrayLookup n) {
        n.e1.accept(this);
        String name = lastResult();
        n.e2.accept(this);
        m_CurrentBlock.addQuad(new Index(name, lastResult(), VAR + varGen++, Index.LOAD));
    }

    // Exp e;
    public void visit(ArrayLength n) {
        n.e.accept(this);
        m_CurrentBlock.addQuad(new Length(lastResult(), null, VAR + varGen++));
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public void visit(Call n)
    {
        Param[] paramerters = new Param[n.el.size() + 1];
        
        for ( int i = 0; i < n.el.size(); i++ )
        {
            n.el.elementAt(i).accept(this);
            paramerters[i + 1] = new Param(lastResult(), null, null);
        }
        
        n.e.accept(this);
        paramerters[0] = new Param(lastResult(), null, null);
        
        for(int index = 0; index < paramerters.length; ++index)
        {
            m_CurrentBlock.addQuad(paramerters[index]);
        }
        m_CurrentBlock.addQuad(new IrCall(n.i.s, Integer.valueOf(paramerters.length).toString(), VAR + varGen++));
    }

    // int i;
    public void visit(IntegerLiteral n) {
        m_CurrentBlock.addQuad(new Copy(Integer.valueOf(n.i).toString(), null, VAR + varGen++));
    }

    public void visit(True n) {
        m_CurrentBlock.addQuad(new Copy(Integer.valueOf(1).toString(), null, VAR + varGen++));
    }

    public void visit(False n) {
        m_CurrentBlock.addQuad(new Copy(Integer.valueOf(0).toString(), null, VAR + varGen++));
    }

    // String s;
    public void visit(IdentifierExp n) {
        m_CurrentBlock.addQuad(new Copy(n.s, null, n.s));
    }

    public void visit(This n) {
        m_CurrentBlock.addQuad(new Copy("this", null, "this"));
    }

    // Exp e;
    public void visit(NewArray n) {
        n.e.accept(this);
        m_CurrentBlock.addQuad(new IrNewArray(lastResult(), null, VAR + varGen++));
    }

    // Identifier i;
    public void visit(NewObject n) {
        m_CurrentBlock.addQuad(new New(n.i.s, null, VAR + varGen++));
    }

    // Exp e;
    public void visit(Not n) {
        n.e.accept(this);
        m_CurrentBlock.addQuad(new UnaryOp(lastResult(), null, VAR + varGen++, UnaryOp.NOT));
    }

    // String s;
    public void visit(Identifier n) {
    }
}
