package compiler.syntaxtree;

public abstract class Node
{
    public int m_Row;
    public int m_Column;
    
    public Node(int row, int column)
    {
        m_Row = row;
        m_Column = column;
    }
}