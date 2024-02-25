import controller.ReadExcel;
import controller.WriteICS;
import model.Course;

import java.sql.Wrapper;
import java.util.List;

/**
 * @description TODO
 *
 * @author DEAiFISH
 * @date 2024/2/25 18:58
 */
public class Main {
    public static void main(String[] args) {
        List<Course> courses = new ReadExcel().readExcel("src/main/resources/classInfo.xlsx");
        new WriteICS().writeICS(courses);
    }
}
