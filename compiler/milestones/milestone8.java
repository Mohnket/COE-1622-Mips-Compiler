class Test {

    public static void main(String[] args) {
        {
            System.out.println(new Test2().factorial(5));
        }
    }
}

class Test2 {

    public int factorial(int y) {
        int x;
        int z;
        
        z = 2;
        
        if(y < 2)
        {
            x = 1;
        }
        else
        {
            x = this.factorial(y - 1);
        }
        
        z = this.test(y);
        System.out.println(100);
        
        return x * y;
    }
    
    public int test(int x)
    {
        while(0 < x)
        {
            System.out.println(x);
            x = x - 1;
        }
        
        return 100;
    }
}