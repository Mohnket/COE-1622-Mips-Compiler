package compiler.ir;

import compiler.generation.IrVisitor;

public class Param extends Quadruple
{
    public Param(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return "param " + m_Arg1;
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}