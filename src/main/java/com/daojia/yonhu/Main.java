package com.daojia.yonhu;


import com.daojia.yonhu.task.ShellJob;
import com.daojia.yonhu.task.Task;
import util.DateUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {


    private static ScheduledExecutorService main = Executors.newSingleThreadScheduledExecutor();
    private static ExecutorService taskPopExecutor = Executors.newSingleThreadExecutor();
    private static ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
    private static PriorityQueue<Task> priorityQueue = new PriorityQueue<Task>(new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            return (int) (o1.getExecuteTime() - o2.getExecuteTime());
        }
    });
    private static final Long ONE_WEEK = 7 * 24 * 3600 * 1000L;
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();
    private static List<String> cronList = new ArrayList<>();


    // 123
    public static void main(String[] args) throws ParseException, InterruptedException {

        taskPopExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    initTask();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread.sleep(500);
        main.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                reExecute();
            }
        }, 0, 10, TimeUnit.SECONDS);


    }


    private static void reExecute() {
        try {
            lock.lock();
            List<String> lines = readLines();
            Collections.sort(lines);
            if (cronList.equals(lines)) {
                return;
            }
            cronList = lines;
            priorityQueue.clear();
            HashMap<Long, Task> map = new HashMap<>();
            for (String line : lines) {
                System.out.println(line);
                String[] timeAndCmd = line.split("#");
                String time = timeAndCmd[0];
                String cmd = timeAndCmd[1];

                String[] weekAndClock = time.split(",");
                Date nextWeekday = DateUtil.getNearestWeekday(new Date(), weekAndClock[0], weekAndClock[1]);
                long finalTime = nextWeekday.getTime();
                long l = System.currentTimeMillis();
                finalTime = finalTime > l ? finalTime : finalTime + ONE_WEEK;
                Task task = map.get(finalTime);
                if (task == null) {
                    task = new Task(finalTime);
                    map.put(finalTime,task);
                }
                task.addJob(new ShellJob(cmd));
            }
            if (!map.isEmpty()) {
                Collection<Task> values = map.values();
                for (Task value : values) {
                    priorityQueue.add(value);
                }
            }
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    private static void initTask() throws InterruptedException {
        try {
            while (true) {
                lock.lock();
                if (!priorityQueue.isEmpty()) {
                    Task peek = priorityQueue.peek();
                    if (peek.getExecuteTime() <= System.currentTimeMillis()) {
                        Task poll = priorityQueue.poll();
                        poll.setExecuteTime(poll.getExecuteTime() + ONE_WEEK);
                        priorityQueue.add(poll);
                        peek = priorityQueue.peek();
                        poll.run(taskExecutor);
                    }
                    //+10 以防出现负值
                    long delay = peek.getExecuteTime() - System.currentTimeMillis() + 10;
                    System.out.println(DateUtil.calDelayWords(delay));
                    condition.await(delay, TimeUnit.MILLISECONDS);
                } else {
                    //空的玩个屁
                    condition.await();
                }
            }
        } finally {
            lock.unlock();
        }

    }

    //return ["FRI,21:00 java -jar xxxx.jar",...]
    private static List<String> readLines() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/opt/zhiban/config/cron.config")));
        String line = null;
        List<String> list = new ArrayList();
        while ((line = reader.readLine()) != null) {
            list.add(line);
        }
        reader.close();
        return list;
    }

}
