package compiler.syntaxtree;
import compiler.visitor.Visitor;
import compiler.visitor.TypeVisitor;

public class Minus extends Exp {
  public Exp e1,e2;
  
  public Minus(Exp ae1, Exp ae2, int row, int column) {
      super(row, column);
    e1=ae1; e2=ae2;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
