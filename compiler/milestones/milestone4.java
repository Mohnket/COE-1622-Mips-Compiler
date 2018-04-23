class Test {

    public static void main(String[] args) {

        System.out.println(new Test2().Start(0));

    }

}

class Test2 {

    public int Start(int y) {
        int x;
        int z;
        boolean b;
        
        b = true;
        b = !b;
        
        x = 5;
        x = x * x;
        z = 12;
        z = z - x;
        y = y + 1;
        y = y - 4;

        return y + x + z;
    }

}