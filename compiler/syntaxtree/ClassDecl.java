package compiler.syntaxtree;
import compiler.visitor.Visitor;
import compiler.visitor.TypeVisitor;

import java.util.ArrayList;

public abstract class ClassDecl extends ProgramSymbol {
    public ArrayList<String> m_MemberVariables;
    
    public ClassDecl(int row, int column)
    {
        super(row, column);
        m_MemberVariables = new ArrayList<String>();
    }
    
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
