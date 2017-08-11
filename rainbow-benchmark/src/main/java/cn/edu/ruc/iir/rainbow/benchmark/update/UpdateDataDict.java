package cn.edu.ruc.iir.rainbow.benchmark.update;

import cn.edu.ruc.iir.rainbow.benchmark.common.SysSettings;

import java.io.*;

/**
 * @version V1.0
 * @Package: cn.edu.ruc.iir.rainbow.benchmark.update
 * @ClassName: UpdateDataDict
 * @Description: To update data_dict
 * @author: Tao
 * @date: Create in 2017-08-11 9:22
 **/
public class UpdateDataDict {


    private static UpdateDataDict instance = new UpdateDataDict();

    private UpdateDataDict() {
    }

    public static UpdateDataDict getInstance() {
        return instance;
    }

    public void getColumnMaps(String columnMapPath, String dictPath, String newDictPath) {
        String curLine;
        BufferedReader br = null;
        String splitLine[];
        try {
            br = new BufferedReader(new FileReader(columnMapPath));
            while ((curLine = br.readLine()) != null) {
                splitLine = curLine.split(",");
                upDataDictByColumnName(dictPath, newDictPath, splitLine[0], splitLine[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
    * @ClassName: UpdateDataDict
    * @Title: upDataDictByColumnName
    * @Description: upDataDictByColumnName
    * @param: dictPath -> origin path, newDictPath -> new dict path, newColumnName -> column_*
    * @author: Tao
    * @date: 9:44 2017/8/11
    */
    private void upDataDictByColumnName(String dictPath, String newDictPath, String columnName, String newColumnName) {
        String curLine;
        BufferedReader br = null;
        String splitLine[];
        try {
            String fileName = dictPath + "\\" + columnName + ".txt";
            String outDictPath = newDictPath + "\\" + newColumnName + ".txt";
            File f = new File(fileName);
            if (f.exists()) {
                br = new BufferedReader(new FileReader(fileName));
                StringBuilder sb = new StringBuilder();
                while ((curLine = br.readLine()) != null) {
                    splitLine = curLine.split("\t");
                    sb.append(newColumnName + "\t").append(splitLine[1] + "\t").append(splitLine[2] + "\n");
                }
                writeDict(sb, outDictPath);
            } else {
                System.out.println(fileName + "is not exist.");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void writeDict(StringBuilder sb, String outDictPath) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(outDictPath), SysSettings.BUFFER_SIZE);
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
