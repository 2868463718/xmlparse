package zy.blue7.xmlparse.model.helpers;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;
import zy.blue7.xmlparse.model.interfaces.IXmlParser;
import zy.blue7.xmlparse.model.utils.StringUtils;

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
    private static String strAction="/OperationConfig/Action";
    private static String strExistenceCheck="/OperationConfig/ExistenceCheck";
    private static String strArrivalLog="/OperationConfig/ArrivalLog";
    private static String strTable="/OperationConfig/Tables/Table";

    @Override
    public String parse(String xmlFilePath) throws Exception {
        Document doc=new SAXReader().read(new File(xmlFilePath));
        return this.parse(doc);
    }


    @Override
    public String parse(Document document) throws Exception {
        StringBuilder keyValue=new StringBuilder();
        Set<String> strSet=new HashSet<>();
//        -----------获取Action节点对应的path属性------------------
        Element actionEl= (Element) document.selectSingleNode(strAction);
        String strActionPath=actionEl.attributeValue("path");
        String toLowerActionPath=this.toLowerStrWordFirst(strActionPath.replace("\\",".").replace("@",""));
        strSet.add(toLowerActionPath+"="+strActionPath.replace("\\","/"));

//        ----------------获取ExistenceCheck节点对应的path属性

        List<Node> queryConditionNodes = document.selectNodes(strExistenceCheck + "/" + "QueryConditions/QueryCondition");
        if(queryConditionNodes.size()>0&&!queryConditionNodes.isEmpty()){
            for(Node queryConditionNode:queryConditionNodes){
                Element queryConditionEl= (Element) queryConditionNode;
                String queryConditionPath=queryConditionEl.attributeValue("path");
                //小写首字母 ConsumerBestRecord.BestRecord.ProgramList.Program.PointsAcquired----》consumerBestRecord.cestRecord.crogramList.crogram.cointsAcquired
                String toLowerQueryConditionPath=this.toLowerStrWordFirst(queryConditionPath.replace("\\",".").replace("@",""));
                strSet.add(toLowerQueryConditionPath+"="+queryConditionPath.replace("\\","/"));
            }

        }



//        ----------------获取ArrivalLog节点对应的path属性
        List<Node> arrivalLogChildNodes = document.selectNodes(strArrivalLog + "/*[@path]");
        if(arrivalLogChildNodes.size()>0&&!arrivalLogChildNodes.isEmpty()){
            for(Node logNode:arrivalLogChildNodes){
                Element logEl = (Element) logNode;
                String logPath=logEl.attributeValue("path");
                String toLowerLogPath=this.toLowerStrWordFirst(logPath.replace("\\",".").replace("@",""));
                strSet.add(toLowerLogPath+"="+logPath.replace("\\","/"));
            }
        }


//        ----------------获取Tables节点对应的path属性
        strSet.addAll(this.parseTable(document,"BCONSUMER",""));
        for(String str:strSet){
            keyValue.append(str+"\r\n");
        }
        return keyValue.toString();
    }

    private String toLowerStrWordFirst(String replace) {
//      可能有的字符串是 a.b.c..<-----------a/b/c/.中将 / 替换成 .  而得到的
//      这一步可能会漏掉 ..
        String[] strs=replace.split("\\.");
        String[] strReplace=new String[strs.length];
        for(int i=0;i<strs.length;i++){
            strReplace[i]= StringUtils.toLowerCaseFirstOne(strs[i]);
        }
        if(replace.endsWith("..")){
//            在这里要添加上  ..
            return String.join(".",strReplace)+"..";
        }
        return  String.join(".",strReplace);
    }

    @Override
    public Set<String> parseTable(Document document, String tableName, String parentPath) throws Exception {
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


//                拼接父路径，因为关联表的path是不完整的，必须添加他的引用表的RelatedTable节点的path属性
                if(!parentPath.isEmpty()&&!parentPath.equalsIgnoreCase("")){
                    path=parentPath+"\\"+path;
                }
 //                替换@字符，避免Java命名不规范,只有key替换
                String toLowerPath1=this.toLowerStrWordFirst(path.replace("\\",".").replace("@",""));
                keyValueSet.add(toLowerPath1+"="+path.replace("\\","/"));
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


                        if(!parentPath.isEmpty()&&!parentPath.equalsIgnoreCase("")){
                            path=parentPath+"\\"+path;
                        }
//                替换@字符，避免Java命名不规范，只有key值替换
                        String toLowerPath2=this.toLowerStrWordFirst(path.replace("\\",".").replace("@",""));
                        keyValueSet.add(toLowerPath2+"="+path.replace("\\","/"));
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
        return this.parseTable(document,relatedTableName,relatedTableParentPath);
    }
}
