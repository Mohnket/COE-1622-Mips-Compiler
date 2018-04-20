package compiler.ir;

import compiler.generation.Generation;

public class Return extends Quadruple
{
    public Return(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return "return " + m_Result;
    }
    
    public String accept(Generation gen)
    {
        return gen.visit(this);
    }
}