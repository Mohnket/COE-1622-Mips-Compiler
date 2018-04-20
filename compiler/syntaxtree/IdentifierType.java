package compiler.syntaxtree;
import compiler.visitor.Visitor;
import compiler.visitor.TypeVisitor;

public class IdentifierType extends Type {
  public String s;

  public IdentifierType(String as, int row, int column) {
    super(row, column);
    s=as;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
