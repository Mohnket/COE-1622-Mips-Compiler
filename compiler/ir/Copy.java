package compiler.ir;

import compiler.generation.IrVisitor;

public class Copy extends Quadruple
{
    public Copy(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return m_Result + " := " + m_Arg1;
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}