package com.daojia.yonhu.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class Task {
    private long executeTime;
    private List<Job> jobs = new ArrayList<>();

    public Task(long next) {
        this.executeTime = next;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void addJob(Job job) {
        jobs.add(job);
    }

    public void setExecuteTime(long time) {
        this.executeTime = time;
    }

    public void run(ExecutorService service) {
        for (Job job : jobs) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        job.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }
}
