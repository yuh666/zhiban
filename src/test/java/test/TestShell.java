package test;

public class TestShell {

    public static void main(String[] args)throws Exception {
        shell("touch /home/lizx/testshell.txt");
    }

    private static void shell(String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        int state = process.waitFor();
        System.out.println(state);
    }
}
