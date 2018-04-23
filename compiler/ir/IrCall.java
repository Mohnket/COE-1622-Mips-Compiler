package compiler.ir;

import compiler.generation.IrVisitor;

public class IrCall extends Quadruple
{
    public IrCall(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return m_Result + " := call " + m_Arg1 + " " + m_Arg2;
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}