package zy.blue7.xmlparse.model.helpers;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;
import zy.blue7.xmlparse.model.interfaces.IXmlParser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author blue7
 * @date 2020/8/4 18:06
 **/
@Component
public class MyXmlParser implements IXmlParser {
    private static String strTables="/OperationConfig/Tables";
    private static String strTable="/OperationConfig/Tables/Table";

    @Override
    public String parse(String xmlFilePath) throws Exception {
        Document doc=new SAXReader().read(new File(xmlFilePath));
        return this.parse(doc);
    }

    @Override
    public String parse(Document document) throws Exception {
        StringBuilder keyValue=new StringBuilder();
        Set<String> strSet=this.parse(document,"BCONSUMER","");
        for(String str:strSet){
            keyValue.append(str+"\r\n");
        }
        return keyValue.toString();
    }
    @Override
    public Set<String> parse(Document document, String tableName, String parentPath) throws Exception {
//        利用set去重
        Set<String> keyValueSet=new HashSet<>();


        Element mainTable = (Element) document.selectSingleNode(strTable + "[@name='"+tableName+"']");
        if(mainTable==null){
            throw new RuntimeException("表名不存在");
        }
        List<Node> columnPathNodes = mainTable.selectNodes(".//Columns/Column[@path]");

        if(columnPathNodes.size()>0&&!columnPathNodes.isEmpty()){
            for(Node columnPathNode:columnPathNodes){
                String path = ((Element) columnPathNode).attributeValue("path");
 //                替换@字符，避免Java命名不规范
                if(path.startsWith("@")){
                   path=path.replace("@","");
                }
//                拼接父路径，因为关联表的path是不完整的，必须添加他的引用表的RelatedTable节点的path属性
                if(!parentPath.isEmpty()&&!parentPath.equalsIgnoreCase("")){
                    path=parentPath+"\\"+path;
                }
                keyValueSet.add(path.replace("\\",".")+"="+path.replace("\\","/"));
//                keyValue.append(path.replace("\\",".")+"="+path.replace("\\","/")+"\r\n");
//            System.out.println(path.getValue());
            }
        }

        List<Node> columnQueryFromNodes = mainTable.selectNodes(".//Columns/Column[@query-from]");
        if(columnQueryFromNodes.size()>0&&!columnQueryFromNodes.isEmpty()){
            for(Node columnQueryFromNode:columnQueryFromNodes){
                List<Node> columnQueryConditions=columnQueryFromNode.selectNodes(".//Condition");
                if(!columnQueryConditions.isEmpty()&&columnQueryConditions.size()>0){
                    for(Node node:columnQueryConditions){
                        String path=((Element)node).attributeValue("path");
 //                替换@字符，避免Java命名不规范
                        if(path.startsWith("@")){
                            path=path.replace("@","");
                        }
                        if(!parentPath.isEmpty()&&!parentPath.equalsIgnoreCase("")){
                            path=parentPath+"\\"+path;
                        }
                        keyValueSet.add(path.replace("\\",".")+"="+path.replace("\\","/"));
//                        keyValue.append(path.replace("\\",".")+"="+path.replace("\\","/")+"\r\n");
                    }
                }
            }
        }


//        -------关联表--------------------------------------------
        Element relatedTablesEl = (Element) mainTable.selectSingleNode(".//RelatedTables");
        if(relatedTablesEl==null){
            return keyValueSet;
        }
//找到关联表的节点
        List<Node> relatedTablesNodes = relatedTablesEl.selectNodes(".//RelatedTable");

        if(relatedTablesNodes.size()>0&&!relatedTablesNodes.isEmpty()){
            for(Node relatedTablesNode:relatedTablesNodes){
//                这里是获取RelatedTable节点
                Element relatedTableEl=(Element)relatedTablesNode;
                keyValueSet.addAll(this.gerenateRelatedTableColumnPath(relatedTableEl,document,parentPath));
            }
        }


       return keyValueSet;
    }
    //    用于添加关联表的column的path
    private Set<String> gerenateRelatedTableColumnPath(Element relatedTableEl, Document document, String parentPath) throws Exception {

        String relatedTableName = relatedTableEl.attributeValue("name");
        String relatedTableParentPath = relatedTableEl.attributeValue("path");
//可能关联表还有关联表，所以这里路径还是要处理一下
        if(!parentPath.isEmpty()&&!parentPath.equalsIgnoreCase("")){
            relatedTableParentPath=parentPath+"\\"+relatedTableParentPath;
        }

        //查找指定的关联表节点
//        Element relatedTable = (Element) document.selectSingleNode(strTable + "[@name='" + relatedTableName + "']");
//到这里关联表还是一张表，跟主表类似的操作
        return this.parse(document,relatedTableName,relatedTableParentPath);
    }
}
