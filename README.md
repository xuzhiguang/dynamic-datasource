
<p align="center">
    <img src="doc/logo.png"/>
</p>
<p align="center">
	一个可通过配置中心<strong>热更新</strong>数据源的多数据源启动器
</p>

# 简介

dynamic datasource 是一个基于 spring boot 的多数据源启动器，可通过配置中心热更新数据源


# 使用方法

## 1.引入依赖（未上传 maven 中央仓库，自行本地打包）
```xml
<dependency>
    <groupId>com.xuzhiguang</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

## 2.配置数据源
```yaml
spring:
  xzg:
    dynamic:
      default-key: mysql1
      datasource:
        mysql1:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost/test1?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
          type: com.zaxxer.hikari.HikariDataSource
          username: root
          password: 123456
        mysql2:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost/test2?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
          type: com.zaxxer.hikari.HikariDataSource
          username: root
          password: 123456
        mysql3:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost/test3?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
          type: com.zaxxer.hikari.HikariDataSource
          username: root
          password: 123456
```

## 3.切换数据源

使用 @DynamicDataSource

可以直接填 key

```java
@DynamicDataSource("mysql1")
public void test() {
    String sql = "select * from test";
    List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
    System.out.println(list);
}
```

也可以通过参数传入

```java
@DynamicDataSource("#p0")
public void test(String key) {
    String sql = "select * from test";
    List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
    System.out.println(list);
}
```

## 4.配置中心热更新

当使用 spring cloud 配置中心时，可热更新数据源


# 开源许可
本项目采用 GPL 开源协议
