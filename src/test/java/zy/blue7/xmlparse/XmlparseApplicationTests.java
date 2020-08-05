package zy.blue7.xmlparse;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zy.blue7.xmlparse.model.helpers.MyXmlParser;

import java.io.File;

@SpringBootTest
class XmlparseApplicationTests {
    @Autowired
    private MyXmlParser myXmlParser;

    @Test
    void contextLoads() {
    }

    @Test
    void testParse() throws Exception {
        String str=myXmlParser.parse("./src/ConsumerBestRecord_OperationConfig.xml");

//        FileUtils.writeStringToFile(new File("C:\\Users\\DELL\\Desktop\\cbrRefactor\\xmlparse\\src\\main\\resources\\ConsumerBestRecord_OperationConfig.properties"),str,"utf8");
//        FileUtils.writeStringToFile(new File("C:\\Users\\DELL\\Desktop/a.text"),str,"utf8");
//        C:\Users\DELL\Desktop\cbrRefactor\common-cbr\src\main\resources\ConsumerBestRecord_OperationConfig.properties
        FileUtils.writeStringToFile(new File("C:\\Users\\DELL\\Desktop\\cbrRefactor\\common-cbr\\src\\main\\resources\\ConsumerBestRecord_OperationConfig.properties"),str,"utf8");

    }

}
