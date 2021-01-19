import lk.ijse.dep.orm.ORMUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author : Damika Anupama Nanayakkara <damikaanupama@gmail.com>
 * @since : 19/01/2021
 **/
public class ORMUtilTest {

    private static Properties properties = new Properties();

    @BeforeClass
    public static void beforeClass() throws Exception {
        properties.load(ORMUtilTest.class.getResourceAsStream("/application.properties"));
    }

    @Test
    public void init() {
        ORMUtil.init(properties, Customer.class, Item.class);
    }
}
