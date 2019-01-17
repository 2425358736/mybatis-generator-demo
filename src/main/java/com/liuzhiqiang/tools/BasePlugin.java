package com.liuzhiqiang.tools;
import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 开发公司：青岛海豚数据技术有限公司
 * 版权：青岛海豚数据技术有限公司
 * <p>
 * BasePlugin
 *
 * @author 刘志强
 * @created Create Time: 2019/1/16
 */
public class BasePlugin extends PluginAdapter {


    public boolean validate(List<String> list) {
        return true;
    }

    /**
     * 修改实体类
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        classAnnotation(topLevelClass,null);
        Set<FullyQualifiedJavaType> set = new HashSet<FullyQualifiedJavaType>();
        set.add(new FullyQualifiedJavaType(Annotation.ApiModel.getClazz()));
        set.add(new FullyQualifiedJavaType(Annotation.DATA.getClazz()));
        topLevelClass.addImportedTypes(set);

        topLevelClass.addAnnotation(Annotation.ApiModel.getAnnotation() + "(value=\"" + topLevelClass.getType() + "\",description=\"" + introspectedTable.getRemarks() + "\")");
        topLevelClass.addAnnotation(Annotation.DATA.getAnnotation() + "()");
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 修改mapper接口
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        interfazeAnnotation(interfaze, null);
        Set<FullyQualifiedJavaType> set = new HashSet<FullyQualifiedJavaType>();
        set.add(new FullyQualifiedJavaType(Annotation.Mapper.getClazz()));
        interfaze.addImportedTypes(set);
        interfaze.addAnnotation(Annotation.Mapper.getAnnotation() + "()");
        return super.clientGenerated(interfaze,topLevelClass,introspectedTable);
    }

    /**
     * 实体类字段
     * @param field
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        // 生成注释
        fieldAnnotation(field, introspectedColumn.getRemarks());
        // 生成注释结束


        // 追加ApiModelProperty注解
        topLevelClass.addImportedType(new FullyQualifiedJavaType(Annotation.ApiModelProperty.getClazz()));
        field.addAnnotation(Annotation.ApiModelProperty.getAnnotation() + "(value=\""+ introspectedColumn.getRemarks() + "\",name=\"" +introspectedColumn.getJavaProperty()+"\")");

        // 追加日期格式化注解
        if (introspectedColumn.getJdbcTypeName() == "TIMESTAMP") {
            field.addJavaDocLine(Annotation.JsonFormat.getAnnotation() + "(pattern = \"yyyy-MM-dd\",timezone=\"GMT+8\")");
            topLevelClass.addImportedType(new FullyQualifiedJavaType(Annotation.JsonFormat.getClazz()));
        }
        // tinyint数据（Byte）转换成（Integer）类型
        String a = field.getType().getShortName();
        if (StringUtils.equals("Byte", a)) {
            field.setType(new FullyQualifiedJavaType("java.lang.Integer"));
        }
        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }





    /**
     * get方法 false 不生成
     * @param method
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    /**
     * set方法 false 不生成
     * @param method
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    /**
     * mapper接口方法
     * DeleteByPrimaryKey方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        methodAnnotation(method, "根据主键删除数据");
        return super.clientDeleteByPrimaryKeyMethodGenerated(method,interfaze,introspectedTable);
    }


    /**
     * mapper接口方法
     * Insert方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        methodAnnotation(method, "插入数据库记录（不建议使用）");
        return super.clientInsertMethodGenerated(method,interfaze,introspectedTable);
    }


    /**
     * mapper接口方法
     * InsertSelective方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        methodAnnotation(method, "插入数据库记录（建议使用）");
        return super.clientInsertSelectiveMethodGenerated(method,interfaze,introspectedTable);
    }


    /**
     * mapper接口方法
     * SelectByPrimaryKey方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        methodAnnotation(method, "根据主键id查询");
        return super.clientSelectByPrimaryKeyMethodGenerated(method,interfaze,introspectedTable);
    }


    /**
     * mapper接口方法
     * UpdateByPrimaryKeySelective方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        methodAnnotation(method, "修改数据(推荐使用)");
        return super.clientUpdateByPrimaryKeySelectiveMethodGenerated(method,interfaze,introspectedTable);
    }


    /**
     * mapper接口方法
     * UUpdateByPrimaryKey方法
     * @param method
     * @param interfaze
     * @param introspectedTable
     * @return
     */
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        methodAnnotation(method, "修改数据");
        return super.clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method,interfaze,introspectedTable);
    }

    /**
     * 方法注释生成
     * @param method
     * @param explain
     */
    public static void methodAnnotation(Method method, String explain) {
        // 生成注释
        StringBuilder sb = new StringBuilder();
        method.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(explain);
        method.addJavaDocLine(sb.toString());
        Parameter parm = method.getParameters().get(0);
        sb.setLength(0);
        sb.append(" * @param ");
        sb.append(parm.getName());
        method.addJavaDocLine(sb.toString());
        method.addJavaDocLine(" */");
        // 生成注释结束
    }


    /**
     * 属性注释生成
     * @param field
     * @param explain
     */
    public static void fieldAnnotation(Field field, String explain) {
        // 生成注释
        StringBuilder sb = new StringBuilder();
        field.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(explain);
        field.addJavaDocLine(sb.toString());
        field.addJavaDocLine(" */");
        // 生成注释结束
    }

    /**
     * 类注释生成
     * @param topLevelClass
     * @param explain
     */
    public static void classAnnotation(TopLevelClass topLevelClass, String explain) {
        // 生成注释
        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine("* 开发公司：青岛海豚数据技术有限公司");
        topLevelClass.addJavaDocLine("* 版权：青岛海豚数据技术有限公司");
        topLevelClass.addJavaDocLine("* <p>");
        topLevelClass.addJavaDocLine("* " + topLevelClass.getType().getShortName());
        topLevelClass.addJavaDocLine("*");
        topLevelClass.addJavaDocLine("* @author 系统");
        topLevelClass.addJavaDocLine("* @created Create Time: " + new Date());
        topLevelClass.addJavaDocLine("*/");
        // 生成注释结束
    }


    /**
     * 接口注释生成
     * @param interfaze
     * @param explain
     */
    public static void interfazeAnnotation(Interface interfaze, String explain) {
        // 生成注释
        interfaze.addJavaDocLine("/**");
        interfaze.addJavaDocLine("* 开发公司：青岛海豚数据技术有限公司");
        interfaze.addJavaDocLine("* 版权：青岛海豚数据技术有限公司");
        interfaze.addJavaDocLine("* <p>");
        interfaze.addJavaDocLine("* " + interfaze.getType().getShortName());
        interfaze.addJavaDocLine("*");
        interfaze.addJavaDocLine("* @author 系统");
        interfaze.addJavaDocLine("* @created Create Time: " + new Date());
        interfaze.addJavaDocLine("*/");
        // 生成注释结束
    }
}
