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
  
8.在MySQL中使用datetime类型时，需要注意时区的问题，如果计算从数据库中取得的时间和当前系统时间的字符串形式相同的情况下，相差接近
8个小时时，可能是时区不一致导致的问题。可以通过 `application.properties/yml`调整连接数据库的时区设置或者调整spring处理前端返回
数据时的时区设置如下所示:
```java
spring.datasource.url=jdbc:mysql://localhost:3306/little_red_book?serverTimezone=Asia/Shanghai
spring.jackson.time-zone=Asia/Shanghai
```
  
9.关于点赞的理解，方便起见为每个存在点赞数统计的实体类均增加了like_num字段及属性，在使用redis缓存时，为了确保点赞业务的一致性。
需要采用远程调用的方式确保相关点赞记录表和点赞数的同时增加。但针对通过redis缓存操作而言，由于点赞/取消点赞都是需要同时给出 `用户ID`
和 `点赞数据的ID`（如点赞评论所需的评论ID、点赞笔记所需的笔记ID）。  
由于先前的缓存只针对了点赞记录本身的ID进行缓存，因此在查询时我们并
不能通过Redis来优化该查询操作，因此如果存在需求（高并发）应用Redis进行优化的话，我们需要设计二级索引，即除了存入以键值对为<id,likeRecord>
Hash结构数据外，增加键值对<点赞数据ID + 用户ID, ID>来帮助我们优化该查询。  
在源代码的基础上，添加方法和删除方法中均增加对于该索引的存储/删除，此外对于删除而言，前端传入的数据为相应的ID，那么我们无法通过其删除索引结构，此时存在
两种方法。
```java
    @Override
    @Transactional
    public Result removeLikeNote(Integer id) {
        if (!removeById(id)) {
            return Result.fail("删除点赞笔记记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_LIKENOTE_KEY + id);
        return Result.ok();
    }
```
① 修改传入的数据为likeRecord整体如下所示
```java
    @Override
    @Transactional
    public Result removeLikeNote(LikeNote likeNote) {
        Integer id = likeNote.getId(id);
        if (!removeById(id) {
            return Result.fail("删除点赞笔记记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_LIKENOTE_KEY + id);
        hashRedisClient.delete(CACHE_LIKENOTE_NOTE_USER_KEY + likenote.getNoteId() + ":" + likenote.getUserId());
        return Result.ok();
    }
```
② 通过传入的id再次查询获取likeRecord如下所示
```java
    @Override
    @Transactional
    public Result removeLikeNote(Integer id) {
        LikeNote likeNote = (LikeNote) this.getLikeNoteById(id).getData();
        if (!removeById(id)) {
            return Result.fail("删除点赞笔记记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_LIKENOTE_KEY + id);
        hashRedisClient.delete(CACHE_LIKENOTE_NOTE_USER_KEY + likeNote.getNoteId() + ":" + likeNote.getUserId());
        return Result.ok();
    }
```
此次采用第一种方式，主要原因在于第二种方式虽然无需改动其他层的代码整体改动小，但会造成需要维护的。或许会好奇这种方式会增加一次查询，是否会导致性能下降/数据库访问压力过大。
但根据我们的实际应用场景而言，取消点赞操作场景频率通常较低，属于完全可接受的范畴。
