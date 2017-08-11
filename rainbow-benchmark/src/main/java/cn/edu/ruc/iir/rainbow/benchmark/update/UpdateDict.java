package cn.edu.ruc.iir.rainbow.benchmark.update;

import cn.edu.ruc.iir.rainbow.benchmark.common.SysSettings;

import java.io.*;
import java.util.Random;
import java.util.UUID;

/**
 * @version V1.0
 * @Package: cn.edu.ruc.iir.rainbow.benchmark.update
 * @ClassName: UpdateDict
 * @Description: To update the info of data_dict
 * @author: Tao
 * @date: Create in 2017-08-03 21:15
 **/
public class UpdateDict {


    public static int MAXN = 40000;
    public static int UUID_Len = 36;
    public String data_schema = "G:\\DBIIR\\data\\schema.txt";
    public String data_dict = "G:\\DBIIR\\data_dict";
    public String new_dict = "G:\\DBIIR\\new_dict";


    private Random random = new Random();


    private static UpdateDict instance = new UpdateDict();

    private UpdateDict() {
    }

    public static UpdateDict getInstance() {
        return instance;
    }

    public void getColumns() {
        String curLine;
        BufferedReader br = null;
        String splitLine[];
        try {
            br = new BufferedReader(new FileReader(data_schema));
            while ((curLine = br.readLine()) != null) {
                splitLine = curLine.split("\t");
                if (splitLine[1].equals("string")) {
                    upDictByColumnName(splitLine[0]);
                } else {
                    // int & boolean
                    addRowByColumnName(splitLine[0]);
                }
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

    private void addRowByColumnName(String columnName) {
        String curLine;
        BufferedReader br = null;
        String splitLine[];
        try {
            String fileName = data_dict + "\\" + columnName + ".txt";
            String outFileName = new_dict + "\\" + columnName + ".txt";
            File f = new File(fileName);
            if (f.exists()) {
                br = new BufferedReader(new FileReader(fileName));
                StringBuilder sb = new StringBuilder();
                int num = 0;
                while ((curLine = br.readLine()) != null) {
                    splitLine = curLine.split("\t");
                    if (splitLine[2].equals("40000")) {
                        // add one row
                        num = getRandomByS();
                        sb.append(columnName + "\t").append(splitLine[1] + "\t").append(MAXN - num + "\n");
                        sb.append(columnName + "\t").append(getRandomByK() + "\t").append(num + "\n");
                    } else {
                        sb.append(columnName + "\t").append(splitLine[1] + "\t").append(splitLine[2] + "\n");
                    }
                }
                writeDict(sb, outFileName);
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

    private void upDictByColumnName(String columnName) {
        String curLine;
        BufferedReader br = null;
        String splitLine[];
        try {
            String fileName = data_dict + "\\" + columnName + ".txt";
            String outFileName = new_dict + "\\" + columnName + ".txt";
            File f = new File(fileName);
            if (f.exists()) {
                br = new BufferedReader(new FileReader(fileName));
                StringBuilder sb = new StringBuilder();
                int num = 0;
                String content = null;
                while ((curLine = br.readLine()) != null) {
                    splitLine = curLine.split("\t");
                    if (splitLine[2].equals("40000")) {
                        // add one row
                        if (content == "") {
                            content = getRandomContent();
                        } else {
                            content = getEqualLengthContent(splitLine[1]);
                        }
                        num = getRandomByK();
                        sb.append(columnName + "\t").append(content + "\t").append(MAXN - num + "\n");
                        sb.append(columnName + "\t").append(getRandomContent() + "\t").append(num + "\n");
                    } else {
                        content = getEqualLengthContent(splitLine[1]);
                        sb.append(columnName + "\t").append(content + "\t").append(splitLine[2] + "\n");
                    }
                }
                writeDict(sb, outFileName);

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

    public void writeDict(StringBuilder sb, String outFileName) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(outFileName), SysSettings.BUFFER_SIZE);
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

    public String getEqualLengthContent(String content) {
        int len = content.length();
        int count = len / UUID_Len;
        StringBuilder sb = new StringBuilder();
        String r = UUID.randomUUID().toString();
        for (int i = 0; i < count; i++) {
            sb.append(r);
            r = UUID.randomUUID().toString();
        }
        int leftCount = len % UUID_Len;
        sb.append(r.substring(0, leftCount));
        return sb.toString();
    }

    private String getRandomContent() {
        return UUID.randomUUID().toString();
    }

    private int getRandomByK() {
        return random.nextInt(1000) + 1;
    }

    private int getRandomByS() {
        return random.nextInt(10) + 1;
    }


}
