package compiler.syntaxtree;
import compiler.visitor.Visitor;
import compiler.visitor.TypeVisitor;

public class IntArrayType extends Type {
  public IntArrayType(int row, int column)
  {
      super(row, column);
  }
    
  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
