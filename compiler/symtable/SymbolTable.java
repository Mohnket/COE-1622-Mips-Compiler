package compiler.symtable;
import compiler.syntaxtree.ProgramSymbol;

import java.util.HashMap;
import java.util.Arrays;

public class SymbolTable
{
    private HashMap<String, ProgramSymbol> m_SymbolTable;
    private HashMap<String, String> m_Derivations;
    private HashMap<String, Integer> m_MemberIndex;
    
    public SymbolTable()
    {
        m_SymbolTable = new HashMap<String, ProgramSymbol>();
        m_Derivations = new HashMap<String, String>();
        m_MemberIndex = new HashMap<String, Integer>();
    }
    
    public boolean put(String fullyQualifiedName, ProgramSymbol node)
    {
        ProgramSymbol existingSymbol = m_SymbolTable.put(fullyQualifiedName, node);
        return existingSymbol == null;
    }
    
    public boolean put(String[] scope, ProgramSymbol node)
    {
        return put(join(scope, 0, scope.length), node);
    }

    public boolean contains(String[] scope)
    {
        return contains(join(scope, 0, scope.length));
    }
    
    public void putDerived(String base, String derived)
    {
        m_Derivations.put(derived, base);
    }
    
    public boolean isDerived(String base, String derived)
    {
        while((derived != null) && (derived.equals(base) == false))
        {
            derived = m_Derivations.get(derived);
        }
        
        return derived != null;
    }
    
    public boolean contains(String fullyQualifiedName)
    {
        return m_SymbolTable.containsKey(fullyQualifiedName);
    }
    
    public ProgramSymbol get(String fullyQualifiedName)
    {
        ProgramSymbol retval = m_SymbolTable.get(fullyQualifiedName);
        // System.out.println(retval);
        return retval;
    }
    
    public String getFullName(String[] scope)
    {
        // System.out.println(Arrays.toString(scope));
        
        for(int index = scope.length - 2; index >= 0; --index)
        {
            String name = scope[index] + "__" + scope[scope.length - 1];
            if(m_SymbolTable.get(name) != null)
            {
                return name;
            }
        }
        
        String name = scope[scope.length - 1];
        if(m_SymbolTable.get(name) != null)
        {
            return name;
        }

        return null;
    }
    
    public String makeFullName(String[] scope)
    {
        return join(scope, 0, scope.length);
    }
    
    private String join(String[] tokens, int start, int end)
    {
        StringBuilder builder = new StringBuilder();
        for(int index = start; index < end - 1; ++index)
        {
            builder.append(tokens[index]).append("__");
        }
        builder.append(tokens[end - 1]);
        
        return builder.toString();
    }
    
    public void print()
    {
        String[] out = m_SymbolTable.toString().split(",");
        for(String o : out)
        {
            System.out.println(o);
        }
    }
    
    public void addMemberIndex(String varName, int index)
    {
        m_MemberIndex.put(varName, index);
    }
    
    public Integer getMemberIndex(String varName)
    {
        return m_MemberIndex.get(varName);
    }
}