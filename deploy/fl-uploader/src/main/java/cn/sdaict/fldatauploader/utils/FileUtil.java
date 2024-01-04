package cn.sdaict.fldatauploader.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @author: zhaoxh
 * @date: 2021年11月25日 17:03
 */
public class FileUtil {
    public static void getFiles(File file){
        List<File> fileList=new ArrayList<>();
        File[] fs = file.listFiles();
        for(File f:fs){
            if(f.isDirectory())	//若是目录，则递归打印该目录下的文件
                getFiles(f);
            if(f.isFile())		//若是文件，直接打印
                System.out.println(f);
        }
    }
}
