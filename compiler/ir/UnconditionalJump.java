package compiler.ir;

import compiler.generation.IrVisitor;

public class UnconditionalJump extends Quadruple
{
    public UnconditionalJump(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return "goto " + m_Result;
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}