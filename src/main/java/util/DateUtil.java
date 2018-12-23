package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DateUtil {

    private DateUtil(){}

    private static final HashMap<String,Integer> weekdayMap = new HashMap<>();
    //这一堆存的是每周的第几天
    private static final int SUN = 1;
    private static final int MON = 2;
    private static final int THUE = 3;
    private static final int WED = 4;
    private static final int THUR = 5;
    private static final int FRI = 6;
    private static final int SAT = 7;

    static {
        weekdayMap.put("SUN", SUN);
        weekdayMap.put("MON", MON);
        weekdayMap.put("THUE", THUE);
        weekdayMap.put("WED", WED);
        weekdayMap.put("THUR", THUR);
        weekdayMap.put("FRI", FRI);
        weekdayMap.put("SAT", SAT);
    }


    private static final Long ONE_WEEK = 7 * 24 * 3600 * 1000L;
    private static final Long ONE_DAY = 24 * 3600 * 1000L;
    private static final Long ONE_HOUR = 3600 * 1000L;
    private static final Long ONE_MINUTE = 60 * 1000L;
    private static final Long ONE_SECOND = 1000L;

    public static Date getNearestWeekday(Date curr, String weekdayStr, String clock) {
        int weekday = weekdayMap.get(weekdayStr);
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

    public static String calDelayWords(long delay) {
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
        return sb.toString();
    }
}
