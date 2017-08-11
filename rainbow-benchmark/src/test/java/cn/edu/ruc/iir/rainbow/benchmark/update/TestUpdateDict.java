package cn.edu.ruc.iir.rainbow.benchmark.update;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

/**
 * @version V1.0
 * @Package: cn.edu.ruc.iir.rainbow.benchmark.update
 * @ClassName: TestUpdateDict
 * @Description: Test
 * @author: Tao
 * @date: Create in 2017-08-04 9:23
 **/
public class TestUpdateDict {

    UpdateDict u = UpdateDict.getInstance();
    Random r = new Random();

    @Test
    public void TestRandom() {
        String randStr = UUID.randomUUID().toString();
        System.out.println(randStr);
        System.out.println(randStr.length());
    }

    @Test
    public void TestGetEqualLengthContent() {
        String s = UUID.randomUUID().toString() + "qwert";
        String str = u.getEqualLengthContent(s);
        System.out.println(str.length());
    }

    @Test
    public void TestGetColumns() {
        long startTime = System.currentTimeMillis();
        u.getColumns();
        long endTime = System.currentTimeMillis();
        System.out.println("dataSize*200M run time : ： " + (endTime - startTime) / 1000 + "s");
    }

}
