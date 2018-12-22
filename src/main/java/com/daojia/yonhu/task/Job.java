package com.daojia.yonhu.task;

/**
 * @author yuh
 * @date 2018-12-22 16:06
 **/
public class Job {

    private long next;
    private Object obj;

    public long getNext() {
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Job(long next, Object obj) {
        this.next = next;
        this.obj = obj;
    }

    public Job() {
    }
}
