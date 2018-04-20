package compiler.ir;

import compiler.generation.Generation;

public class ConditionalJump extends Quadruple
{
    public ConditionalJump(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return "iffalse " + m_Arg1 + " goto " + m_Result;
    }
    
    public String accept(Generation gen)
    {
        return gen.visit(this);
    }
}