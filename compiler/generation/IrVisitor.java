package compiler.generation;

import compiler.ir.*;

public interface IrVisitor
{
    public String visit(BinaryOp quad);
    public String visit(ConditionalJump quad);
    public String visit(Copy quad);
    public String visit(Index quad);
    public String visit(IrCall quad);
    public String visit(IrNewArray quad);
    public String visit(IrPrint quad);
    public String visit(Length quad);
    public String visit(New quad);
    public String visit(Param quad);
    public String visit(Quadruple quad);
    public String visit(Return quad);
    public String visit(UnaryOp quad);
    public String visit(UnconditionalJump quad);
}