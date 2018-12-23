package com.daojia.yonhu.task;


public class ShellJob implements Job {

    private String cmd;

    public ShellJob(String cmd) {
        this.cmd = cmd;
    }

    public ShellJob() {
    }

    @Override
    public void run() throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        int state = process.waitFor();
        System.out.println(state);
    }
}
