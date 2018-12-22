import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class ZhiBanTask {

    private static String configPath = "/opt/zhiban/config/zhiban.properties";
    private static String WEBHOOK_URL = "https://oapi.dingtalk.com/robot/send?access_token=aaf8b34eb97350d8f423ab70a2177b4262df56949ebc9a7de5a12d517f14dd90";
    private static String template = "值班信息\n\n今天是: %s\n本周值班同学是: %s";
    private static HttpClient httpclient = HttpClients.createDefault();

    public void run() {
        String nowDateStr = getNowDateStr();
        String students = getStudents();

        String msg = String.format(template, nowDateStr, students);

        send(WEBHOOK_URL, msg);
    }

    private String getStudents() {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(configPath)));
        String studentsValue = properties.getProperty("students");
        List<String> students = JSONObject.parseArray(studentsValue, String.class);
        int index = getWeek(new Date()) % students.size();
        return students.get(index);
    }

    private static int getWeek(Date curr) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curr);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private String getNowDateStr() {
        return new SimpleDateFormat("yyyy-MM-dd EEEE").format(new Date());
    }

    private void send(String url, String msg) {
        HttpPost httpPost = assemblyHttpPost(url, msg);
        HttpResponse response = httpclient.execute(httppost);
        String result = EntityUtils.toString(response.getEntity(), "utf-8");
        System.out.println(result);
    }

    private HttpPost assemblyHttpPost(String url, String msg) {
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        httppost.setEntity(new StringEntity(msg, "utf-8"));
        return httppost;
    }

}
