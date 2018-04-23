package compiler.ir;

import compiler.generation.IrVisitor;

public class UnaryOp extends Quadruple
{
    public static final int NOT = 0;
    public int m_Op;
    
    public UnaryOp(String arg1, String arg2, String result, int op)
    {
        super(arg1, arg2, result);
        m_Op = op;
    }
    
    public String toString()
    {
        switch(m_Op)
        {
            case NOT:
                return m_Result + " := ! " + m_Arg1;
            default:
                return null;
        }
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}