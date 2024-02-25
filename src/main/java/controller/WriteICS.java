package controller;


import com.alibaba.fastjson2.JSONObject;
import model.Course;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WriteICS {
    private final String PATH = "src/main/resources/";
    public final String FORMAT_UTC = "yyyyMMdd'T'HHmmss'Z'";
    private final String now = new SimpleDateFormat(FORMAT_UTC).format(new Date());
    private String first_week = "20240226"; //第一周周一的日期
    private int informTime = 30;  // 提前 N 分钟提醒
    private String[] weekdays = new String[]{"MO", "TU", "WE", "TH", "FR", "SA", "SU"};
    private String gName;   // 全局课程表名
    private String gColor = "#ff9500";  // 预览时的颜色（可以在 iOS 设备上修改）
    private String aTrigger = "";
    private int maxWeek = 0;

    public WriteICS() {
        try {
            gName = new SimpleDateFormat("yyMM").format(new Date()) + "课程表@" + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeICS(List<Course> list) {
        setAttribute();

        BufferedWriter writer = null;
        JSONObject timeJson = parseJson(PATH + "conf_classTime.json");
        File file = new File(PATH + "conf_classInfo.ics");
        try {
            writer = new BufferedWriter(new FileWriter(file));

            // 写入文件头
            String icalBeginBase = "BEGIN:VCALENDAR\n" +
                    "VERSION:2.0\n" +
                    "X-WR-CALNAME:" + gName + "\n" +
                    "X-APPLE-CALENDAR-COLOR:" + gColor + "\n" +
                    "X-WR-TIMEZONE:Asia/Shanghai\n" +
                    "BEGIN:VTIMEZONE\n" +
                    "TZID:Asia/Shanghai\n" +
                    "X-LIC-LOCATION:Asia/Shanghai\n" +
                    "BEGIN:STANDARD\n" +
                    "TZOFFSETFROM:+0800\n" +
                    "TZOFFSETTO:+0800\n" +
                    "TZNAME:CST\n" +
                    "DTSTART:19700101T000000\n" +
                    "END:STANDARD\n" +
                    "END:VTIMEZONE\n";
            writer.write(icalBeginBase);
            System.out.println("头部信息写入成功！");

            // 写入主体内容
            Date firstWeek;
            try {
                firstWeek = new SimpleDateFormat("yyyyMMdd").parse(first_week);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            int i = 1;
            for (Course course : list) {
                // 计算课程第一次开始的日期，公式：7*(开始周数-1) （//把第一周减掉） + 周几 - 1 （没有周0，等于把周一减掉）
                int deltaTime = 7 * (course.getStartWeek() - 1) + course.getWeekday() - 1;
                if (course.getWeekStatus() == 1 && course.getStartWeek() % 2 == 0) {// 单周
                    // 若单周就不变，双周加7
                    deltaTime += 7;
                } else if (course.getWeekStatus() == 2 && course.getStartWeek() % 2 != 0) {
                    deltaTime += 7;
                }

                // 处理完单双周之后 first_time_obj 就是真正开始的日期
                Calendar firstTime = Calendar.getInstance();
                firstTime.setTime(firstWeek);
                firstTime.add(Calendar.DATE, deltaTime);
                String extraStatus;
                if (course.getWeekStatus() == 0) {
                    extraStatus = "1";
                } else {
                    extraStatus = "2;BYDAY=" + weekdays[course.getWeekday() - 1];  // BYDAY 是周 N，隔周重复需要带上

                }

                // 计算课程第一次开始、结束的时间，后面使用RRule重复即可，格式类似 20200225T120000
                String finalSTimeStr = new SimpleDateFormat("yyyyMMdd").format(firstTime.getTime()) + "T"
                        + timeJson.getJSONObject(String.valueOf(course.getClassTime())).getString("startTime");
                String finalETimeStr = new SimpleDateFormat("yyyyMMdd").format(firstTime.getTime()) + "T"
                        + timeJson.getJSONObject(String.valueOf(course.getClassTime())).getString("endTime");

                int deltaWeek = 7 * (course.getEndWeek() - course.getStartWeek());
                Calendar stopTimeObj = Calendar.getInstance();
                stopTimeObj.setTime(firstTime.getTime());
                stopTimeObj.add(Calendar.DATE, deltaWeek + 1);
                String stopTimeStr = new SimpleDateFormat(FORMAT_UTC).format(stopTimeObj.getTime());

                String teacher = "教师：" + course.getClassTeacher();

                String alarmBase;
                if (!"".equals(aTrigger)) {
                    alarmBase = "BEGIN:VALARM\nACTION:DISPLAY\nDESCRIPTION:This is an event reminder\n" +
                            "TRIGGER:" + aTrigger + "\nX-WR-ALARMUID:" + UUID.randomUUID() + "\nUID:" + UUID.randomUUID() + "\nEND:VALARM\n";
                } else {
                    alarmBase = "";
                }
                String icalBase = "\nBEGIN:VEVENT\n" +
                        "CREATED:" + now + "\nDTSTAMP:" + now + "\nSUMMARY:" + course.getClassName() + "\n" +
                        "DESCRIPTION:" + teacher + "\nLOCATION:" + course.getClassroom() + "\n" +
                        "TZID:Asia/Shanghai\nSEQUENCE:0\nUID:" + UUID.randomUUID() + "\nRRULE:FREQ=WEEKLY;UNTIL=" + stopTimeStr + ";INTERVAL=" + extraStatus + "\n" +
                        "DTSTART;TZID=Asia/Shanghai:" + finalSTimeStr + "\nDTEND;TZID=Asia/Shanghai:" + finalETimeStr + "\n" +
                        "X-APPLE-TRAVEL-ADVISORY-BEHAVIOR:AUTOMATIC\n" + alarmBase + "END:VEVENT\n";

                writer.write(icalBase);

                System.out.println("第" + i + "条课程信息写入成功！");
                i++;

                maxWeek = Math.max(maxWeek, course.getEndWeek());
            }


            // 写入周数指示
            Calendar week = Calendar.getInstance();
            week.setTime(firstWeek); // 当前周第一天
            String beginDate, endDate;
            for (i = 1; i <= maxWeek; i++) {

                beginDate = new SimpleDateFormat("yyyyMMdd").format(week.getTime());
                week.add(Calendar.DATE, 1);
                endDate = new SimpleDateFormat("yyyyMMdd").format(week.getTime());
                week.add(Calendar.DATE, -1);

                // 构造 ical 文件体
                String icalBase = "\nBEGIN:VEVENT\n" +
                        "CREATED:" + now + "\nDTSTAMP:" + now + "\nTZID:Asia/Shanghai\nSEQUENCE:0\n" +
                        "SUMMARY:第 " + i + " 周\nDTSTART;VALUE=DATE:" + beginDate + "\nDTEND;VALUE=DATE:" + endDate + "\nUID:" + UUID.randomUUID() + "\n" +
                        "END:VEVENT\n";
                writer.write(icalBase);

                System.out.println("第" + i + "周写入成功！");

                week.add(Calendar.DATE, 7);
            }


            writer.write("\nEND:VCALENDAR");
            System.out.println("尾部信息写入成功！");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     *  读取Json数据
     * @return 时间块对象
     */
    public JSONObject parseJson(String filePath) {
        String json = readFileToString(filePath);
        return JSONObject.parse(json);
    }

    /**
     * @description 将json文件转换为字符串
     *
     * @author DEAiFISH
     * @date 2024/2/25 14:19
     * @return java.lang.String
     */
    private String readFileToString(String filePath) {
        BufferedReader br = null;

        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filePath))));
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    /**
     * @description 设置提前提醒时间
     *
     * @author DEAiFISH
     * @date 2024/2/25 16:42
     * @return void
     */
    private void setAttribute() {
        System.out.println("请输入第一周周一的日期，格式为 YYYYMMDD，如 20240226：");
        Scanner scanner = new Scanner(System.in);
        first_week = scanner.nextLine();
        System.out.println("请输入提前提醒时间，以分钟计；若不需要提醒请输入 N：");
        String str = scanner.nextLine();
        if (!"n".equals(str.toLowerCase(Locale.US))) {
            informTime = Integer.parseInt(str);
            if (informTime <= 60) {
                aTrigger = "-P0DT0H" + informTime + "M0S";
            } else if (informTime <= 1440) {
                int minutes = informTime % 60;
                int hours = informTime / 60;
                aTrigger = "-P0DT" + hours + "H" + minutes + "M0S";
            } else {
                int minutes = informTime % 60;
                int hours = (informTime / 60) - 24;
                int days = informTime / 1440;
                aTrigger = "-P" + days + "DT" + hours + "H" + minutes + "M0S";
            }
        }
    }
}

