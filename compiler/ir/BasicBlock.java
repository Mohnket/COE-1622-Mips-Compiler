package compiler.ir;

import java.util.ArrayList;

public class BasicBlock
{
    public String m_Label;
    public String m_FallThrough;
    public String m_Jump;
    
    public boolean m_HasParent;
    
    public ArrayList<Quadruple> m_Block;
    
    public BasicBlock(String label)
    {
        m_Label = label;
        m_Block = new ArrayList<Quadruple>();
        
        m_HasParent = false;
    }
    
    public void addQuad(Quadruple quad)
    {
        if(quad instanceof UnconditionalJump)
        {
            m_Jump = quad.m_Result;
        }
        else if(quad instanceof ConditionalJump)
        {
            m_Jump = quad.m_Result;
        }
        else
        {
            
        }
        
        m_Block.add(quad);
    }
    
    public Quadruple[] getQuads()
    {
        return m_Block.toArray(new Quadruple[0]);
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        for(Quadruple quad : m_Block)
        {
            builder.append(quad.toString()).append("\n");
        }
        
        return builder.toString();
    }
}