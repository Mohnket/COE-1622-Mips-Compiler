package compiler.ir;

import compiler.generation.Generation;

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
    
    public String accept(Generation gen)
    {
        return gen.visit(this);
    }
}