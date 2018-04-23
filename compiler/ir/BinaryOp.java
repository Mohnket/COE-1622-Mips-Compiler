package compiler.ir;

import compiler.generation.IrVisitor;

public class BinaryOp extends Quadruple
{
    public static final int AND = 0;
    public static final int LESS = 1;
    public static final int PLUS = 2;
    public static final int MINUS = 3;
    public static final int TIMES = 4;
    public int m_Op;
    
    public BinaryOp(String arg1, String arg2, String result, int op)
    {
        super(arg1, arg2, result);
        m_Op = op;
    }
    
    public String toString()
    {
        switch(m_Op)
        {
            case AND:
                return m_Result + " := " + m_Arg1 + " && " + m_Arg2;
            case LESS:
                return m_Result + " := " + m_Arg1 + " < " + m_Arg2;
            case PLUS:
                return m_Result + " := " + m_Arg1 + " + " + m_Arg2;
            case MINUS:
                return m_Result + " := " + m_Arg1 + " - " + m_Arg2;
            case TIMES:
                return m_Result + " := " + m_Arg1 + " * " + m_Arg2;
            default:
                return null;
        }
    }
    
    public String accept(IrVisitor visitor)
    {
        return visitor.visit(this);
    }
}