package cn.sdaict.fldatauploader.utils;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvFileUtil {

    public static File createCSVFile(List<Object> head, List<LinkedHashMap<String, Object>> dataList, String outPutPath, String filename) {
        File csvFile = null;
        BufferedWriter csvWtriter = null;
        try {
            csvFile = new File(outPutPath + File.separator + filename + ".csv");
            File parent = csvFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            csvFile.createNewFile();
            // GB2312使正确读取分隔符","
            csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 1024);
            // 写入文件头部
            writeHead(head, csvWtriter);
            // 写入文件内容
            for (LinkedHashMap<String, Object> row : dataList) {
                writeRow(row, csvWtriter);
            }
            csvWtriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvWtriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvFile;
    }

    public static File addCSVFile(List<LinkedHashMap<String, Object>> dataList, File csvFile) {
        BufferedWriter csvWtriter = null;
        try {
            csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile, true), "UTF-8"), 1024);
            // 写入文件内容
            for (LinkedHashMap<String, Object> row : dataList) {
                writeRow(row, csvWtriter);
            }
            csvWtriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvWtriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvFile;
    }

    private static void writeHead(List<Object> row, BufferedWriter csvWriter) throws IOException {
//         写入文件内容
        StringBuffer sb = new StringBuffer();
        for (Object data : row) {
            sb.append(data).append(",").toString();
        }
        csvWriter.write(sb.substring(0, sb.length() - 1));
        csvWriter.newLine();
    }

    /**
     * 写一行数据方法
     *
     * @param row
     * @param csvWriter
     * @throws IOException
     */
    private static void writeRow(LinkedHashMap<String, Object> row, BufferedWriter csvWriter) throws IOException {
//         写入文件内容
        Iterator iterator = row.entrySet().iterator();
        StringBuffer sb = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (!"id".equals(entry.getKey()) && !"job_id".equals(entry.getKey()) && !"path".equals(entry.getKey()) && !"path_small".equals(entry.getKey())) {
                sb.append(entry.getValue()).append(",").toString();
            }
        }
        csvWriter.write(sb.substring(0, sb.length() - 1));
        csvWriter.newLine();
    }


//    @SuppressWarnings("static-access")
//    public static void main(String[] args) {
//        List<Object> exportData = new ArrayList<Object>();
//        exportData.add("第一列");
//        exportData.add("第二列");
//        exportData.add("第三列");
//        List<List<Object>> datalist = new ArrayList<List<Object>>();
//        List<Object> data = new ArrayList<Object>();
//        data.add("111");
//        data.add("222");
//        data.add("333");
//        List<Object> data1 = new ArrayList<Object>();
//        data1.add("444");
//        data1.add("555");
//        data1.add("666");
//        datalist.add(data);
//        datalist.add(data1);
//        String path = "/Users/duchenghao/Desktop";
//        String fileName = "文件导出";
//        File file = createCSVFile(exportData, datalist, path, fileName);
//        String fileName2 = file.getName();
//        System.out.println("文件名称：" + fileName2);
//    }

}
