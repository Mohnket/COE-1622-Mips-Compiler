package compiler.generation;

import compiler.ir.*;
import compiler.symtable.SymbolTable;

import compiler.syntaxtree.MethodDecl;
import compiler.syntaxtree.ProgramSymbol;

import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;

public class Generation
{   
    public IR m_IR;
    private SymbolTable m_SymbolTable;
    
    private HashMap<String, String> m_AllocatedRegisters;
    
    private int m_RegisterGenerator;
    private static final int REGISTER_MIN = 8;
    private static final int REGISTER_MAX = 25;
    
    private int m_ParamNumber;
    private static final int PARAM_MIN = 4;
    private static final int PARAM_MAX = 7;

    public Generation(IR ir, SymbolTable symbolTable)
    {
        m_IR = ir;
        m_SymbolTable = symbolTable;
        
        m_AllocatedRegisters = new HashMap<String, String>();
        
        m_RegisterGenerator = REGISTER_MIN;
        m_ParamNumber = PARAM_MIN;
    }
    
    public String generate()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append(".text\n");
        builder.append(generate("main", m_IR.m_Functions.get("main"))).append("\n");
        
        for(Map.Entry<String, HashMap<String, BasicBlock>> function : m_IR.m_Functions.entrySet())
        {
            if(function.getKey().equals("main"))
            {
                continue;
            }
            
            builder.append(generate(function.getKey(), function.getValue())).append("\n");
        }
        
        try
        {
            BufferedReader library = new BufferedReader(new FileReader(".\\compiler\\generation\\library.asm"));
            library.lines().forEach(s -> builder.append(s).append("\n"));
            library.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        
        return builder.toString();
    }
    
    private String generate(String name, HashMap<String, BasicBlock> function)
    {
        StringBuilder builder = new StringBuilder();
        
        // do the block for the function entrance first with stack allocation
        builder.append(name).append(":\n");
        if(name.equals("main") == false)
        {
            builder.append(storeRegisters());
        }
        
        // map the name of a param to it's register
        m_RegisterGenerator = REGISTER_MIN;
        ProgramSymbol temp = m_SymbolTable.get(name);
        if(temp != null && temp instanceof MethodDecl)
        {
            MethodDecl methodDecl = (MethodDecl)temp;
            if(methodDecl.fl.size() <= 3)
            {
                m_ParamNumber = PARAM_MIN + 1;
                for(int index = 0; index < methodDecl.fl.size(); ++index)
                {
                    tempGiveRegister(methodDecl.fl.elementAt(index).i.s);
                    builder.append("    ADD ").append(m_AllocatedRegisters.get(methodDecl.fl.elementAt(index).i.s));
                    builder.append(", $").append(m_ParamNumber++).append(", $zero").append("\n");
                }
                m_ParamNumber = PARAM_MIN;
            }
            else
            {
                System.out.println("Methods cannot have more than 4 parameters including the implicit this");
                System.exit(1);
            }
        }
        
        BasicBlock currentBlock = function.get(name);
        builder.append(generate(currentBlock)).append("\n");
        function.remove(name);

        // I dont think this is the proper way to do this, but it works
        currentBlock = function.get(currentBlock.m_FallThrough);
        while(function.isEmpty() == false)
        {
            while(currentBlock != null)
            {
                builder.append(currentBlock.m_Label).append(":\n");
                builder.append(generate(currentBlock)).append("\n");
                
                function.remove(currentBlock.m_Label);
                currentBlock = function.get(currentBlock.m_FallThrough);
            }
            
            if(function.isEmpty() == false)
            {
                // find another block. It cannot be the fallthrough of another block
                BasicBlock[] remainingBlocks = function.values().toArray(new BasicBlock[0]);
                for(BasicBlock newBlock : remainingBlocks)
                {
                    if(newBlock.m_HasParent == false)
                    {
                        currentBlock = newBlock;
                    }
                }
            }
        }
        
        return builder.toString();
    }
    
    private String generate(BasicBlock block)
    {
        StringBuilder builder = new StringBuilder();
        
        for(Quadruple quad : block.getQuads())
        {
            builder.append("    ").append(quad.accept(this)).append("\n");
        }
        
        if(block.m_Label.equals("main"))
        {
            builder.append("    J _system_exit\n");
        }
        
        return builder.toString();
    }
    
    private void tempGiveRegister(String var)
    {
        if((m_AllocatedRegisters.get(var) == null) && (var != null))
        {
            m_AllocatedRegisters.put(var, "$" + m_RegisterGenerator++);
        }
        
        if(m_RegisterGenerator > REGISTER_MAX)
        {
            System.out.println("Out of registers");
            System.exit(1);
        }
    }
    
    private String storeRegisters()
    {
        StringBuilder builder = new StringBuilder();
        
        // +2 one for ra, one to prevent overlap with the previous activation record
        builder.append("    SUB $sp, $sp, ").append((REGISTER_MAX - REGISTER_MIN + 2)*4).append("\n");
        
        int offset = 0;
        for(int register = REGISTER_MIN; register <= REGISTER_MAX; ++register)
        {
            builder.append("    SW $").append(register).append(", ").append(offset).append("($sp)").append("\n");
            offset += 4;
        }
        builder.append("    SW $ra, ").append(offset).append("($sp)").append("\n");
        
        return builder.toString();
    }
    
    private String restoreRegisters()
    {
        StringBuilder builder = new StringBuilder();
        
        int offset = 0;
        for(int register = REGISTER_MIN; register <= REGISTER_MAX; ++register)
        {
            builder.append("    LW $").append(register).append(", ").append(offset).append("($sp)").append("\n");
            offset += 4;
        }
        builder.append("    LW $ra, ").append(offset).append("($sp)").append("\n");
        
        // +2 one for ra, one to prevent overlap with the previous activation record
        builder.append("    ADD $sp, $sp, ").append((REGISTER_MAX - REGISTER_MIN + 2) * 4).append("\n");
        
        return builder.toString();
    }
    
    public String visit(Quadruple quad)
    {
        return "";
    }
    
    public String visit(Copy copy)
    {
        StringBuilder builder = new StringBuilder();
        
        tempGiveRegister(copy.m_Result);
        
        String to = m_AllocatedRegisters.get(copy.m_Result);
        String from = m_AllocatedRegisters.get(copy.m_Arg1);
        
        // Integer literal
        if(from == null)
        {
            builder.append("ADDI ").append(to).append(", $zero, ").append(copy.m_Arg1);
        }
        else
        {
            builder.append("ADD ").append(to).append(", ").append(from).append(", $zero");
        }
        
        return builder.toString();
    }
    
    public String visit(IrPrint irPrint)
    {
        StringBuilder builder = new StringBuilder();
        
        String print = m_AllocatedRegisters.get(irPrint.m_Arg1);
            
        // Integer literal
        if(print == null)
        {
            builder.append("ADDI $a0, $zero, ").append(irPrint.m_Arg1).append("\n");
        }
        else
        {
            builder.append("ADD $a0, $zero, ").append(print).append("\n");
        }
        
        builder.append("    JAL _system_out_println");
        
        return builder.toString();
    }
    
    public String visit(BinaryOp binOp)
    {
        StringBuilder builder = new StringBuilder();
        
        tempGiveRegister(binOp.m_Result);
        
        String op = null;
        switch(binOp.m_Op)
        {
            case BinaryOp.AND:
                op = "AND ";
                break;
            case BinaryOp.LESS:
                op = "SLT ";
                break;
            case BinaryOp.PLUS:
                op = "ADD ";
                break;
            case BinaryOp.MINUS:
                op = "SUB ";
                break;
            case BinaryOp.TIMES:
                op = "MUL ";
                break;
        }
        
        String dest = m_AllocatedRegisters.get(binOp.m_Result);
        String left = m_AllocatedRegisters.get(binOp.m_Arg1);
        String right = m_AllocatedRegisters.get(binOp.m_Arg2);
        
        // Integer liter
        if(right == null)
        {
            // Only slt doesn't have a pseudo instuction that accepts a integer literal
            if(binOp.m_Op == BinaryOp.LESS)
            {
                op = "SLTI ";
            }
            builder.append(op).append(dest).append(", ").append(left).append(", ").append(binOp.m_Arg2);
        }
        else
        {
            builder.append(op).append(dest).append(", ").append(left).append(", ").append(right);
        }    
        
    
        return builder.toString();
    }
    
    // Todo do this properly
    public String visit(New newObject)
    {
        tempGiveRegister(newObject.m_Result);
        
        return "ADD " + m_AllocatedRegisters.get(newObject.m_Result) + ", $zero, $zero";
    }
    
    public String visit(Param param)
    {
        if(m_ParamNumber <= PARAM_MAX)
        {
            StringBuilder builder = new StringBuilder();
            
            String value = m_AllocatedRegisters.get(param.m_Result);
            // Integer literal
            if(value == null)
            {
                builder.append("ADDI $").append(m_ParamNumber++).append(", $zero, ").append(param.m_Result);
            }
            else
            {
                builder.append("ADD $").append(m_ParamNumber++).append(", ").append(value).append(", $zero");
            }
            
            return builder.toString();
        }
        else
        {
            System.out.println("Can't have more than 4 parameters");
            System.exit(1);
        }
        
        return null;
    }
    
    public String visit(IrCall call)
    {
        m_ParamNumber = PARAM_MIN;
        
        tempGiveRegister(call.m_Result);
        
        StringBuilder builder = new StringBuilder();
        builder.append("JAL ").append(call.m_Arg1).append("\n");
        builder.append("    ADD ").append(m_AllocatedRegisters.get(call.m_Result)).append(", $v0, $zero");
        
        return builder.toString();
    }
    
    public String visit(Return returnIr)
    {
        StringBuilder builder = new StringBuilder();
        
        String value = m_AllocatedRegisters.get(returnIr.m_Result);
        
        if(value == null)
        {
            builder.append("ADDI $v0, $zero, ").append(returnIr.m_Result).append("\n");
        }
        else
        {
            builder.append("ADD $v0, ").append(value).append(", $zero").append("\n");
        }
        
        builder.append(restoreRegisters());
        builder.append("    JR $ra");
        return builder.toString();
    }
    
    public String visit(UnaryOp op)
    {
        StringBuilder builder = new StringBuilder();
        
        tempGiveRegister(op.m_Result);
        
        // is only ever NOT
        String destination = m_AllocatedRegisters.get(op.m_Result);
        String value = m_AllocatedRegisters.get(op.m_Arg1);
        
        builder.append("NOR ").append(destination).append(", ").append(value).append(", ").append(value);
        
    
        return builder.toString();
    }
    
    public String visit(UnconditionalJump jump)
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append("J ").append(jump.m_Result);
        
        return builder.toString();
    }
    
    public String visit(ConditionalJump jump)
    {
        StringBuilder builder = new StringBuilder();
        
        String value = m_AllocatedRegisters.get(jump.m_Arg1);
        builder.append("BEQZ ").append(value).append(", ").append(jump.m_Result);
        
        return builder.toString();
    }
}