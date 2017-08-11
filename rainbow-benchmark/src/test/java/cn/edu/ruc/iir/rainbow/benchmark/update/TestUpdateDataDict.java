package cn.edu.ruc.iir.rainbow.benchmark.update;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

/**
 * @version V1.0
 * @Package: cn.edu.ruc.iir.rainbow.benchmark.update
 * @ClassName: TestUpdateDataDict
 * @Description: To update data_dict for the columnName
 * @author: Tao
 * @date: Create in 2017-08-11 9:26
 **/
public class TestUpdateDataDict {
    UpdateDataDict u = UpdateDataDict.getInstance();

    @Test
    public void TestGetColumnMaps() {
        String columnMapPath = "G:\\DBIIR\\rainbow\\rainbow-benchmark\\src\\main\\resources\\data\\column_mapping.csv";
        String dictPath = "G:\\DBIIR\\new_dict";
        String newDictPath = "G:\\DBIIR\\rainbow\\rainbow-benchmark\\src\\main\\resources\\dict";
        long startTime = System.currentTimeMillis();
        u.getColumnMaps(columnMapPath, dictPath, newDictPath);
        long endTime = System.currentTimeMillis();
        System.out.println("dataSize*200M run time : ： " + (endTime - startTime) / 1000 + "s");
    }


}
