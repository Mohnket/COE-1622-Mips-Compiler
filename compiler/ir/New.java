package compiler.ir;

import compiler.generation.IrVisitor;

public class New extends Quadruple
{
    public New(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return m_Result + " := new " + m_Arg1; 
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}