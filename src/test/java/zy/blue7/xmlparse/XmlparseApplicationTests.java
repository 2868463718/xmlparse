package zy.blue7.xmlparse;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import zy.blue7.xmlparse.model.helpers.MyXmlParser;
import zy.blue7.xmlparse.model.helpers.MyXmlParserToYml;

import java.io.File;

@SpringBootTest
class XmlparseApplicationTests {
    @Autowired
    private MyXmlParser myXmlParser;
    @Autowired
    private MyXmlParserToYml myXmlParserToYml;



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

    @Test
    void testParseToYml() throws Exception {
        String str=myXmlParserToYml.parse("./src/ConsumerBestRecord_OperationConfig.xml");

//        FileUtils.writeStringToFile(new File("C:\\Users\\DELL\\Desktop\\cbrRefactor\\xmlparse\\src\\main\\resources\\ConsumerBestRecord_OperationConfig.properties"),str,"utf8");
//        FileUtils.writeStringToFile(new File("C:\\Users\\DELL\\Desktop/a.text"),str,"utf8");
//        C:\Users\DELL\Desktop\cbrRefactor\common-cbr\src\main\resources\ConsumerBestRecord_OperationConfig.properties
        FileUtils.writeStringToFile(new File("C:\\Users\\DELL\\Desktop\\cbrRefactor\\common-cbr\\src\\main\\resources\\ConsumerBestRecord_OperationConfig.yml"),str,"utf8");

    }

}
