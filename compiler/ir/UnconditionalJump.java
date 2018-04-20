package compiler.ir;

import compiler.generation.Generation;

public class UnconditionalJump extends Quadruple
{
    public UnconditionalJump(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return "goto " + m_Result;
    }
    
    public String accept(Generation gen)
    {
        return gen.visit(this);
    }
}