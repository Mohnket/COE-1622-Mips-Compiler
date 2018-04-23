class Test
{
    public static void main(String[] args)
    {
        {
            System.out.println(new C().test());
            System.out.println(new C().anotherTest());
            System.out.println(new Array().copy());
        }
    }
}

class C extends B
{
    int c;
    B Ayy;
    
    public int test()
    {
        c = 4;
        return c + (this.bTest());
    }
    
    public int anotherTest()
    {
        Ayy = new B();
        return Ayy.bTest();
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

class Array
{
    int[] array1;
    
    public int copy()
    {
        int[] array2;
        int index;
        int temp;
        
        array1 = new int[5];
        array2 = new int[5];
        
        array1[0] = 1;
        array1[1] = 1;
        array1[2] = 1;
        array1[3] = 1;
        array1[4] = 1;
        array2[0] = 1;
        
        index = 0;
        while(index < (array2.length))
        {
            temp = array1[index];
            array2[index] = temp;
            index = index + 1;
        }
        
        temp = array2[4];
        return temp;
    }
}