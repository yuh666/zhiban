package com.daojia.yonhu.task;

/**
 * @author yuh
 * @date 2018-12-22 16:06
 **/
public class Job {

    private long next;
    private String shell;

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(String shell) {
        this.shell = shell;
    }

    public Job(long next, String shell) {
        this.next = next;
        this.shell = shell;
    }

    public Job() {
    }
}
