package compiler.syntaxtree;
import compiler.visitor.Visitor;
import compiler.visitor.TypeVisitor;

public abstract class Statement extends Node{
    public Statement(int row, int column)
    {
        super(row, column);
    }
    
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
