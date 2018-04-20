package compiler.ir;

import compiler.generation.Generation;

public class Param extends Quadruple
{
    public Param(String arg1, String arg2, String result)
    {
        super(arg1, arg2, result);
    }
    
    public String toString()
    {
        return "param " + m_Result;
    }
    
    public String accept(Generation gen)
    {
        return gen.visit(this);
    }
}