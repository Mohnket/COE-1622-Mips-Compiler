package compiler.syntaxtree;
import compiler.visitor.Visitor;
import compiler.visitor.TypeVisitor;

public abstract class Type extends ProgramSymbol{
  public Type(int row, int column)
  {
      super(row, column);
  }
    
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
