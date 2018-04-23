package compiler.generation;

import compiler.ir.*;
import compiler.symtable.SymbolTable;

import compiler.syntaxtree.MethodDecl;
import compiler.syntaxtree.ClassDecl;
import compiler.syntaxtree.ProgramSymbol;

import compiler.generation.Registers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;

public class Generation implements IrVisitor
{   
    public IR m_IR;
    private SymbolTable m_SymbolTable;
    
    private Registers m_AllocatedRegisters;
    
    private int m_RegisterGenerator;
    private static final int REGISTER_MIN = 16;
    private static final int REGISTER_MAX = 23;
    
    private int m_ParamNumber;
    private static final int PARAM_MIN = 4;
    private static final int PARAM_MAX = 7;
    
    private String m_CurrentFunction;

    public Generation(IR ir, SymbolTable symbolTable)
    {
        m_IR = ir;
        m_SymbolTable = symbolTable;
        
        m_AllocatedRegisters = new Registers(m_IR.m_Functions, m_SymbolTable);
        
        m_RegisterGenerator = REGISTER_MIN;
        m_ParamNumber = PARAM_MIN;
        
        m_SavedStack = new LinkedList<String>();
        m_SavedParameters = new LinkedList<String>();
    }
    
    public String generate()
    {
        m_AllocatedRegisters.allocate();
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
        
        m_CurrentFunction = name;
        
        // do the block for the function entrance first with stack allocation
        builder.append(name).append(":\n");
        if(name.equals("main") == false)
        {
            builder.append(storeRegisters());
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
                        break;
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
    
    private String storeRegisters()
    {
        return m_AllocatedRegisters.storeRegisters(m_CurrentFunction);
    }
    
    private String restoreRegisters()
    {
        return m_AllocatedRegisters.restoreRegisters(m_CurrentFunction);
    }
    
    public String visit(Quadruple quad)
    {
        return "";
    }
    
    public String visit(Copy copy)
    {
        StringBuilder builder = new StringBuilder();
        
        String to = m_AllocatedRegisters.get(copy.m_Result);
        String from = m_AllocatedRegisters.get(copy.m_Arg1);
        
        String spillTo = null;
        String spillFrom = null;
        
        if(to.charAt(0) != '$')
        {
            spillTo = to;
            to = "$t8";
        }
        
        // Integer literal
        if(from == null)
        {
            builder.append("ADDI ").append(to).append(", $zero, ").append(copy.m_Arg1);
        }
        else
        {
            if(from.charAt(0) != '$')
            {
                spillFrom = from;
                from = "$t8";
                builder.append("LW $t8, ").append(spillFrom).append("($sp)\n    ");
                
            }
            else if(to.equals(from))
            {
                return "";
            }
            
            builder.append("ADD ").append(to).append(", ").append(from).append(", $zero");
        }
        
        if(spillTo != null)
        {
            builder.append("\n    SW $t8, ").append(spillTo).append("($sp)");
        }
        
        return builder.toString();
    }
    
    public String visit(IrPrint irPrint)
    {
        StringBuilder builder = new StringBuilder();
        
        String print = m_AllocatedRegisters.get(irPrint.m_Arg1);

        String spillPrint = null;
        
        // Integer literal
        if(print == null)
        {
            builder.append("ADDI $a0, $zero, ").append(irPrint.m_Arg1).append("\n");
        }
        else
        {
            if(print.charAt(0) != '$')
            {
                spillPrint = print;
                print = "$t8";
                builder.append("LW $t8, ").append(spillPrint).append("($sp)\n    ");
            }
            
            builder.append("ADD $a0, $zero, ").append(print).append("\n");
        }
        
        builder.append("    JAL _system_out_println");
        
        return builder.toString();
    }
    
    public String visit(BinaryOp binOp)
    {
        StringBuilder builder = new StringBuilder();
        
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
        
        String spillDest = null;
        String spillLeft = null;
        String spillRight = null;
        
        if(dest.charAt(0) != '$')
        {
            spillDest = dest;
            dest = "$t8";
        }
        
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
            if(left.charAt(0) != '$')
            {
                spillLeft = left;
                left = "$t8";
                builder.append("LW $t8, ").append(spillLeft).append("($sp)\n    ");
            }
            if(right.charAt(0) != '$')
            {
                spillRight = right;
                right = "$t9";
                builder.append("LW $t9, ").append(spillRight).append("($sp)\n    ");
            }
            
            builder.append(op).append(dest).append(", ").append(left).append(", ").append(right);
        }
        
        if(spillDest != null)
        {
            builder.append("\n    SW $t8, ").append(spillDest).append("($sp)");
        }
    
        return builder.toString();
    }
    
    public String visit(New newObject)
    {
        StringBuilder builder = new StringBuilder();
        
        String dest = m_AllocatedRegisters.get(newObject.m_Result);
        
        int byteSize = ((ClassDecl)(m_SymbolTable.get(newObject.m_Arg1))).m_MemberVariables.size() * 4;
        
        builder.append("SUB $sp, $sp, 8\n");
        builder.append("    SW $v0, 0($sp)\n");
        builder.append("    SW $a0, 4($sp)\n");
        builder.append("    ADDI $a0, $zero, ").append(byteSize).append("\n");
        builder.append("    JAL _new_object\n");
        
        if(dest.charAt(0) != '$')
        {
            builder.append("    SW $v0, ").append(dest).append("($sp)\n");
        }
        else
        {
            builder.append("    ADD ").append(dest).append(", $v0, $zero\n");
        }
        
        builder.append("    LW $v0, 0($sp)\n");
        builder.append("    LW $a0, 4($sp)\n");
        builder.append("    ADD $sp, $sp, 8");
        
        return builder.toString();
    }
    
    public String visit(Param param)
    {
        storeAcrossCall("$" + m_ParamNumber, param.m_Arg1);
        m_ParamNumber++;
        
        return "";
    }
    
    public String visit(IrCall call)
    {
        m_ParamNumber = PARAM_MIN;
        
        StringBuilder builder = new StringBuilder();
        
        String destination = m_AllocatedRegisters.get(call.m_Result);
        if((destination.equals("$2") == false) && (destination.equals("$v0") == false))
        {
            storeAcrossCall("$v0", null);
        }
        
        String spillDest = null;
        if(destination.charAt(0) != '$')
        {
            spillDest = destination;
            destination = "$t8";
        }
        
        builder.append(saveAcrossCall());
        builder.append("    JAL ").append(call.m_Arg1).append("\n");
        builder.append("    ADD ").append(destination).append(", $v0, $zero\n");
        builder.append(restoreAcrossCall());
        if(spillDest != null)
        {
            builder.append("\n    SW $t8, ").append(spillDest).append("($sp)");
        }
        return builder.toString();
    }
    
    public String visit(Return returnIr)
    {
        StringBuilder builder = new StringBuilder();
        
        String value = m_AllocatedRegisters.get(returnIr.m_Arg1);
        
        String spillValue = null;
        
        if(value == null)
        {
            builder.append("ADDI $v0, $zero, ").append(returnIr.m_Arg1).append("\n");
        }
        else
        {
            if(value.charAt(0) != '$')
            {
                spillValue = value;
                value = "$t8";
                builder.append("LW $t8, ").append(spillValue).append("($sp)\n    ");
            }
            
            builder.append("ADD $v0, ").append(value).append(", $zero").append("\n");
        }
        
        builder.append(restoreRegisters());
        builder.append("    JR $ra");
        return builder.toString();
    }
    
    public String visit(UnaryOp op)
    {
        StringBuilder builder = new StringBuilder();
        
        // is only ever NOT
        String destination = m_AllocatedRegisters.get(op.m_Result);
        String value = m_AllocatedRegisters.get(op.m_Arg1);
        
        String spillDest = null;
        String spillValue = null;
        
        if(destination.charAt(0) != '$')
        {
            spillDest = destination;
            destination = "$t8";
        }
        if(value.charAt(0) != '$')
        {
            spillValue = value;
            value = "$t8";
            builder.append("LW $t8, ").append(spillValue).append("($sp)\n    ");
        }
        
        builder.append("XORI ").append(destination).append(", ").append(value).append(", 1");
        
        if(spillDest != null)
        {
            builder.append("\n    SW $t8, ").append(spillDest).append("($sp)");
        }
        
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
        
        String spillValue = null;
        if(value.charAt(0) != '$')
        {
            spillValue = value;
            value = "$t8";
            builder.append("LW $t8, ").append(spillValue).append("($sp)\n    ");
        }
        
        builder.append("BEQZ ").append(value).append(", ").append(jump.m_Result);
        
        return builder.toString();
    }
    
    public String visit(Length length)
    {
        return "";
    }
    
    public String visit(IrNewArray newArray)
    {
        return "";
    }
    
    public String visit(Index index)
    {
        return "";
    }
    
    private LinkedList<String> m_SavedStack;
    private LinkedList<String> m_SavedParameters;
    private void storeAcrossCall(String register, String parameter)
    {
        m_SavedStack.push(register);
        m_SavedParameters.add(parameter);
    }
    
    private String saveAcrossCall()
    {
        StringBuilder builder = new StringBuilder();
        
        int additionalOffset = m_SavedStack.size() * 4;
        builder.append("SUB $sp, $sp, ").append(additionalOffset).append("\n");
        
        int offset = 0;
        for(String register : m_SavedStack)
        {
            builder.append("    SW ").append(register).append(", ").append(offset).append("($sp)\n");
            offset += 4;
        }
        
        for(String parameter : m_SavedParameters)
        {
            if(parameter == null)
            {
                continue;
            }
            
            String register = m_AllocatedRegisters.get(parameter);
            String spillRegister = null;
            
            
            if(register == null)
            {
                builder.append("    ADDI $").append(m_ParamNumber++).append(", $zero, ").append(parameter).append("\n");
            }
            else
            {
                if(register.charAt(0) != '$')
                {
                    spillRegister = register;
                    register = "$t8";
                    builder.append("    LW $t8, ").append((Integer.decode(spillRegister) + additionalOffset)).append("($sp)\n");
                }
                builder.append("    ADD $").append(m_ParamNumber++).append(", $zero, ").append(register).append("\n");
            }
        }
        m_SavedParameters.clear();
        m_ParamNumber = PARAM_MIN;
        
        return builder.toString();
    }
    
    private String restoreAcrossCall()
    {
        StringBuilder builder = new StringBuilder();
        
        int offset = 0;
        while(m_SavedStack.isEmpty() == false)
        {
            String register = m_SavedStack.pop();
            builder.append("    LW ").append(register).append(", ").append(offset).append("($sp)\n");
            offset += 4;
        }
        builder.append("    ADDI $sp, $sp, ").append(offset).append("\n");

        return builder.toString();
    }
}