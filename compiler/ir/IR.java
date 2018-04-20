package compiler.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IR
{
    public HashMap<String, HashMap<String, BasicBlock>> m_Functions;
    
    public IR()
    {
        m_Functions = new HashMap<String, HashMap<String, BasicBlock>>();
    }
    
    public void addFunction(String name, HashMap<String, BasicBlock> controlGraph)
    {
        m_Functions.put(name, controlGraph);
    }
    
    public ArrayList<HashMap<String, BasicBlock>> getFunctions()
    {
        return new ArrayList<HashMap<String, BasicBlock>>(m_Functions.values());
    }
    
    private String mainString()
    {
        HashMap<String, BasicBlock> mainFunction = m_Functions.get("main");
        StringBuilder builder = new StringBuilder();
        builder.append("Function name: main\n");
        
        BasicBlock main = mainFunction.get("main");
        builder.append("main:\t");
        if(main.m_FallThrough != null)
        {
            builder.append("(falls through to: ").append(main.m_FallThrough).append(")");
        }
        builder.append("\n").append(main.toString());

        
        for(BasicBlock block : mainFunction.values())
        {
            if(block.m_Label.equals("main") == false)
            {
                builder.append("\n").append(block.m_Label).append(":\t");
                
                if(block.m_FallThrough != null)
                {
                    builder.append("(falls through to: ").append(block.m_FallThrough).append(")");
                }
                
                builder.append("\n").append(block.toString());
            }
        }
        
        return builder.toString();
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(mainString()).append("\n");
        
        for(Map.Entry<String, HashMap<String, BasicBlock>> function : m_Functions.entrySet())
        {
            String functionName = function.getKey();
            if(functionName.equals("main"))
            {
                continue;
            }
            
            builder.append("\nFunction name: ").append(functionName).append("\n");
            HashMap<String, BasicBlock> labels = function.getValue();
            
            BasicBlock entry = labels.get(functionName);
            builder.append(functionName).append(":\t");
            if(entry.m_FallThrough != null)
            {
                builder.append("(falls through to: ").append(entry.m_FallThrough).append(")");
            }
            builder.append("\n").append(entry.toString());
            
            for(BasicBlock block : labels.values())
            {
                if(block.m_Label.equals(functionName) == false)
                {
                    builder.append("\n").append(block.m_Label).append(":\t");
                    
                    if(block.m_FallThrough != null)
                    {
                        builder.append("(falls through to: ").append(block.m_FallThrough).append(")");
                    }
                    
                    builder.append("\n").append(block.toString());
                }
            }
        }
        
        return builder.toString();
    }
}