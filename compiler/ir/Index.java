package compiler.ir;

import compiler.generation.Generation;

public class Index extends Quadruple
{
    public static final int STORE = 0;
    public static final int LOAD = 1;
    public int m_Op;
    
    public Index(String arg1, String arg2, String result, int op)
    {
        super(arg1, arg2, result);
        m_Op = op;
    }
    
    public String toString()
    {
        switch(m_Op)
        {
            case STORE:
                return m_Result + "[" + m_Arg1 + "] := " + m_Arg2;
            case LOAD:
                return m_Result + " := " + m_Arg1 + "[" + m_Arg2 + "]";
            default:
                return null;
        }
    }
    
    public String accept(Generation gen)
    {
        return gen.visit(this);
    }
}