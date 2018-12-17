package com.daojia.yonhu;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yuh
 * @date 2018-12-14 14:20
 **/
public class Main {

    private static final int SUN = 1;
    private static final int MON = 2;
    private static final int THUE = 3;
    private static final int WED = 4;
    private static final int THUR = 5;
    private static final int FRI = 6;
    private static final int SAT = 7;

    private static String WEBHOOK_URL = "https://oapi.dingtalk.com/robot/send";


    private static List<ScheduledExecutorService> executorServices = new ArrayList<>();
    private static ScheduledExecutorService main = Executors.newSingleThreadScheduledExecutor();
    private static List<String> plans = new ArrayList<>();
    private static List<String> tokens = new ArrayList<>();
    private static JSONArray jsa = new JSONArray();
    private static Map<String, Integer> map = new HashMap<>();

    static {
        map.put("SUN", SUN);
        map.put("MON", MON);
        map.put("THUE", THUE);
        map.put("WED", WED);
        map.put("THUR", THUR);
        map.put("FRI", FRI);
        map.put("SAT", SAT);
    }

    public static void main(String[] args) throws ParseException {

        main.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                reExecute();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }


    private static void reExecute() {
        try {
            String all = read();
            JSONObject jb = JSONObject.parseObject(all);
            JSONArray times = jb.getJSONArray("times");
            if (jsa.equals(times)) {
                return;
            }
            jsa = times;

            if (!executorServices.isEmpty()) {
                for (ScheduledExecutorService executorService : executorServices) {
                    executorService.shutdown();
                }
            }
            executorServices.clear();
            for (Object obj : times) {
                String time = (String) obj;
                String[] split = time.split(",");
                System.out.println(time);
                initTask(map.get(split[0]), split[1], Executors.newSingleThreadScheduledExecutor());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void initTask(int weekday, String clock, ScheduledExecutorService scheduledExecutorService) {
        Date nextWeekday = getNearestWeekday(new Date(), weekday, clock);
        long finalTime = nextWeekday.getTime();
        long delay = finalTime - System.currentTimeMillis();
        if (delay < 0) {
            delay += 7 * 24 * 3600 * 1000;
        }
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }, delay, 7 * 24 * 3600 * 1000, TimeUnit.MILLISECONDS);
        executorServices.add(scheduledExecutorService);
    }


    private static void doWork() {
        try {
            refresh();
            sendMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMsg() throws IOException {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd EEEE");
        int week = getWeek(date);
        StringBuilder sb = new StringBuilder();
        sb.append("值班信息");
        sb.append("\n\n");
        sb.append("今天是: ").append(sdf.format(date));
        sb.append("\n");
        sb.append("本周值班同学是: ").append(plans.get(week % plans.size()));


        HttpClient httpclient = HttpClients.createDefault();
        for (String token : tokens) {
            for (int i = 0; i < 2; i++) {
                HttpPost httppost = new HttpPost(WEBHOOK_URL + "?access_token=" + token);
                httppost.addHeader("Content-Type", "application/json; charset=utf-8");
                String msg = "{\"msgtype\":\"text\",\"text\":{\"content\":\"" + sb.toString() + "\"},\"at\":{ \"atMobiles\":\"\",\"isAtAll\":true}}";
                StringEntity se = new StringEntity(msg, "utf-8");
                httppost.setEntity(se);
                HttpResponse response = httpclient.execute(httppost);
                if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
                    String result= EntityUtils.toString(response.getEntity(), "utf-8");
                    System.out.println(result);
                }
            }
        }
    }

    private static Date getNearestWeekday(Date curr, int weekday, String clock) {
        String[] split = clock.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curr);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        if (day != weekday) {
            calendar.add(Calendar.DAY_OF_MONTH, day < weekday ? weekday : 7 + weekday - day);
        }
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(split[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private static int getWeek(Date curr) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curr);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private static void refresh() throws IOException {
        String all = read();
        JSONObject jb = JSONObject.parseObject(all);
        JSONArray plansJA = jb.getJSONArray("plans");
        for (Object plan : plansJA) {
            plans.add((String) plan);
        }
        JSONArray tokensJA = jb.getJSONArray("tokens");
        for (Object token : tokensJA) {
            tokens.add((String) token);
        }

    }

    private static String read() throws IOException {
        InputStream stream = new FileInputStream("/opt/config/config.json");
        byte[] bytes = new byte[1024 * 100];
        int read = stream.read(bytes);
        stream.close();
        return new String(bytes, 0, read);
    }

}
