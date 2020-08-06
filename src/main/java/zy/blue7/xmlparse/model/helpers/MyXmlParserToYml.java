package zy.blue7.xmlparse.model.helpers;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;
import zy.blue7.xmlparse.model.interfaces.IXmlParser;
import zy.blue7.xmlparse.model.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author blue7
 * @date 2020/8/6 13:21
 **/
@Component
public class MyXmlParserToYml implements IXmlParser {
    private static String strTables="/OperationConfig/Tables";
    private static String strAction="/OperationConfig/Action";
    private static String strExistenceCheck="/OperationConfig/ExistenceCheck";
    private static String strArrivalLog="/OperationConfig/ArrivalLog";
    private static String strTable="/OperationConfig/Tables/Table";
    static Set<String> pathSet=new HashSet<>();


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
        strActionPath=strActionPath.replace("\\","/");
        pathSet.add(strActionPath);
//        这里值设置为 最后一个 / 后面的值
        strSet.add(toLowerActionPath+": "+strActionPath.substring(strActionPath.lastIndexOf("/")+1));

//        ----------------获取ExistenceCheck节点对应的path属性

        List<Node> queryConditionNodes = document.selectNodes(strExistenceCheck + "/" + "QueryConditions/QueryCondition");
        if(queryConditionNodes.size()>0&&!queryConditionNodes.isEmpty()){
            for(Node queryConditionNode:queryConditionNodes){
                Element queryConditionEl= (Element) queryConditionNode;
                String queryConditionPath=queryConditionEl.attributeValue("path");
                //小写首字母 ConsumerBestRecord.BestRecord.ProgramList.Program.PointsAcquired----》consumerBestRecord.cestRecord.crogramList.crogram.cointsAcquired
                String toLowerQueryConditionPath=this.toLowerStrWordFirst(queryConditionPath.replace("\\",".").replace("@",""));
                queryConditionPath=queryConditionPath.replace("\\","/");
                pathSet.add(queryConditionPath);
                strSet.add(toLowerQueryConditionPath+": "+queryConditionPath.substring(queryConditionPath.lastIndexOf("/")+1));
            }

        }



//        ----------------获取ArrivalLog节点对应的path属性
        List<Node> arrivalLogChildNodes = document.selectNodes(strArrivalLog + "/*[@path]");
        if(arrivalLogChildNodes.size()>0&&!arrivalLogChildNodes.isEmpty()){
            for(Node logNode:arrivalLogChildNodes){
                Element logEl = (Element) logNode;
                String logPath=logEl.attributeValue("path");
                String toLowerLogPath=this.toLowerStrWordFirst(logPath.replace("\\",".").replace("@",""));
                logPath=logPath.replace("\\","/");
                pathSet.add(logPath);
                strSet.add(toLowerLogPath+": "+logPath.substring(logPath.lastIndexOf("/")+1));
            }
        }


//        ----------------获取Tables节点对应的path属性
        strSet.addAll(this.parseTable(document,"BCONSUMER",""));

//        ---------------------将path字符串中每个节点都生成key：value---------------------------------

        for(String str:pathSet){

            List<String> strK=this.parsePathToYml(str);
            strSet.addAll(strK);
        }



//        --------------------------------------------------------------




        for(String str:strSet){
            keyValue.append(str+"\r\n");
        }
        return keyValue.toString();
    }

    private List<String> parsePathToYml(String str) {
        List<String> strings=new ArrayList<>();
        if(str==null||str.equalsIgnoreCase("")){
            return  null;
        }
        if(!str.contains("/")){
            strings.add(toLowerStrWordFirst(str.replace("@",""))+": "+str);
            return strings;
        }
//      value,就是json中的key，属性文件中的value
        String strLast=str.substring(str.lastIndexOf("/")+1);

        String strPro=str.substring(0,str.lastIndexOf("/"));

        strings.add(toLowerStrWordFirst(str.replace("@","").replace("/","."))+": "+strLast);
        strings.addAll(this.parsePathToYml(strPro));


        return  strings;
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
                path=path.replace("\\","/");
                pathSet.add(path);
                keyValueSet.add(toLowerPath1+": "+path.substring(path.lastIndexOf("/")+1));
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
                        path=path.replace("\\","/");
                        pathSet.add(path);
                        keyValueSet.add(toLowerPath2+": "+path.substring(path.lastIndexOf("/")+1));
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
