package com.daojia.yonhu;


import com.daojia.yonhu.clazzload.ClazzLoader;
import com.daojia.yonhu.task.Job;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yuh
 * @date 2018-12-14 14:20
 **/
public class Main {

    //这一堆存的是每周的第几天
    private static final int SUN = 1;
    private static final int MON = 2;
    private static final int THUE = 3;
    private static final int WED = 4;
    private static final int THUR = 5;
    private static final int FRI = 6;
    private static final int SAT = 7;


    private static ScheduledExecutorService main = Executors.newSingleThreadScheduledExecutor();
    private static ExecutorService taskPopExecutor = Executors.newSingleThreadExecutor();
    private static ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
    private static Map<String, Integer> map = new HashMap<>();
    private static PriorityQueue<Job> priorityQueue = new PriorityQueue<>(new Comparator<Job>() {
        @Override
        public int compare(Job o1, Job o2) {
            return (int) (o1.getNext() - o2.getNext());
        }
    });
    private static final Long ONE_WEEK = 7 * 24 * 3600 * 1000L;
    private static final Long ONE_DAY = 24 * 3600 * 1000L;
    private static final Long ONE_HOUR = 3600 * 1000L;
    private static final Long ONE_MINUTE = 60 * 1000L;
    private static final Long ONE_SECOND = 1000L;
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();
    private static List<String> cronList = new ArrayList<>();
    private static final ClazzLoader loader = new ClazzLoader();


    static {
        map.put("SUN", SUN);
        map.put("MON", MON);
        map.put("THUE", THUE);
        map.put("WED", WED);
        map.put("THUR", THUR);
        map.put("FRI", FRI);
        map.put("SAT", SAT);
    }

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
        Thread.sleep(1000);
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

            for (String line : lines) {
                System.out.println(line);
                String[] timeAndCmd = line.split("#");
                String time = timeAndCmd[0];
                String cmd = timeAndCmd[1];

                String[] weekAndClock = time.split(",");
                Date nextWeekday = getNearestWeekday(new Date(), map.get(weekAndClock[0]), weekAndClock[1]);
                long finalTime = nextWeekday.getTime();
                long l = System.currentTimeMillis();
                finalTime = finalTime > l ? finalTime : finalTime + ONE_WEEK;
                priorityQueue.add(new Job(finalTime, cmd));
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
                    Job peek = priorityQueue.peek();
                    if (peek.getNext() <= System.currentTimeMillis()) {
                        Job poll = priorityQueue.poll();
                        poll.setNext(poll.getNext() + ONE_WEEK);
                        priorityQueue.add(poll);
                        peek = priorityQueue.peek();
                        taskExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    doWork(poll.getShell());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                    //+10 以防出现负值
                    long delay = peek.getNext() - System.currentTimeMillis() + 10;
                    System.out.println(calDelayWords(delay, peek.getShell()));
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

    private static void doWork(String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        int state = process.waitFor();
        System.out.println(state);
    }

    private static String calDelayWords(long delay, String className) {
        long temp = delay;
        long day = -1;
        long hour = -1;
        long minute = -1;
        long second = -1;
        if (delay >= ONE_DAY) {
            day = delay / ONE_DAY;
            delay %= ONE_DAY;
        }
        if (delay >= ONE_HOUR) {
            hour = delay / ONE_HOUR;
            delay %= ONE_HOUR;
        }
        if (delay >= ONE_MINUTE) {
            minute = delay / ONE_MINUTE;
            delay %= ONE_MINUTE;
        }
        if (delay >= ONE_SECOND) {
            second = delay / ONE_SECOND;
            delay %= ONE_SECOND;
        }
        long mm = delay;

        StringBuilder sb = new StringBuilder();
        sb.append("下次调度将在");
        if (day != -1) {
            sb.append(day).append("天");
        }
        if (hour != -1) {
            sb.append(hour).append("小时");
        }
        if (minute != -1) {
            sb.append(minute).append("分钟");
        }
        if (second != -1) {
            sb.append(second).append("秒钟");
        }
        sb.append(mm).append("毫秒");
        sb.append("后执行");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE");
        String format = simpleDateFormat.format(new Date(temp + System.currentTimeMillis()));
        sb.append(",").append("也就是").append(format);
        sb.append(" ");
        sb.append(className);
        return sb.toString();
    }


    private static Date getNearestWeekday(Date curr, int weekday, String clock) {
        String[] split = clock.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curr);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        if (day != weekday) {
            calendar.add(Calendar.DAY_OF_YEAR, (day < weekday ? weekday : 7 + weekday) - day);
        }
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(split[1]));
        calendar.set(Calendar.SECOND, Integer.parseInt(split[2]));
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }


    //return ["FRI,21:00 com.daojia.yonghu.zhiban.SendMsg",...]
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
