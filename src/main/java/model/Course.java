package model;

public class Course {
    private String className;//课程名称
    private int startWeek;//开始周数
    private int endWeek;//结束周数
    private int weekday;//课程日期（周几）
    private int classTime;//conf_classTime.json 中定义的时间段代号
    private String classroom;//教室
    private int weekStatus;//是否单双周排课：正常排课 = 0，单周排课 = 1，双周排课 = 2
    private String classTeacher;//可选，教师名

    public Course() {
    }

    public Course(String className, int startWeek, int endWeek, int weekday, int classTime, String classroom, int weekStatus, String classTeacher) {
        this.className = className;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
        this.weekday = weekday;
        this.classTime = classTime;
        this.classroom = classroom;
        this.weekStatus = weekStatus;
        this.classTeacher = classTeacher;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(int startWeek) {
        this.startWeek = startWeek;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(int endWeek) {
        this.endWeek = endWeek;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public int getClassTime() {
        return classTime;
    }

    public void setClassTime(int classTime) {
        this.classTime = classTime;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public int getWeekStatus() {
        return weekStatus;
    }

    public void setWeekStatus(int weekStatus) {
        this.weekStatus = weekStatus;
    }

    public String getClassTeacher() {
        return classTeacher;
    }

    public void setClassTeacher(String classTeacher) {
        this.classTeacher = classTeacher;
    }

    @Override
    public String toString() {
        return "  {\n" +
                "    \"className\":\"" + className + "\",\n" +
                "    \"startWeek\":" + startWeek + ",\n" +
                "    \"endWeek\":" + endWeek + ",\n" +
                "    \"weekday\":" + weekday + ",\n" +
                "    \"classTime\":" + classTime + ",\n" +
                "    \"classroom\":\"" + classroom + "\",\n" +
                "    \"weekStatus\":" + weekStatus + ",\n" +
                "    \"classTeacher\":\"" + classTeacher + "\"\n" +
                "  }";
    }
}
