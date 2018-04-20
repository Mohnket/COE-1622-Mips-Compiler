package compiler.syntaxtree;

public abstract class ProgramSymbol
{
    public int m_Row;
    public int m_Column;
    
    public ProgramSymbol(int row, int column)
    {
        m_Row = row;
        m_Column = column;
    }
}