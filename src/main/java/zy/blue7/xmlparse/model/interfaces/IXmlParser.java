package zy.blue7.xmlparse.model.interfaces;

import org.dom4j.Document;

import java.util.Set;

/**
 * @author blue7
 * @date 2020/8/4 18:05
 **/
public interface IXmlParser {
    String parse(String xmlFilePath) throws Exception;
    String parse(Document document) throws Exception;
    Set<String> parse(Document document, String tableName, String parentPath) throws Exception;

}
