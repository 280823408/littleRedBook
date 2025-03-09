# littleRedBook

在实际开发过程中遇到的问题和实现细节：

1. 使用 MyBatis-Plus 中 `query()` 方法时，需要注意其是否存在多个表字段相同的问题。对于存在多表同名字段的情况，使用 `表名.字段名` 即可解决问题。如下所示：

```java
@Override
public class Result getUserByPhone(String phone) {
    return Result.ok(query().eq("user.phone", phone));
}
```
2. 使用hutool中的 `BeanUtil.beanToMap()`时，需注意转换的 `bean`中的属性不可存在空值,此处有两种处理方法

```java
stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().
                setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString())));
```

或者设置默认如下

```java
public class UserDTO {
    private Long id;
    private String userName = ""; // 默认值
    private String phone = ""; // 默认值
    private String userPassword = ""; // 默认值
}
```

3. 碰到SpringBoot启动类并未加载其配置文件的问题（如设置好了端口但并未使用）。`目前未解决`
4. 碰到了不同模块之间依赖注入无法共用的问题，比如 `CommunityApplication`启动类中调用littleredbook  
模块中的 `StringRedisClient`类，尝试使用了三种不同的注解方式如下所示
```java
@SpringBootApplication(scanBasePackages = "com.example")
@MapperScan("com.example.community.mapper")
```
```java
@SpringBootApplication
@ComponentScan("com.example")
@MapperScan("com.example.community.mapper")
```
```java
@SpringBootApplication(scanBasePackages = "com.example.littleredbook","com.example.community")
@MapperScan("com.example.community.mapper")
```
事实上，以上三种方式均无法解决问题，最终通过在 `CommunityApplication` 启动类中使用 `@Import` 注解引入 `littleredbook` 模块的配置类解决问题
```java
@SpringBootApplication
@MapperScan("com.example.community.mapper")
@Import({com.example.littleredbook.config.RedissonConfig.class, com.example.littleredbook.utils.StringRedisClient.class})
```
5. 使用springcloud时必须指定其相应版本号，并按照所发布的不会冲突的版本号进行依赖导入，最好使用BOM进行版本管理，如下所示，详细可看  
`https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E`
```java
<dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2022.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>2022.0.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```  
6.使用MyBatis-Plus时，遇到如下错误:  
`Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception 
[Request processing failed: org.mybatis.spring.MyBatisSystemException]
with root cause  com.baomidou.mybatisplus.core.exceptions.MybatisPlusException: can not use this method for "getSqlFirst"`
发生在如下代码段
```java
@Override
    public Result getConcernNumByUserId(Integer userId) {
        return Result.ok(baseMapper.selectCount(query().eq("user_id", userId)));
    }
```
经检查发现应该先获取 `query()`对应的 `Wapper()`即修改为如下代码段即可  
```java
@Override
    public Result getConcernNumByUserId(Integer userId) {
        return Result.ok(baseMapper.selectCount(query().getWrapper().eq("user_id", userId)));
    }
```
实际上，在 MyBatis-Plus 中，query() 是 QueryChainWrapper 的入口方法，返回的是 QueryChainWrapper 对象，而非直接可用的 QueryWrapper。  
selectCount 方法需要接收 Wrapper<T> 类型参数（通常为 QueryWrapper），但实际传入的是 QueryChainWrapper。  
MyBatis-Plus 尝试将 QueryChainWrapper 转换为 SQL 时，发现其不包含有效的 getSqlFirst 实现，抛出异常。  
而采用getWrapper() 从 QueryChainWrapper 中提取出真正的 QueryWrapper 实例，即可解决问题。  
7.采用先操作数据库再操作缓存除了防止多线程并发时可能出现的仍写入旧数据的情况外，还可以确保存入缓存的数据中`ID`一定非空。
具体情况如下代码所示
```java
public Result addConcern(Concern concern) {
        if (!save(concern)) {
            return Result.fail("添加新的关注记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_CONCERN_KEY + concern.getId(), concern);
        return Result.ok();
    }
```
假设此时我们采用先存入缓存再写入数据库的情况，那么当concern的`Id`为数据库中的自增字段，那么从前端获取的concern数据中的id
大概率为空，此时会产生空调用报错，并且写入缓存的数据也为空，从而造成隐患。
