## 课表导入（Excel --> ICS）

>className - 课程名称
>startWeek - 开始周数
>endWeek - 结束周数
>weekday - 课程日期（周几）
>classTime - conf_classTime.json 中定义的时间段代号
>classroom - 教室
>weekStatus - 是否单双周排课：正常排课 = 0，单周排课 = 1，双周排课 = 2
>classTeacher - 教师名
>
>注意：若课程有不同排课方式或一周有多节课，需要分多条记录录入。



### 命名规则

- 课表excel文件：classInfo.xlsx
- 生成的ics文件：conf_classInfo.ics
- 时间段定义：conf_classTime.json