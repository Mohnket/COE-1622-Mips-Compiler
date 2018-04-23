package compiler.ir;

import compiler.generation.IrVisitor;

public abstract class Quadruple
{
    public String m_Arg1;
    public String m_Arg2;
    public String m_Result;
    
    public Quadruple(String arg1, String arg2, String result)
    {
        m_Arg1 = arg1;
        m_Arg2 = arg2;
        m_Result = result;
    }

    public abstract String accept(IrVisitor visitor);
}