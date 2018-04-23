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

import java.util.regex.Pattern;

public class Registers
{
    public static final int REGISTER_MIN = 16;
    public static final int REGISTER_MAX = 23;
    
    private static final Pattern f_IsDigit = Pattern.compile("-?\\d+");
    private SymbolTable m_SymbolTable;
    private HashMap<String, HashMap<String, BasicBlock>> m_Program;
    private HashMap<String, String> m_AllocatedRegisters;
    private HashMap<String, Integer> m_NormalSpills;
    private HashMap<String, Integer> m_BadSpiils;
    
    public Registers(HashMap<String, HashMap<String, BasicBlock>> program, SymbolTable symbolTable)
    {
        m_Program = program;
        m_SymbolTable = symbolTable;
        m_AllocatedRegisters = new HashMap<String, String>();
        m_NormalSpills = new HashMap<String, Integer>();
        m_BadSpiils = new HashMap<String, Integer>();
    }
    
    public int getSpilledRegisters(String functionName)
    {
        Integer value = m_NormalSpills.get(functionName);
        if(value == null)
        {
            return 0;
        }
        else
        {
            return value;
        }
    }
    
    public String storeRegisters(String functionName)
    {
        StringBuilder builder = new StringBuilder();
        
        int storedRegisters = this.getSpilledRegisters(functionName);
        // +1 for ra
        int allocationSize = storedRegisters + 1 + m_BadSpiils.get(functionName);
        
        builder.append("    SUB $sp, $sp, ").append(allocationSize * 4).append("\n");
        
        // lowest indexes are reserved for spilled registers
        int offset = m_BadSpiils.get(functionName) * 4;
        for(int register = REGISTER_MIN; register < REGISTER_MIN + storedRegisters; ++register)
        {
            builder.append("    SW $").append(register).append(", ").append(offset).append("($sp)").append("\n");
            offset += 4;
        }
        builder.append("    SW $ra, ").append(offset).append("($sp)").append("\n");
        
        return builder.toString();
    }
    
    public String restoreRegisters(String functionName)
    {
        StringBuilder builder = new StringBuilder();
        
        int storedRegisters = this.getSpilledRegisters(functionName);
        // +1 for ra
        int allocationSize = storedRegisters + 1 + m_BadSpiils.get(functionName);
        
        int offset = m_BadSpiils.get(functionName) * 4;
        for(int register = REGISTER_MIN; register < REGISTER_MIN + storedRegisters; ++register)
        {
            builder.append("    LW $").append(register).append(", ").append(offset).append("($sp)").append("\n");
            offset += 4;
        }
        builder.append("    LW $ra, ").append(offset).append("($sp)").append("\n");
        
        // +2 one for ra, one to prevent overlap with the previous activation record
        builder.append("    ADD $sp, $sp, ").append(allocationSize * 4).append("\n");
        
        return builder.toString();
    }
    
    public void allocate()
    {
        for(Map.Entry<String, HashMap<String, BasicBlock>> function : m_Program.entrySet())
        {
            allocate(function.getKey(), function.getValue());
        }
    }
    
    private void allocate(String functionName, HashMap<String, BasicBlock> blocks)
    {
        // create a list of quads and a map to find the additional successors of a jump
        
        HashMap<String, BasicBlock> blocksCopy = new HashMap<String, BasicBlock>();
        for(Map.Entry<String, BasicBlock> entry : blocks.entrySet())
        {
            blocksCopy.put(entry.getKey(), entry.getValue());
        }
        
        // Copied from generation. Need to get all the quads in order
        ArrayList straightCode = new ArrayList<Quadruple>();
        HashMap<String, Integer> labels = new HashMap<String, Integer>();
        
        BasicBlock currentBlock = blocksCopy.get(functionName);
        straightCode.addAll(Arrays.asList(currentBlock.getQuads()));
        blocksCopy.remove(functionName);
        
        currentBlock = blocksCopy.get(currentBlock.m_FallThrough);
        while(blocksCopy.isEmpty() == false)
        {
            while(currentBlock != null)
            {
                labels.put(currentBlock.m_Label, straightCode.size());
                straightCode.addAll(Arrays.asList(currentBlock.getQuads()));
                blocksCopy.remove(currentBlock.m_Label);
                currentBlock = blocksCopy.get(currentBlock.m_FallThrough);
            }
            
            if(blocksCopy.isEmpty() == false)
            {
                BasicBlock[] remainingBlocks = blocksCopy.values().toArray(new BasicBlock[0]);
                for(BasicBlock newBlock : remainingBlocks)
                {
                    if(newBlock.m_HasParent == false)
                    {
                        currentBlock = newBlock;
                        break;
                    }
                }
            }
        }
        
        allocate(straightCode, labels, functionName);
    }
    
    // @note index's defs and uses are weird
    private void allocate(ArrayList<Quadruple> straightCode, HashMap<String, Integer> labels, String functionName)
    {
        ArrayList<HashSet<String>> liveIn = new ArrayList<HashSet<String>>();
        ArrayList<HashSet<String>> liveOut = new ArrayList<HashSet<String>>();
        ArrayList<HashSet<String>> use = new ArrayList<HashSet<String>>();
        ArrayList<String> def = new ArrayList<String>();
        
        for(int count = 0; count < straightCode.size(); ++count)
        {
            liveOut.add(new HashSet<String>());
            
            HashSet<String> uses = new HashSet<String>();
            Quadruple currentQuad = straightCode.get(count);
            
            // member exist in memory and do not interfere with anything
            // call & new do not "use" their arguments, every other quad does
            if(((currentQuad instanceof IrCall) == false) && ((currentQuad instanceof New) == false))
            {
                if((currentQuad.m_Arg1 != null) && (f_IsDigit.matcher(currentQuad.m_Arg1).matches() == false) && (currentQuad.m_Arg1.split("__").length != 2))
                {
                    uses.add(currentQuad.m_Arg1);
                }
                if((currentQuad.m_Arg2 != null) && (f_IsDigit.matcher(currentQuad.m_Arg2).matches() == false) && (currentQuad.m_Arg2.split("__").length != 2))
                {
                    uses.add(currentQuad.m_Arg2);
                }
            }
            use.add(uses);
            
            // jumps do not define their results
            if(((currentQuad instanceof ConditionalJump) == false) && ((currentQuad instanceof UnconditionalJump) == false))
            {
                if((currentQuad.m_Result != null) && (currentQuad.m_Result.split("__").length != 2))
                {
                    def.add(currentQuad.m_Result);
                }
                else
                {
                    def.add(null);
                }
            }
            else
            {
                def.add(null);
            }
            
            liveIn.add(new HashSet<String>(uses));
        }
        
        boolean changed = true;
        while(changed == true)
        {
            changed = false;
            // The last statement never changes across al iterations
            for(int index = straightCode.size() - 2; index >= 0; --index)
            {
                Quadruple currentQuad = straightCode.get(index);
                HashSet<String> currentOut = liveOut.get(index);
                HashSet<String> currentIn = liveIn.get(index);
                
                if(currentQuad instanceof UnconditionalJump)
                {
                    int succIndex = labels.get(currentQuad.m_Result);
                    changed |= currentOut.addAll(liveIn.get(succIndex));
                }
                else if(currentQuad instanceof ConditionalJump)
                {
                    int succIndex = labels.get(currentQuad.m_Result);
                    changed |= currentOut.addAll(liveIn.get(succIndex));
                    changed |= currentOut.addAll(liveIn.get(index + 1));
                }
                else
                {
                    changed |= currentOut.addAll(liveIn.get(index + 1));
                }
                
                HashSet<String> outCopy = new HashSet<String>(currentOut);
                outCopy.remove(def.get(index));
                
                changed |= currentIn.addAll(outCopy);
            }
        }

        InterferenceGraph graph = new InterferenceGraph(def, liveOut, m_SymbolTable, straightCode);
        graph.preColor(functionName, straightCode.get(straightCode.size() - 1).m_Arg1);
        graph.color();
        
        int spills = 0;
        for(Map.Entry<String, Integer> allocatedRegisters : graph.m_Colors.entrySet())
        {
            String[] names = allocatedRegisters.getKey().split(",");
            Integer position = allocatedRegisters.getValue();
            if(position > 0)
            {
                String register = "$" + position.toString();
                for(String name : names)
                {
                    m_AllocatedRegisters.put(name, register);
                }
            }
            else
            {
                int stackLocation = -4 * position;
                for(String name : names)
                {
                    m_BadSpiils.put(name, stackLocation);
                }
                spills++;
            }
        }
        
        m_BadSpiils.put(functionName, spills);
        if(graph.m_MaxRegister != 0)
        {
            int spillAmt = graph.m_MaxRegister - REGISTER_MIN + 1;
            if(spillAmt < 0)
            {
                spillAmt = 0;
            }
            
            m_NormalSpills.put(functionName, spillAmt);
        }
    }
    
    public String get(String name)
    {
        String normalName = m_AllocatedRegisters.get(name);
        if(normalName != null)
        {
            return normalName;
        }
        
        Integer spillLocation = m_BadSpiils.get(name);
        if(spillLocation != null)
        {
            return spillLocation.toString();
        }
        
        Integer memberVariable = m_SymbolTable.getMemberIndex(name);
        if(memberVariable != null)
        {
            return memberVariable.toString();
        }
        
        return null;
    }
}