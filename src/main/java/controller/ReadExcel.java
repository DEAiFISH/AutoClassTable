package controller;

import model.Course;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Dreamer-1
 * Date: 2019-03-01
 * Time: 10:21
 * Description: 读取Excel内容
 */
public class ReadExcel {
    private int className;//课程名称
    private int startWeek;//开始周数
    private int endWeek;//结束周数
    private int weekday;//课程日期（周几）
    private int classTime;//conf_classTime.json 中定义的时间段代号
    private int classroom;//教室
    private int weekStatus;//是否单双周排课：正常排课 = 0，单周排课 = 1，双周排课 = 2
    private int classTeacher;//可选，教师名

    private static Logger logger = Logger.getLogger(ReadExcel.class.getName()); // 日志打印类

    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";

    /**
     * 根据文件后缀名类型获取对应的工作簿对象
     *
     * @param inputStream 读取文件的输入流
     * @param fileType    文件后缀名类型（xls或xlsx）
     * @return 包含文件数据的工作簿对象
     * @throws IOException
     */
    private Workbook getWorkbook(InputStream inputStream, String fileType) throws IOException {
        Workbook workbook = null;
        if (fileType.equalsIgnoreCase(XLS)) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (fileType.equalsIgnoreCase(XLSX)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        return workbook;
    }

    /**
     * 读取Excel文件内容
     *
     * @param fileName 要读取的Excel文件所在路径
     * @return 读取结果列表，读取失败时返回null
     */
    public List<Course> readExcel(String filePath) {

        Workbook workbook = null;
        FileInputStream inputStream = null;

        try {
            // 获取Excel后缀名
            String fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
            // 获取Excel文件
            File excelFile = new File(filePath);
            if (!excelFile.exists()) {
                logger.warning("指定的Excel文件不存在！");
                return null;
            }

            // 获取Excel工作簿
            inputStream = new FileInputStream(excelFile);
            workbook = getWorkbook(inputStream, fileType);

            // 读取excel中的数据
            List<Course> resultDataList = parseExcel(workbook);

            return resultDataList;
        } catch (Exception e) {
            logger.warning("解析Excel失败，文件名：" + filePath + " 错误信息：" + e.getMessage());
            return null;
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                logger.warning("关闭数据流出错！错误信息：" + e.getMessage());
                return null;
            }
        }
    }

    /**
     * 解析Excel数据
     *
     * @param workbook Excel工作簿对象
     * @return 解析结果
     */
    private List<Course> parseExcel(Workbook workbook) {
        List<Course> resultDataList = new ArrayList<>();
        // 解析sheet
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            Sheet sheet = workbook.getSheetAt(sheetNum);

            // 校验sheet是否合法
            if (sheet == null) {
                continue;
            }

            // 获取第一行数据
            int firstRowNum = sheet.getFirstRowNum();
            Row firstRow = sheet.getRow(firstRowNum);
            if (null == firstRow) {
                logger.warning("解析Excel失败，在第一行没有读取到任何数据！");
            }else {
                formatName(firstRow);
            }

            // 解析每一行的数据，构造数据对象
            int rowStart = firstRowNum + 1;
            int rowEnd = sheet.getPhysicalNumberOfRows();
            for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
                Row row = sheet.getRow(rowNum);

                if (null == row) {
                    continue;
                }

                Course resultData = convertRowToData(row);
                if (null == resultData) {
                    logger.warning("第 " + row.getRowNum() + "行数据不合法，已忽略！");
                    continue;
                }
                resultDataList.add(resultData);
            }
        }

        return resultDataList;
    }

    /**
     * 将单元格内容转换为字符串
     *
     * @param cell
     * @return
     */
    private String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case NUMERIC:   //数字
                Double doubleValue = cell.getNumericCellValue();

                // 格式化科学计数法，取一位整数
                DecimalFormat df = new DecimalFormat("0");
                returnValue = df.format(doubleValue);
                break;
            case STRING:    //字符串
                returnValue = cell.getStringCellValue();
                break;
            case BOOLEAN:   //布尔
                Boolean booleanValue = cell.getBooleanCellValue();
                returnValue = booleanValue.toString();
                break;
            case BLANK:     // 空值
                break;
            case FORMULA:   // 公式
                returnValue = cell.getCellFormula();
                break;
            case ERROR:     // 故障
                break;
            default:
                break;
        }
        return returnValue;
    }

    /**
     * 格式化列名称
     * @param row 表格第一行
     */
    private void formatName(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            switch (convertCellValueToString(row.getCell(i))) {
                case "className":
                    className = i;
                    break;
                case "startWeek":
                    startWeek = i;
                    break;
                case "endWeek":
                    endWeek = i;
                    break;
                case "weekday":
                    weekday = i;
                    break;
                case "classTime":
                    classTime = i;
                    break;
                case "classroom":
                    classroom = i;
                    break;
                case "weekStatus":
                    weekStatus = i;
                    break;
                case "classTeacher":
                    classTeacher = i;
                    break;
                default:
            }
        }
    }

    /**
     * 提取每一行中需要的数据，构造成为一个结果数据对象
     * <p>
     * 当该行中有单元格的数据为空或不合法时，忽略该行的数据
     *
     * @param row 行数据
     * @return 解析后的行数据对象，行数据错误时返回null
     */
    private Course convertRowToData(Row row) {
        Course resultData = new Course();

        Cell cell;
        // 获取课程名称
        cell = row.getCell(className);
        resultData.setClassName(convertCellValueToString(cell));
        // 获取开始周
        cell = row.getCell(startWeek);
        int s = Integer.parseInt(convertCellValueToString(cell));
        resultData.setStartWeek(s);
        // 获取结束周
        cell = row.getCell(endWeek);
        resultData.setEndWeek(Integer.parseInt(convertCellValueToString(cell)));
        // 获取星期
        cell = row.getCell(weekday);
        resultData.setWeekday(Integer.parseInt(convertCellValueToString(cell)));
        // 获取上课时间
        cell = row.getCell(classTime);
        resultData.setClassTime(Integer.parseInt(convertCellValueToString(cell)));
        // 获取教室
        cell = row.getCell(classroom);
        resultData.setClassroom(convertCellValueToString(cell));
        // 获取单双周
        cell = row.getCell(weekStatus);
        resultData.setWeekStatus(Integer.parseInt(convertCellValueToString(cell)));
        // 获取教室
        cell = row.getCell(classTeacher);
        resultData.setClassTeacher(convertCellValueToString(cell));

        return resultData;
    }
}
