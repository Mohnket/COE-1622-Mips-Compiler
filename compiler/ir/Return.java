package compiler.ir;

import compiler.generation.IrVisitor;

public class Return extends Quadruple
{
    public Return(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return "return " + m_Arg1;
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}