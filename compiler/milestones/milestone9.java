class Test
{
    public static void main(String[] args)
    {
        System.out.println(new C().test());
    }
}

class C extends B
{
    int c;
    
    public int test()
    {
        c = 4;
        return c + (this.bTest());
    }
}

class B extends A
{
    int b;
    
    public int bTest()
    {
        b = 3;
        return b + (this.aTest());
    }
}

class A
{
    int a;
    
    public int aTest()
    {
        a = 2;
        return a;
    }
}