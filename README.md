# generator mysql反射生成（两种实现）
## 项目目录结构

![image](https://raw.githubusercontent.com/2425358736/mybatis-generator-demo/master/image/V2UP06S9X_93%5D%7DU0%5BQCL%6044.png)

## mybatis-generator文件
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>

    <context id="DB2Tables"    targetRuntime="MyBatis3">
        <!-- <plugin>元素用来定义一个插件。插件用于扩展或修改通过MyBatis Generator (MBG)代码生成器生成的代码。 -->
        <plugin type="com.liuzhiqiang.tools.BasePlugin" />

        <commentGenerator>
            <property name="javaFileEncoding" value="UTF-8"/>
            <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
            <property name="suppressAllComments" value="true" />
            <property name="suppressDate" value="true" />
        </commentGenerator>

        <!--数据库链接地址账号密码-->
        <jdbcConnection
                driverClass="com.mysql.jdbc.Driver"
                connectionURL="jdbc:mysql://127.0.0.1:3306/springcloud"
                userId="数据库账号"
                password="数据库密码"
        />

        <!--生成Model实体类存放位置-->
        <javaModelGenerator targetPackage="com.liuzhiqiang.domain.sysRole" targetProject="src/main/java">
            <property name="enableSubPackages" value="true"/>
            <property name="trimStrings" value="true"/>
        </javaModelGenerator>


        <!--生成映射文件xml存放位置-->
        <sqlMapGenerator targetPackage="mappers.sysRole" targetProject="src/main/resources">
            <property name="enableSubPackages" value="true"/>
        </sqlMapGenerator>


        <!--生成mapper接口存放位置-->
        <!-- 客户端代码，生成易于使用的针对Model对象和XML配置文件 的代码
                type="ANNOTATEDMAPPER",生成Java Model 和基于注解的Mapper对象
                type="MIXEDMAPPER",生成基于注解的Java Model 和相应的Mapper对象
                type="XMLMAPPER",生成SQLMap XML文件和独立的Mapper接口
        -->
        <javaClientGenerator type="XMLMAPPER" targetPackage="com.liuzhiqiang.mapper.sysRole" targetProject="src/main/java">
            <property name="enableSubPackages" value="true"/>
        </javaClientGenerator>
        <!--生成对应表及类名-->
            <table tableName="sys_role"
                   domainObjectName="SysRole"
                   enableCountByExample="false"
                   enableUpdateByExample="false"
                   enableDeleteByExample="false"
                   enableSelectByExample="false"
                   selectByExampleQueryId="false">
            </table>
    </context>
</generatorConfiguration>
```

#####  plugin元素用来定义一个插件。插件用于扩展或修改通过MyBatis Generator (MBG)代码生成器生成的代码
##### mybatis-generator-core 提供了常用的插件 PluginAdapter 要定制 生成的实体类 接口 及sql 我们只要继承PluginAdapter 并重写相对应的内部方法就好了
##### 简单介绍本文中用到的几个方法 及 参数说明





```
参数
    topLevelClass 对应生成的类
    Interface 对应生成的接口
    Field 对应生成的属性
    Method 对应生成的方法
        参数对象的常用方法
            addAnnotation 增加注解
            addImportedTypes 增加导入的包
            addJavaDocLine 增加注释
            setType 修改属性类型
    introspectedTable 数据库表映射信息
    introspectedColumn 数据库字段映射信息
        
    
PluginAdapter  提供的接口
    modelBaseRecordClassGenerated 修改实体类在此方法
    clientGenerated 修改生成的mapper接口
    modelFieldGenerated 修改生成的属性
    modelSetterMethodGenerated 修改set方法 false不生成
    modelGetterMethodGenerated 修改get方法 false不生成
    clientDeleteByPrimaryKeyMethodGenerated  对应mapper接口方法 DeleteByPrimaryKey方法
    client*****MethodGenerated 对应mapper接口内的各个方法

```

### 新建BasePlugin类 并继承 PluginAdapter。新建枚举类Annotation

### 本类中实现的业务主要有 实体类增加lombok 及 swagger2注解， 日期类型格式化注解，tinyint 转 Integer类型，增加类注释，接口注释，属性注释，方法注释，mapper接口增加@Mapper注解


```
package com.liuzhiqiang.tools;

/**
 * 开发公司：青岛海豚数据技术有限公司
 * 版权：青岛海豚数据技术有限公司
 * <p>
 * Annotation
 *
 * @author 刘志强
 * @created Create Time: 2019/1/16
 */
public enum Annotation {
    DATA("@Data", "lombok.Data"),
    Mapper("@Mapper", "org.apache.ibatis.annotations.Mapper"),
    Param("@Param", "org.apache.ibatis.annotations.Param"),
    ApiModel("@ApiModel", "io.swagger.annotations.ApiModel"),
    ApiModelProperty("@ApiModelProperty", "io.swagger.annotations.ApiModelProperty"),
    JsonFormat("@JsonFormat", "com.fasterxml.jackson.annotation.JsonFormat");

    private String annotation;

    private String clazz;
    Annotation(String annotation, String clazz) {
        this.annotation = annotation;
        this.clazz = clazz;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getClazz() {
        return clazz;
    }
}
```


```
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
            field.addAnnotation(Annotation.JsonFormat.getAnnotation() + "(pattern = \"yyyy-MM-dd\",timezone=\"GMT+8\")");
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
     * get方法 false 不生成
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


```

## 两种实现方式
#### 1 使用Java编码方式运行MBG
##### 新建main类 GenerateStartUp 运行main方法生成

```
package com.liuzhiqiang.tools;/**
 * Created by lzq on 2019/1/17.
 */

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 开发公司：青岛海豚数据技术有限公司
 * 版权：青岛海豚数据技术有限公司
 * <p>
 * GenerateStartUp
 *
 * @author 刘志强
 * @created Create Time: 2019/1/17
 */
public class GenerateStartUp {

    public static void main(String[] args) {
        List<String> warnings = new ArrayList<String>();
        try {
            boolean overwrite = true;
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("mybatis-generator.xml");
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(is);
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        } catch (XMLParserException e) {
            e.printStackTrace();
        }
        for (String warning : warnings) {
            System.out.println(warning);
        }
    }
}
```

#### 2 使用Maven执行MBG
#### pom文件如下 

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <name>mybatis-generator-demo</name>
    <groupId>mybatis-generator-demo</groupId>
    <artifactId>mybatis-generator-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!--定义插件仓库-->
    <pluginRepositories>

        <pluginRepository>
            <id>aliyun-repos</id>
            <name>aliyun Repository</name>
            <url>http://maven.aliyun.com/nexus/content/groups/public</url>
        </pluginRepository>

        <!-- 用gitee搭建maven私有库 -->
        <pluginRepository>
            <id>gitee-repos</id>
            <name>generator</name>
            <url>https://gitee.com/lzq199528/generator-extend/raw/master/</url>
        </pluginRepository>

    </pluginRepositories>

    <dependencies>
        <dependency>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-core</artifactId>
            <version>1.3.5</version>
        </dependency>

        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.2</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.21</version>
        </dependency>

        <!--lombok-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.2</version>
            <scope>provided</scope>
        </dependency>
        <!--end-->

        <!-- Swagger核心包 start -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.6.1</version>
        </dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.6.1</version>
        </dependency>
        <!-- Swagger核心包 end -->

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>2.8.7</version>
        </dependency>
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
            <version>2.6</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <!--mybatis-generator数据表自动生成-->
            <plugin>
                <groupId>org.mybatis.generator</groupId>
                <artifactId>mybatis-generator-maven-plugin</artifactId>
                <version>1.3.5</version>
                <dependencies>
                    <dependency>
                        <groupId> mysql</groupId>
                        <artifactId>mysql-connector-java</artifactId>
                        <version>5.1.21</version>
                    </dependency>
                    <dependency>
                        <groupId>org.mybatis.generator</groupId>
                        <artifactId>mybatis-generator-core</artifactId>
                        <version>1.3.5</version>
                    </dependency>
                    <dependency>
                        <groupId>com.liuzhiqiang.tools</groupId>
                        <artifactId>generator-extend</artifactId>
                        <version>1.0-SNAPSHOT</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <!--允许移动生成的文件 -->
                    <verbose>true</verbose>
                    <!-- 是否覆盖 -->
                    <overwrite>true</overwrite>
                    <!-- 自动生成的配置 -->
                    <configurationFile>
                        src/main/resources/mybatis-generator.xml</configurationFile>
                </configuration>
            </plugin>

        </plugins>
    </build>
</project>

```
#### generator-extend是BasePlugin生成的maven包 编译后地址：https://gitee.com/lzq199528/generator-extend 源码地址： https://github.com/2425358736/generator-extend

![image](https://raw.githubusercontent.com/2425358736/mybatis-generator-demo/master/image/P%5B1OV_LAW%252TZD1JN5%40_N\(F.png)

##### 双击运行mybatis-generator:generate 或者 运行GenerateStartUp后将生成实体类，接口，及xml 格式如下
![image](https://raw.githubusercontent.com/2425358736/mybatis-generator-demo/master/image/6L%24V345M%7B2QR%5DS8%24U5FZB%7BC.png)


#### SysRole

```
package com.liuzhiqiang.domain.sysRole;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;

/**
* 开发公司：青岛海豚数据技术有限公司
* 版权：青岛海豚数据技术有限公司
* <p>
* SysRole
*
* @author 系统
* @created Create Time: Thu Jan 17 17:01:12 CST 2019
*/
@ApiModel(value="com.liuzhiqiang.domain.sysRole.SysRole",description="")
@Data()
public class SysRole {
    /**
     * 
     */
    @ApiModelProperty(value="",name="id")
    private Long id;

    /**
     * 部门id
     */
    @ApiModelProperty(value="部门id",name="departmentId")
    private Long departmentId;

    /**
     * 角色编号
     */
    @ApiModelProperty(value="角色编号",name="roleNumber")
    private String roleNumber;

    /**
     * 角色名称
     */
    @ApiModelProperty(value="角色名称",name="roleName")
    private String roleName;

    /**
     * 角色类型
     */
    @ApiModelProperty(value="角色类型",name="roleType")
    private String roleType;

    /**
     * 备注
     */
    @ApiModelProperty(value="备注",name="remarks")
    private String remarks;

    /**
     * 创建人
     */
    @ApiModelProperty(value="创建人",name="createBy")
    private Long createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    @ApiModelProperty(value="创建时间",name="createDate")
    private Date createDate;

    /**
     * 更新者
     */
    @ApiModelProperty(value="更新者",name="updateBy")
    private Long updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    @ApiModelProperty(value="更新时间",name="updateDate")
    private Date updateDate;

    /**
     * 删除标记 0.正常 1.删除
     */
    @ApiModelProperty(value="删除标记 0.正常 1.删除",name="delFlag")
    private Integer delFlag;
}
```

#### SysRoleMapper

```
package com.liuzhiqiang.mapper.sysRole;

import com.liuzhiqiang.domain.sysRole.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
* 开发公司：青岛海豚数据技术有限公司
* 版权：青岛海豚数据技术有限公司
* <p>
* SysRoleMapper
*
* @author 系统
* @created Create Time: Thu Jan 17 17:01:12 CST 2019
*/
@Mapper()
public interface SysRoleMapper {
    /**
     * 根据主键删除数据
     * @param id
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 插入数据库记录（不建议使用）
     * @param record
     */
    int insert(SysRole record);

    /**
     * 插入数据库记录（建议使用）
     * @param record
     */
    int insertSelective(SysRole record);

    /**
     * 根据主键id查询
     * @param id
     */
    SysRole selectByPrimaryKey(Long id);

    /**
     * 修改数据(推荐使用)
     * @param record
     */
    int updateByPrimaryKeySelective(SysRole record);

    /**
     * 修改数据
     * @param record
     */
    int updateByPrimaryKey(SysRole record);
}
```
#### SysRoleMapper.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.liuzhiqiang.mapper.sysRole.SysRoleMapper">
  <resultMap id="BaseResultMap" type="com.liuzhiqiang.domain.sysRole.SysRole">
    <id column="id" jdbcType="BIGINT" property="id" />
    <result column="department_id" jdbcType="BIGINT" property="departmentId" />
    <result column="role_number" jdbcType="VARCHAR" property="roleNumber" />
    <result column="role_name" jdbcType="VARCHAR" property="roleName" />
    <result column="role_type" jdbcType="VARCHAR" property="roleType" />
    <result column="remarks" jdbcType="VARCHAR" property="remarks" />
    <result column="create_by" jdbcType="BIGINT" property="createBy" />
    <result column="create_date" jdbcType="TIMESTAMP" property="createDate" />
    <result column="update_by" jdbcType="BIGINT" property="updateBy" />
    <result column="update_date" jdbcType="TIMESTAMP" property="updateDate" />
    <result column="del_flag" jdbcType="TINYINT" property="delFlag" />
  </resultMap>
  <sql id="Base_Column_List">
    id, department_id, role_number, role_name, role_type, remarks, create_by, create_date, 
    update_by, update_date, del_flag
  </sql>
  <select id="selectByPrimaryKey" parameterType="java.lang.Long" resultMap="BaseResultMap">
    select 
    <include refid="Base_Column_List" />
    from sys_role
    where id = #{id,jdbcType=BIGINT}
  </select>
  <delete id="deleteByPrimaryKey" parameterType="java.lang.Long">
    delete from sys_role
    where id = #{id,jdbcType=BIGINT}
  </delete>
  <insert id="insert" parameterType="com.liuzhiqiang.domain.sysRole.SysRole">
    insert into sys_role (id, department_id, role_number, 
      role_name, role_type, remarks, 
      create_by, create_date, update_by, 
      update_date, del_flag)
    values (#{id,jdbcType=BIGINT}, #{departmentId,jdbcType=BIGINT}, #{roleNumber,jdbcType=VARCHAR}, 
      #{roleName,jdbcType=VARCHAR}, #{roleType,jdbcType=VARCHAR}, #{remarks,jdbcType=VARCHAR}, 
      #{createBy,jdbcType=BIGINT}, #{createDate,jdbcType=TIMESTAMP}, #{updateBy,jdbcType=BIGINT}, 
      #{updateDate,jdbcType=TIMESTAMP}, #{delFlag,jdbcType=TINYINT})
  </insert>
  <insert id="insertSelective" parameterType="com.liuzhiqiang.domain.sysRole.SysRole">
    insert into sys_role
    <trim prefix="(" suffix=")" suffixOverrides=",">
      <if test="id != null">
        id,
      </if>
      <if test="departmentId != null">
        department_id,
      </if>
      <if test="roleNumber != null">
        role_number,
      </if>
      <if test="roleName != null">
        role_name,
      </if>
      <if test="roleType != null">
        role_type,
      </if>
      <if test="remarks != null">
        remarks,
      </if>
      <if test="createBy != null">
        create_by,
      </if>
      <if test="createDate != null">
        create_date,
      </if>
      <if test="updateBy != null">
        update_by,
      </if>
      <if test="updateDate != null">
        update_date,
      </if>
      <if test="delFlag != null">
        del_flag,
      </if>
    </trim>
    <trim prefix="values (" suffix=")" suffixOverrides=",">
      <if test="id != null">
        #{id,jdbcType=BIGINT},
      </if>
      <if test="departmentId != null">
        #{departmentId,jdbcType=BIGINT},
      </if>
      <if test="roleNumber != null">
        #{roleNumber,jdbcType=VARCHAR},
      </if>
      <if test="roleName != null">
        #{roleName,jdbcType=VARCHAR},
      </if>
      <if test="roleType != null">
        #{roleType,jdbcType=VARCHAR},
      </if>
      <if test="remarks != null">
        #{remarks,jdbcType=VARCHAR},
      </if>
      <if test="createBy != null">
        #{createBy,jdbcType=BIGINT},
      </if>
      <if test="createDate != null">
        #{createDate,jdbcType=TIMESTAMP},
      </if>
      <if test="updateBy != null">
        #{updateBy,jdbcType=BIGINT},
      </if>
      <if test="updateDate != null">
        #{updateDate,jdbcType=TIMESTAMP},
      </if>
      <if test="delFlag != null">
        #{delFlag,jdbcType=TINYINT},
      </if>
    </trim>
  </insert>
  <update id="updateByPrimaryKeySelective" parameterType="com.liuzhiqiang.domain.sysRole.SysRole">
    update sys_role
    <set>
      <if test="departmentId != null">
        department_id = #{departmentId,jdbcType=BIGINT},
      </if>
      <if test="roleNumber != null">
        role_number = #{roleNumber,jdbcType=VARCHAR},
      </if>
      <if test="roleName != null">
        role_name = #{roleName,jdbcType=VARCHAR},
      </if>
      <if test="roleType != null">
        role_type = #{roleType,jdbcType=VARCHAR},
      </if>
      <if test="remarks != null">
        remarks = #{remarks,jdbcType=VARCHAR},
      </if>
      <if test="createBy != null">
        create_by = #{createBy,jdbcType=BIGINT},
      </if>
      <if test="createDate != null">
        create_date = #{createDate,jdbcType=TIMESTAMP},
      </if>
      <if test="updateBy != null">
        update_by = #{updateBy,jdbcType=BIGINT},
      </if>
      <if test="updateDate != null">
        update_date = #{updateDate,jdbcType=TIMESTAMP},
      </if>
      <if test="delFlag != null">
        del_flag = #{delFlag,jdbcType=TINYINT},
      </if>
    </set>
    where id = #{id,jdbcType=BIGINT}
  </update>
  <update id="updateByPrimaryKey" parameterType="com.liuzhiqiang.domain.sysRole.SysRole">
    update sys_role
    set department_id = #{departmentId,jdbcType=BIGINT},
      role_number = #{roleNumber,jdbcType=VARCHAR},
      role_name = #{roleName,jdbcType=VARCHAR},
      role_type = #{roleType,jdbcType=VARCHAR},
      remarks = #{remarks,jdbcType=VARCHAR},
      create_by = #{createBy,jdbcType=BIGINT},
      create_date = #{createDate,jdbcType=TIMESTAMP},
      update_by = #{updateBy,jdbcType=BIGINT},
      update_date = #{updateDate,jdbcType=TIMESTAMP},
      del_flag = #{delFlag,jdbcType=TINYINT}
    where id = #{id,jdbcType=BIGINT}
  </update>
</mapper>
```

#### 传送门： https://github.com/2425358736/mybatis-generator-demo
#### Java学习交流群 111764814

    