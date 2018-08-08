package glasses.hadutech.com.engineerapp;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void date(){
        Date newDate = DateUtils.addDays(new Date(), 1);
        System.out.println(DateFormatUtils.format(newDate, "yyyy-MM-dd") + " 00:00:00");
    }

    @Test
    public void t1() {
        System.out.println((false || true));
    }
}