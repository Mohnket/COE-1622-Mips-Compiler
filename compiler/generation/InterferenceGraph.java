package compiler.generation;

import compiler.ir.*;
import compiler.symtable.SymbolTable;

import compiler.syntaxtree.MethodDecl;
import compiler.syntaxtree.ProgramSymbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayDeque;

public class InterferenceGraph
{
    private static final int PARAM_MIN = 4;
    private static final int PARAM_MAX = 7;
    
    private static final int RETURN = 2;
    
    public static final int REGISTER_MIN = 8;
    public static final int REGISTER_MAX = 23;
    // +8 for temporary registers, should only be allocated by coallescing though
    private static final int REGISTER_SIZE = REGISTER_MAX - REGISTER_MIN + 1;
    
    HashMap<String, ArrayList<String>> m_Graph;
    HashMap<String, Integer> m_Colors;
    ArrayList<String> m_Nodes;
    private SymbolTable m_SymbolTable; 
    
    private int m_ReservedCount;
    public int m_MaxRegister;
    private int m_Spill;
    
    public InterferenceGraph(ArrayList<String> def, ArrayList<HashSet<String>> liveOut, SymbolTable symbolTable, ArrayList<Quadruple> straightCode)
    {
        m_Spill = 0;
        m_SymbolTable = symbolTable;
        m_Colors = new HashMap<String, Integer>();
        m_Colors.put("$t0", 8);
        m_Colors.put("$t1", 9);
        m_Colors.put("$t2", 10);
        m_Colors.put("$t3", 11);
        m_Colors.put("$t4", 12);
        m_Colors.put("$t5", 13);
        m_Colors.put("$t6", 14);
        m_Colors.put("$t7", 15);
        
        m_Graph = new HashMap<String, ArrayList<String>>();
        m_Graph.put("$t0", new ArrayList<String>());
        m_Graph.put("$t1", new ArrayList<String>());
        m_Graph.put("$t2", new ArrayList<String>());
        m_Graph.put("$t3", new ArrayList<String>());
        m_Graph.put("$t4", new ArrayList<String>());
        m_Graph.put("$t5", new ArrayList<String>());
        m_Graph.put("$t6", new ArrayList<String>());
        m_Graph.put("$t7", new ArrayList<String>());
        m_ReservedCount = 8;
        
        for(int index = 0; index < def.size(); ++index)
        {
            String currentVariable = def.get(index);
            if(currentVariable == null)
            {
                continue;
            }
            
            ArrayList<String> interferenceList = m_Graph.get(currentVariable);
            if(interferenceList == null)
            {
                interferenceList = new ArrayList<String>();
            }
            
            HashSet<String> set = new HashSet<String>(interferenceList);
            for(String out : liveOut.get(index))
            {
                set.add(out);
                
                // keep the undirectional, add a link from out to currentVariable
                ArrayList<String> outInterferenceList = m_Graph.get(out);
                if(outInterferenceList == null)
                {
                    outInterferenceList = new ArrayList<String>();
                }
                HashSet<String> outSet = new HashSet<String>(outInterferenceList);
                outSet.add(currentVariable);
                outSet.remove(out);
                if(straightCode.get(index) instanceof IrCall)
                {
                    interfereWithTemps(out, outSet, 8);
                }
                else if(straightCode.get(index) instanceof New)
                {
                    interfereWithTemps(out, outSet, 2);
                }
                else if(straightCode.get(index) instanceof IrNewArray)
                {
                    interfereWithTemps(out, outSet, 2);
                }
                
                outInterferenceList = new ArrayList<String>(outSet);
                m_Graph.put(out, outInterferenceList);
                
                if(out.equals(currentVariable))
                {
                    set.addAll(outInterferenceList);
                }
            }
            
            set.remove(currentVariable);
            interferenceList = new ArrayList<String>(set);
            m_Graph.put(currentVariable, interferenceList);
        }
        
        m_Nodes = new ArrayList<String>(m_Graph.keySet());
    }

    public void preColor(String functionName, String returnValue)
    {
        if(functionName.equals("main"))
        {
            return;
        }
        
        ProgramSymbol temp = m_SymbolTable.get(functionName);
        if(temp != null && temp instanceof MethodDecl)
        {
            MethodDecl methodDecl = (MethodDecl)temp;
            
            if(methodDecl.fl.size() <= 3)
            {
                // for coloring, this, the return value, and other parameters to be precolored, and temps
                m_ReservedCount += 2 + methodDecl.fl.size();
                
                int paramNumber = PARAM_MIN + 1;
                for(int index = 0; index < methodDecl.fl.size(); ++index)
                {
                    m_Colors.put(methodDecl.fl.elementAt(index).i.s, paramNumber);
                    paramNumber++;
                }
            }
            else
            {
                System.out.println("Methods cannot have more than 4 parameters including the implicit this");
                System.exit(1);
            }
            
            // avoids the case where a parameter is the return value
            if(m_Colors.get(returnValue) == null)
            {
                m_Colors.put(returnValue, RETURN);
            }
            
            m_Colors.put("this", PARAM_MIN);
            m_Graph.put("this", new ArrayList<String>());
        }
    }
    
    public void color()
    {
        ArrayDeque<String> stack = new ArrayDeque<String>();
        HashSet<String> pushedStrings = new HashSet<String>();

        // System.err.println(m_Graph.toString().replace("], ", "\n"));
        // System.err.println();
        
        int currentSize = m_Graph.size();
        while(currentSize > m_ReservedCount)
        {
            boolean simplified = false;
            for(Map.Entry<String, ArrayList<String>> node : m_Graph.entrySet())
            {
                String name = node.getKey();
                ArrayList<String> connections = node.getValue();
                
                if(m_Colors.get(name) != null)
                {
                    continue;
                }
                if(pushedStrings.contains(name))
                {
                    continue;
                }
                
                int connectionCount = 0;
                for(String connection : connections)
                {
                    if(m_Colors.get(connection) != null)
                    {
                        continue;
                    }
                    if(pushedStrings.contains(connection))
                    {
                        continue;
                    }
                    connectionCount++;
                }
                
                if(connectionCount < REGISTER_SIZE)
                {
                    stack.push(name);
                    pushedStrings.add(name);
                    currentSize--;
                    simplified = true;
                }
            }
            
            // coallesce
            if(simplified == false)
            {
                String node1 = null;
                String node2 = null;
                for(int outerIndex = 0; outerIndex < m_Nodes.size(); ++outerIndex)
                {
                    String node = m_Nodes.get(outerIndex);
                    if(pushedStrings.contains(node))
                    {
                        continue;
                    }
                    node1 = node;
                    
                    for(int innerIndex = outerIndex + 1; innerIndex < m_Nodes.size(); ++innerIndex)
                    {
                        node = m_Nodes.get(innerIndex);
                        if(pushedStrings.contains(node))
                        {
                            continue;
                        }
                        if(m_Colors.containsKey(node1) && m_Colors.containsKey(node))
                        {
                            continue;
                        }
                        node2 = node;
                        
                        if(canCoalesce(node1, node2))
                        {
                            break;
                        }
                        else
                        {
                            node2 = null;
                        }
                    }
                    
                    if(node2 != null)
                    {
                        break;
                    }
                }
                
                if(node2 != null)
                {
                    coallesce(node1, node2);
                    currentSize--;
                }
                else
                {
                    spillNode(pushedStrings);
                    currentSize--;
                }
            }
        }
        
        while(stack.isEmpty() == false)
        {
            String name = stack.pop();
            int minRegister = REGISTER_MIN;
            boolean tryAgain = true;
            
            while(tryAgain == true)
            {
                tryAgain = false;
                for(String connection : m_Graph.get(name))
                {
                    Integer register = m_Colors.get(connection);
                    if((register != null) && (register.equals(minRegister) == true))
                    {
                        minRegister++;
                        tryAgain = true;
                        break;
                    }
                }
            }
            
            if(minRegister > m_MaxRegister)
            {
                m_MaxRegister = minRegister;
            }
            m_Colors.put(name, minRegister);
        }
    }
    
    private boolean canCoalesce(String node1, String node2)
    {
        ArrayList<String> connections1 = m_Graph.get(node1);
        ArrayList<String> connections2 = m_Graph.get(node2);
        
        // Can't coallesce adjacent nodes
        if(connections1.contains(node2))
        {
            return false;
        }
        
        boolean retval = true;
        for(String connection : connections1)
        {
            retval &= m_Graph.get(connection).size() < REGISTER_SIZE;
            retval &= connections2.contains(connection);
            
            if(retval == false)
            {
                break;
            }
        }
        
        return retval;
    }
    
    private void coallesce(String node1, String node2)
    {
        String coallesce = node1 + "," + node2;
        
        if(m_Colors.containsKey(node1))
        {
            m_Colors.put(coallesce, m_Colors.get(node1));
        }
        if(m_Colors.containsKey(node2))
        {
            m_Colors.put(coallesce, m_Colors.get(node2));
        }
        
        ArrayList<String> connections1 = m_Graph.remove(node1);
        ArrayList<String> connections2 = m_Graph.remove(node2);
        HashSet<String> newConnections = new HashSet<String>(connections1);
        newConnections.addAll(connections2);
        ArrayList<String> connections = new ArrayList<String>(newConnections);
        
        for(String connection : connections)
        {
            ArrayList<String> otherConnections = m_Graph.get(connection);
            otherConnections.remove(node1);
            otherConnections.remove(node2);
            otherConnections.add(coallesce);
        }
        
        m_Nodes.remove(node1);
        m_Nodes.remove(node2);
        m_Nodes.add(coallesce);
        
        m_Graph.put(coallesce, connections);
    }
    
    private void spillNode(HashSet<String> pushedStrings)
    {
        int highestDegree = 0;
        int highestIndex = -1;
        for(int index = 0; index < m_Nodes.size(); ++index)
        {
            int degree = m_Graph.get(m_Nodes.get(index)).size();
            if((m_Colors.containsKey(m_Nodes.get(index)) == false) && (pushedStrings.contains(m_Nodes.get(index)) == false) && (degree > highestDegree))
            {
                highestIndex = index;
            }
        }
        
        if(highestIndex == -1)
        {
            System.out.println("Cannot allocate registers ");
            System.exit(1);
        }
        
        String node = m_Nodes.remove(highestIndex);
        m_Colors.put(node, m_Spill--);
        ArrayList<String> connections = m_Graph.remove(node);
        for(String connection : connections)
        {
            ArrayList<String> otherConnections = m_Graph.get(connection);
            otherConnections.remove(node);
        }
    }
    
    private void interfereWithTemps(String var, HashSet<String> varSet, int number)
    {
        String temps[] = new String[number];
        for(int num = 0; num < number; ++num)
        {
            temps[num]  = "$t" + num;
        }
        
        varSet.addAll(Arrays.asList(temps));
        
        for(String tempRegister : temps)
        {
            HashSet<String> temp = new HashSet<String>(m_Graph.get(tempRegister));
            temp.add(var);
            m_Graph.put(tempRegister, new ArrayList<String>(temp));
        }
    }
}