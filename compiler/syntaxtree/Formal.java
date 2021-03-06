package compiler.syntaxtree;
import compiler.visitor.Visitor;
import compiler.visitor.TypeVisitor;

public class Formal extends ProgramSymbol{
  public Type t;
  public Identifier i;
 
  public Formal(Type at, Identifier ai, int row, int column) {
      super(row, column);
    t=at; i=ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
