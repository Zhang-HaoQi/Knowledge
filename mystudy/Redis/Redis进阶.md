# Redis进阶

## 基础

### 1. Redis是什么，优缺点

Redis是一个存储KV类型数据的内存数据库，整个数据库加载在内存中操作，定期通过异步操作将数据flush到磁盘上保存。

Redis是纯内存操作，性能好，每秒可以处理超过10万次读写操作，目前性能最快的kv数据库。

优点：

1. 读写性能极高， Redis能读的速度是110000次/s，写的速度是81000次/s。

2. 支持数据持久化，支持AOF和RDB两种持久化方式。

3. 支持事务， Redis的所有操作都是原子性的，意思就是要么成功执行要么失败完全不执行。单个操作是原子性的。多个操作也支持事务，即原子性，通过MULTI和EXEC指令包起来。

4. 数据结构丰富，除了支持string类型的value外，还支持hash、set、zset、list等数据结构。

5. 支持主从复制，主机会自动将数据同步到从机，可以进行读写分离。

6. 支持发布订阅， 通知， key 过期等特性。

缺点：

1. 数据库容量受到物理内存的限制，不能用作海量数据的高性能读写，因此Redis适合的场景主要局限在较小数据量的高性能操作和运算上。

2. 主机宕机，宕机前有部分数据未能及时同步到从机，切换IP后还会引入数据不一致的问题，降低了系统的可用性。

### 2. Redis为什么快

1. 基于内存存储：数据操作都基于内存，没有IO开销，数据都已KV形式保存，查找和操作数据的复杂度是O（1）

2. 单线程实现：

   Redis使用单个线程处理请求，避免了多个线程之间线程切换和锁资源争用的开销。注意：单线程是指的是在核心网络模型中，网络请求模块使用一个线程来处理，即一个线程处理所有网络请求。

   Redis 6 引入多线程IO，但多线程部分只是用来处理网络数据的读写和协议解析，执行命令仍然是单线程。

3. 非阻塞IO

   Redis使用多路复用IO技术，将epoll作为I/O多路复用技术的实现，再加上Redis自身的事件处理模型将epoll中的连接、读写、关闭都转换为事件，不在网络I/O上浪费过多的时间。

   ![image-20221129111317896](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221129111317896.png)

4. 优化的数据结构

   Redis有诸多可以直接应用的优化数据结构的实现，应用层可以直接使用原生的数据结构提升性能。

5. 底层模型不同

   Redis实现了自己的虚拟内存机制（VM），因为一般的系统调用系统函数的话，会浪费一定的时间与移动和请求。

   > Redis的VM(虚拟内存)机制就是暂时把不经常访问的数据(冷数据)从内存交换到磁盘中，从而腾出宝贵的内存空间用于其它需要访问的数据(热数据)。
   >
   > 通过VM功能可以实现冷热数据分离，使热数据仍在内存中、冷数据保存到磁盘。这样就可以避免因为内存不足而造成访问速度下降的问题。
   >
   > 
   >
   > Redis提高数据库容量的办法有两种：一种是可以将数据分割到多个RedisServer上；另一种是使用虚拟内存把那些不经常访问的数据交换到磁盘上。需要特别注意的是**Redis**并没有使用**OS**提供的**Swap**，而是自己实现。

### 3. Redis相比Memcached优势

1. 数据类型：redis提供了字符串，list，set，map，zset等数据结构，memcached所有值都是简单字符串
2. 持久化：redis支持AOF和RDB持久化存储，memcache不支持
3. 集群模式：Redis提供主从同步机制，以及 Cluster集群部署能力，能够提供高可用服务。Memcached没有原生的集群模式，需要依靠客户端来实现往集群中分片写入数据。
4. 性能：redis速度比memcached快
5. 网络IO：redis单线程实现多路复用的IO模型，memcached使用多线程实现非阻塞IO
6. 支持服务端操作：redis可以在服务端直接进行操作，而memcached必须先通过客户端获取，再Set回去。

### 4.为什么使用Redis做缓存

 数据库的并发能力和读写速度是有限的，使用Redis可以大大降低数据库压力，并且操作Redis直接操作缓存中的数据，比操作磁盘中的数据快很多。

### 5. 为什么用Redis而不用本地做缓存。

Java提供的本地缓存轻量，快速，但是随着JVM的销毁而结束，在多实例下，缓存会产生不一致性。

redis是分布式缓存，多实例可以共用一份缓存，缓存具有一致性。

1. Redis是分布式缓存，多个实例可以共享一份缓存数据，本地缓存不能共享。
2. Redis提供了持久化，本地缓存程序重启即消失。
3. Redis可以用几十 G 内存来做缓存，Map 不行，一般 JVM 也就分几个 G 数据就够大了；
4. Redis 可以处理每秒百万级的并发，是专业的缓存服务，Map 只是一个普通的对象；
5. Redis 可以处理每秒百万级的并发，是专业的缓存服务，Map 只是一个普通的对象；
6. Redis 可以处理每秒百万级的并发，是专业的缓存服务，Map 只是一个普通的对象；

### 6. Redis使用场景

1. 缓存
2. 排行榜，使用zset数据类型
3. 计数器，使用incr命令
4. 分布式会话，存储token或session信息
5. 分布式锁，应用高并发下可能产生不一致的问题。如：全局ID，减库存，秒杀等。
6. 朋友圈点赞，关注等
7. 消息系统 ，使用list或者stream可以实现队列功能
8. 队列
9. 限流

### 7. Redis数据结构

**基本数据类型**:string,hash,set,list,sortset

**其他数据结构：**

1、Bitmap：位图，Bitmap想象成一个以位为单位数组，数组中的每个单元只能存0或者1。签到，统计，布隆过滤器。

2、Hyperloglog。HyperLogLog 是一种用于统计基数的数据集合类型，HyperLogLog 的优点是，在输入元素的数量或者体积非常非常大时，计算基数所需的空间总是固定 的、并且是很小的。每个 HyperLogLog 键只需要花费 12 KB 内存，就可以计算接近 2^64 个不同元素的基数。场景：统计网页的UV（不重复访客，一个人访问某个网站多次，但是还是只计算为一次）。

要注意，HyperLogLog 的统计规则是基于概率完成的，所以它给出的统计结果是有一定误差的，标准误算率是 0.81%。

3、Geospatial ：主要用于存储地理位置信息，并对存储的信息进行操作，适用场景如朋友的定位、附近的人、打车距离计算等。

## 持久化

持久化原理查看：[mystudy/Redis/深度历险.md · Zhang-HaoQi/Knowledge - 码云 - 开源中国 (gitee.com)](https://gitee.com/zhang-haoqi/knowledge/blob/develop/mystudy/Redis/深度历险.md#2-持久化)

### 1. Redis持久化

将Redis内存数据写入磁盘，防止Redis意外宕机数据丢失和做Redis集群，数据备份。

**RDB**

指定间隔将内存快照写入磁盘，恢复时直接将快照文件数据读入内存。

优点：数据大规模恢复快

缺点：隔断时间备份，备份期间如果Redis宕机，会丢失最后一次快照后的所有修改。

**AOF**

追加写命令的形式备份数据，Redis重启时会读取该文件重新构建数据。

AOF使用追加写的方式，文件会越来越大，AOF有重写机制，当AOF文件的大小超过所设定的阈值时， Redis就会启动AOF文件的内容压缩，只保留可以恢复数据的最小指令集.。

优点：支持3种同步时机，每次发生数据修改持久化；每秒同步；从不同步。可以根据程序要求选择合适的同步时机

缺点：aof文件大小远大于rdb，aof的运行效率不如rdb

**混合持久化**

两次RDB之间，使用AOF，减少RDB次数和AOF文件大小。

优点：即使用了RDB快速恢复的特性，又发挥AOF记录数据不丢失。

缺点：兼容性差，redis4.0之后版本才支持。日志可读性较差。

### 2. 如何选择合适的持久化方式

1. 数据不敏感，容易重新补回：关闭持久化
2. 数据比较重要，但可以容忍一段时间的数据丢失，如缓存，可以只是用RDB
3. 如果做内存数据库，要使用持久化，RDB和AOF都开启，RDB做数据备份，AOF保证数据不丢失。

### 3. Redis持久化数据和缓存怎么做扩容

## 过期键的删除/淘汰策略

### 1. Redis过期键的删除策略

**惰性删除：**不主动删除key，查询时判断是否过期，过期的话删除返回null。优点：不需要对过期数据做额外处理。缺点：删除不及时，浪费内存空间。

**定期删除：**周期性的随机测试一批设置过期时间的key并进行处理。优点：控制key的删除频率，减少对CPU的影响，减少内存占用。缺点：key已过期，但是没有删除，对业务产生影响。

**定时删除：**为key设置过期时间，到时自动删除。redis内部创建了一个定时器，过期时间到来时执行。优点：节约内存，及时清理无效key。缺点：对CPU不好，过期key较多时，删除过期key会占用一部分CPU时间，对服务器的响应时间和吞吐量造成影响

### 2. Redis内存淘汰策略

如果key没有设置过期时间，redis中的数据越来越多，当超出最大的允许内存后，Redis会触发淘汰策略，删除不常用数据，保证Redis服务器正常运行。

通过：maxmemory-policy配置，默认 noeviction

六种淘汰策略：

**noeviction** ：拒绝写，可以删。线上业务不能继续运行，默认的淘汰策略。

**volatile-ttl：** 根据key 的剩余寿命 ttl 的值进行淘汰，ttl 越小越优先被淘汰。

**volatile-lru**：使用LRU算法，尝试淘汰设置了过期时间的 key，最近最少使用的 key 优先被淘汰。没有设置过期时间的 key 不会被淘汰，这样可以保证需要持久化的数据不会突然丢失。

**allkeys-lru：**使用LRU算法，淘汰所有最近最少使用的key，这意味着没有设置过期时间的 key 也会被淘汰。

**volatile-random**：淘汰的 key 是过期 key 集合中随机的 key。

**allkeys-random:** 不论是否设置过期时间，都随机淘汰

总结：volatile策略只会针对带过期时间的 key 进行淘汰，allkeys-xxx 策略会对所有的key 进行淘汰。

如果你只拿 Redis 做缓存，那应该使用 allkeys-xxx，客户端写缓存时不必携带过期时间。如果你还想同时使用 Redis 的持久化功能，那就使用 volatile-xxx 策略，这样可以保留没有设置过期时间的 key，它们是永久的 key 不会被 LRU 算法淘汰。

redis4.0之后，新增两种：

1. volatile-lfu:设置过期时间的key中，淘汰最不经常使用的key
2. allkeys-lfu：不论是否设置过期时间，都随机淘汰

## 缓存异常

缓存异常有四种类型，数据库的数据不一致、缓存雪崩、缓存击穿和缓存穿透。

### 1. 如何保证缓存与数据库双写时的数据一致性？

1. **先更新数据库，再更新缓存。  问题：并发更新，将脏数据刷新到缓存**
   1. ![image-20221129094459270](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221129094459270.png)

2. 先更新缓存，在更新数据库。 问题：数据库回滚
3. 先删除缓存，后更新数据库。 问题：删除后，有查询的话会再次建立缓存，造成脏数据
4. **先更新数据库，后删除缓存。  问题：删除失败**

### 2. 先删除缓存，后更新数据库产生的问题

问题：

1. 请求A删除缓存后，请求B发送新的请求，缓存不存在，将旧值更新进缓存，请求A更新数据库。如果数据没有设置失效时间，那么脏数据会一直存在。

   解决方式：使用延时双删，更新前删除一次，更新后，休眠1S后再删除一次。

2. 如果是主从分离，进行更新时，先将缓存中数据删除后，此时如果从库发生了读操作，此时缓存中没有，那么会从从库中获取数据返回，此时从库的数据还不是最新，又造成脏数据。

   解决方式：查询时，如果redis中数据为null，直接到主库进行查询。

使用读写异步的方式去解决：

请求A删除缓存后，更新数据库时，将请求A的更新信息发送到队列，此时如果信息B进行查询，如果redis为Null，并且更新队列中有数据，将查询B入队列。（思考：入队列后不是需要排队执行吗？如果排队的话那么需要等待程序响应，时间长的话用户体验就不是很好）

另外，读操作可以做去重处理，因为多个读操作在缓存中是没有意义的，这个方案自我感觉有一些问题。

### 3. 先更新数据库，再删除缓存产生的问题

问题：

删除缓存时，删除失败

解决：可以删除缓存时，如果删除失败，将删除缓存信息发送到队列（不可靠，容易造成误删，可以通过加UUID解决）

### 4. 双写模式和失效模式和最终方案

双写模式：先更新数据库，再更新redis。会造成写并发，导致旧的缓存把新的缓存覆盖

![image-20221129100636639](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221129100636639.png)

失效模式：先更新数据库，再删除redis。会造成删除缓存失败，再次读取脏数据。

![image-20221129100646785](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221129100646785.png)

最终方案：

![image-20221129100854223](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221129100854223.png)

两个模式，如果对数据的实时性要求不高，那么添加过期时间即可。一段时间后，肯定能获取最新的数据。

如果不能容忍缓存数据不一致，可以通过加读写锁保证并发读写或写写的时候按顺序排好队，读读的时候相当于无锁。

也可以用阿里开源的canal通过监听数据库的binlog日志及时的去修改缓存，但是引入了新的中间件，增加了系统的复杂度。

**使用cannal订阅更新缓存：**

![image-20221129100915912](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221129100915912.png)

canal更新缓存：可以通过监听mysql的binlog日志来更新redis，不好的是需要多维护一个canal服务。

canal解决数据异构：商城中，每个人推荐的内容可能都不一样，通过canal收集用户数据后，生成用户推荐表，为不同的用户推荐不同的数据。

### 5. 击穿

某一热点key失效，大量请求同时打入数据库，导致Mysql压力剧增。

解决方案：

1. 加互斥锁或者分布式锁，一次只允许一个请求进入数据库查询，其它请求等待。
2. 缓存永不过期
   1. 物理不过期，针对热点key不设置过期时间
   2. 逻辑过期，把过期时间存在key对应的value里，如果发现要过期了，通过一个后台的异步线程进行缓存的构建（定时续期）

### 6. 穿透

恶意攻击者不断请求系统中不存在的数据，会导致短时间大量请求落在数据库上，造成数据库压力过大。

解决：

前提是做好无效参数的校验，如id必须大于0，小于XX等。

1. 不存在的key放入redis，并设置过期时间。适合：key数量有限，重复率较高。
2. 使用布隆过滤器，如果判断不存在，则一定不存在，判断存在，则可能存在（有误判率）。将数据存入redis前，先存入过滤器。适合：key重复率较低，数量较多等。可以应用于恶意请求过滤。

### 7. 雪崩

某一时刻，大量key同时失效，大量的请求打在了数据库上面，造成数据库压力过大。

原因：1. redis宕机。2. redis设置的缓存时间一样。

解决方案：

1. 事前
   1. 设置不同的过期时间
   2. 分级缓存
   3. 热点key永不过期
      1. 物理不过期，针对热点key不设置过期时间
      2. 逻辑过期，把过期时间存在key对应的value里，如果发现要过期了，通过一个后台的异步线程进行缓存的构建（定时续期）
   4. redis集群
      1. 可以使用 主从+ 哨兵，Redis集群来避免 Redis 全盘崩溃的情况
2. 事中
   1. 互斥锁：在缓存失效后，通过互斥锁或者队列来控制读数据写缓存的线程数量，比如某个key只允许一个线程查询数据和写缓存，其他线程等待。这种方式会阻塞其他的线程，此时系统的吞吐量会下降
   2. 使用熔断机制，限流降级。当流量达到一定的阈值，直接返回“系统拥挤”之类的提示，防止过多的请求打在数据库上将数据库击垮，至少能保证一部分用户是可以正常使用，其他用户多刷新几次也能得到结果。
3. 事后
   1. 开启Redis持久化机制，尽快恢复缓存数据，一旦重启，就能从磁盘上自动加载数据恢复内存中的数据。

### 8. 什么是缓存预热

缓存预热是指系统上线后，提前将相关的缓存数据加载到缓存系统。避免在用户请求的时候，先查询数据库，然后再将数据缓存的问题，用户直接查询事先被预热的缓存数据。

如果不进行预热，那么Redis初始状态数据为空，系统上线初期，对于高并发的流量，都会访问到数据库中， 对数据库造成流量的压力。

缓存预热解决方案：

1. 数据量不大的时候，工程启动的时候进行加载缓存动作；

2. 数据量大的时候，设置一个定时任务，进行缓存的刷新；

3. 数据量太大的时候，优先保证热点数据进行提前加载到缓存。

### 9. 什么是缓存降级

缓存降级是指缓存失效或缓存服务器挂掉的情况下，不去访问数据库，直接返回默认数据或访问服务的内存数据。降级一般是有损的操作，所以尽量减少降级对于业务的影响程度。

在进行降级之前要对系统进行梳理，看看系统是不是可以丢卒保帅；从而梳理出哪些必须誓死保护，哪些可降级；比如可以参考日志级别设置预案：

一般：比如有些服务偶尔因为网络抖动或者服务正在上线而超时，可以自动降级；

警告：有些服务在一段时间内成功率有波动（如在95~100%之间），可以自动降级或人工降级，并发送告警；

错误：比如可用率低于90%，或者数据库连接池被打爆了，或者访问量突然猛增到系统能承受的最大阀值，此时可以根据情况自动降级或者人工降级；

严重错误：比如因为特殊原因数据错误了，此时需要紧急人工降级。

## 线程模型

### 1. Redis为何选择单线程？

在Redis 6.0以前，Redis的核心网络模型选择用单线程来实现。

解答：redis是一个DB，大多数请求都不是CPU密集型，而是IO密集型，如果不考虑 RDB/AOF 等持久化方案，Redis是完全的纯内存操作，执行速度是非常快的，因此这部分操作通常不会是性能瓶颈，Redis真正的性能瓶颈在于网络 I/O，也就是客户端和服务端之间的网络传输延迟，因此 Redis选择了单线程的 I/O 多路复用来实现它的核心网络模型。

具体原因:

1. 避免多线程切换带来的开销：线程之前的调度
2. 避免同步机制的开销：加锁
3. 简单可维护：如果使用多线程，底层数据结构必须是线程安全的，让redis的实现更复杂

### 2. Redis的多线程

 Redisv4.0（引入多线程处理异步任务）

 Redis 6.0（在网络模型中实现多线程 I/O ）

Redis是单线程，通常是指在Redis 6.0之前，其核心网络模型使用的是单线程。Redis6.0引入多线程**I/O**，只是用来处理网络数据的读写和协议的解析，而执行命令依旧是单线程。

> Redis在 v4.0 版本的时候就已经引入了的多线程来做一些异步操作，此举主要针对的是那些非常耗时的命令，通过将这些命令的执行进行异步化，避免阻塞单线程的事件循环。(持久化)
>
> 在 Redisv4.0 之后增加了一些的非阻塞命令如 UNLINK 、 FLUSHALL ASYNC 、 FLUSHDB ASYNC 。

### 3. 为什么引入多线程

 Redis的网络 I/O 瓶颈已经越来越明显。

互联网业务系统所要处理的线上流量越来越大，Redis的单线程模式会导致系统消耗很多 CPU 时间在网络 I/O 上从而降低吞吐量，要提升 Redis的性能有两个方向：

1. 优化网络 I/O 模块
2. 提高机器内存读写的速度

内存和读写速度依赖于硬件，优化网络IO的方向：

1. 零拷贝技术或者 DPDK 技术 ： 复杂，难度高，依赖硬件
2. 利用多核优势：目前主线程只能利用一个核

Redis支持多线程主要就是两个原因：

1. 可以充分利用服务器 CPU 资源，目前主线程只能利用一个核
2. 多线程任务可以分摊 Redis 同步 IO 读写负荷

引入多线程后，提升有1倍以上。

### 4. Redis的线程模型

**6.0之前**

Redis 是基于 reactor 模式开发了网络事件处理器，这个处理器叫做文件事件处理器,由于这个文件事件处理器是单线程的，所以 Redis 才叫做单线程的模型。

采用 IO 多路复用机制同时监听多个 Socket，根据 socket 上的事件来选择对应的事件处理器来处理这个事件。

多路指的是多个 Socket 连接，复用指的是复用一个线程。多路复用主要有三种技术：Select，Poll，Epoll,Epoll 是最新的也是目前最好的多路复用技术。

多个 socket 会产生不同的事件，不同的事件对应着不同的操作，IO 多路复用程序监听着这些 Socket，当这些 Socket 产生了事件，IO 多路复用程序会将这些事件放到一个队列中，通过这个队列，以有序、同步、每次一个事件的方式向文件时间分派器中传送。当事件处理器处理完一个事件后，IO 多路复用程序才会继续向文件分派器传送下一个事件。

![image-20221129160200764](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221129160200764.png)

文件事件处理器结构：

1. 多个Scoket

   Socket 会产生 AE_READABLE 和 AE_WRITABLE 事件：

   当 socket 变得**可读时或者有新的可以应答的 socket 出现时**，socket 就会产生一个AE_READABLE 事件

   当 socket 变得可写时，socket 就会产生一个 AE_WRITABLE 事件

2. IO多路复用程序

3. 文件事件分派器

4. 事件处理器

   1. 包含：连接应答处理器，命令请求处理器，命令回复处理器

   2. 如果是客户端要连接 Redis，那么会为 socket 关联连接应答处理器

      如果是客户端要写数据到 Redis（读、写请求命令），那么会为 socket 关联命令请求处理器

      如果是客户端要从 Redis 读数据，那么会为 socket 关联命令回复处理器

具体流程：

1. Redis 启动初始化的时候，Redis 会将连接应答处理器与 AE_READABLE 事件关联起来。

2. 如果一个客户端跟 Redis 发起连接，此时 Redis 会产生一个 AE_READABLE 事件，由于开始之初AE_READABLE 是与连接应答处理器关联，所以由连接应答处理器来处理该事件，这时连接应答处理器会与客户端建立连接，创建客户端响应的 socket，同时将这个 socket 的 AE_READABLE 事件与命令请求处理器关联起来。

3. 如果这个时间客户端向 Redis 发送一个命令（set k1 v1），这时 socket 会产生一个AE_READABLE 事件，IO 多路复用程序会将该事件压入队列中，此时事件分派器从队列中取得该事件，由于该 socket 的 AE_READABLE 事件已经和命令请求处理器关联了，因此事件分派器会将该事件交给命令请求处理器处理，命令请求处理器读取事件中的命令并完成。操作完成后，Redis 会将该 socket 的 AE_WRITABLE 事件与命令回复处理器关联。

4. 如果客户端已经准备好接受数据后，Redis 中的该 socket 会产生一个 AE_WRITABLE 事件，同样会压入队列然后被事件派发器取出交给相对应的命令回复处理器，由该命令回复处理器将准备好的响应数据写入 socket 中，供客户端读取。

5. 命令回复处理器写完后，就会删除该 socket 的 AE_WRITABLE 事件与命令回复处理器的关联关系。

### 5. Redis6.0多线程的实现机制

**流程**

1. 主线程负责接收建立连接请求，获取 Socket 放入全局等待读处理队列。
2. 主线程处理完读事件之后，通过 RR（Round Robin）将这些连接分配给这些 IO 线程。
3. 主线程阻塞等待 IO 线程读取 Socket 完毕。
4. **主线程通过单线程的方式执行请求命令，请求数据读取并解析完成，但并不执行。**
5. 主线程阻塞等待 IO 线程将数据回写 Socket 完毕

**特点：**

1. IO 线程要么同时在读 Socket，要么同时在写，不会同时读或写。
2. IO 线程只负责读写 Socket 解析命令，不负责命令处理。

**安全问题：**

从实现机制可以看出，Redis 的多线程部分只是用来处理网络数据的读写和协议解析，执行命令仍然是单线程顺序执行。

不需要去考虑控制 Key、Lua、事务，LPUSH/LPOP 等等的并发及线程安全问题。

### 6.  **Redis 6.0** 与 **Memcached** 多线程模型的对比

相同点：都采用了 Master 线程 -Worker 线程的模型。

不同点：Memcached 执行主逻辑也是在 Worker 线程里，模型更加简单，实现了真正的线程隔离，符合我们对线程隔离的常规理解。

而 Redis 把处理逻辑交还给 Master 线程，虽然一定程度上增加了模型复杂度，但也解决了线程并发安全等问题。

## 主从，哨兵，集群

### 1. Redis集群的方式

1. 单机
2. 主从
3. Redis Sentinel
4. Redis Cluster
5. Redis 自研

如果数据量很少，主要是承载高并发高性能的场景，比如缓存一般就几个G的话，单机足够了。

主从模式：master 节点挂掉后，需要手动指定新的 master，可用性不高，基本不用。

哨兵模式：master 节点挂掉后，哨兵进程会主动选举新的 master，可用性高，但是每个节点存储的数据是一样的，浪费内存空间。数据量不是很多，集群规模不是很大，需要自动容错容灾的时候使用。

Redis cluster 主要是针对海量数据+高并发+高可用的场景，如果是海量数据，如果你的数据量很大，那么建议就用Redis cluster，所有master的容量总和就是Redis cluster可缓存的数据容量。

### 2. 优缺点

**单机：**

优点：架构简单，部署方便，性能高。

缺点：

1. 数据可靠性不保证
2. 使用中，如果进程重启后，数据丢失，不能解决缓存预热问题，不适用于数据可靠性要求高的业务
3. 高性能受限于单核CPU的处理能力（Redis是单线程机制），CPU为主要瓶颈，所以适合操作命令简单，排序、计算较少的场景。也可以考虑用Memcached替代。

**主从：**

优点：

1. 高可靠性：一方面，采用双机主备架构，能够在主库出现故障时自动进行主备切换，从库提升为主库提供服务，保证服务平稳运行；另一方面，开启数据持久化功能和配置合理的备份策略，能有效的解决数据误操作和数据异常丢失的问题；
2. 读写分离策略：从节点可以扩展主库节点的读能力，有效应对大并发量的读操作。

缺点：

1. 故障恢复复杂，如果主节点故障，需要手动升级子节点为主节点
2. 主库写能力/存储能力受单机限制，可以考虑分片

原生复制的弊端在早期的版本中也会比较突出，如：Redis复制中断后，Slave会发起psync，此时如果同步不成功，则会进行全量同步，主库执行全量备份的同时可能会造成毫秒或秒级的卡顿；又由于COW机制，导致极端情况下的主库内存溢出，程序异常退出或宕机；主库节点生成备份文件导致服务器磁盘IO和CPU（压缩）资源消耗；发送数GB大小的备份文件导致服务器出口带宽暴增，阻塞请求，建议升级到最新版本。

**哨兵：**

master 宕机，哨兵会自动选举 master 并将其他的 slave 指向新的 master。

部署架构主要包括两部分：Redis Sentinel集群和Redis数据集群。

Redis Sentinel集群是由若干Sentinel节点组成的分布式集群，可以实现故障发现、故障自动转移、配置中心和客户端通知。Redis Sentinel的节点数量要满足2n+1（n>=1）的奇数个。

优点：

1. 解决主从模式下的高可用切换问题。

缺点：

1. 部署相对主从复杂
2. Redis数据节点中slave节点作为备份节点不提供服务
3. 不能解决读写分离问题
4. 每个服务都存储相同的数据，浪费内存
5. Redis Sentinel主要是针对Redis数据节点中的主节点的高可用切换，对Redis的数据节点做失败判定分为主观下线和客观下线两种，对于Redis的从节点有对节点做主观下线操作，**并不执行故障转移。**

**集群：**

将数据分片存储

Redis Cluster集群节点最小配置6个节点以上（3主3从），其中主节点提供读写操作，**从节点作为备用节点，不提供请求，只作为故障转移使用。**

优点：

1. 无中心架构
2. 数据分布在多个节点上，节点间数据共享，可动态调整数据分布
3. 可扩展：可线性扩展到1000多个节点，节点可动态添加或删除；
4. 高可用：部分节点不可用时，集群仍可用。通过增加Slave做standby数据副本，能够实现故障自动failover，节点之间通过gossip协议交换状态信息，用投票机制完成Slave到Master的角色提升；
5. 降低运维成本，提高系统的扩展性和可用性。

缺点：

> Client实现复杂，驱动要求实现Smart Client，缓存slots mapping信息并及时更新，提高了开发难度，客户端的不成熟影响业务的稳定性。目前仅JedisCluster相对成熟，异常处理部分还不完善，比如常见的“max redirect exception”。
>
> 节点会因为某些原因发生阻塞（阻塞时间大于clutser-node-timeout），被判断下线，这种failover是没有必要的。
>
> 数据通过异步复制，不保证数据的强一致性。
>
> 多个业务使用同一套集群时，无法根据统计区分冷热数据，资源隔离性较差，容易出现相互影响的情况。
>
> 150Slave在集群中充当“冷备”，不能缓解读压力，当然可以通过SDK的合理设计来提高Slave资源的利用率。
>
> Key批量操作限制，如使用mset、mget目前只支持具有相同slot值的Key执行批量操作。对于映射为不同slot值的Key由于Keys不支持跨slot查询，所以执行mset、mget、sunion等操作支持不友好。
>
> Key事务操作支持有限，只支持多key在同一节点上的事务操作，当多个Key分布于不同的节点上时无法使用事务功能。
>
> Key作为数据分区的最小粒度，不能将一个很大的键值对象如hash、list等映射到不同的节点。
>
> 不支持多数据库空间，单机下的Redis可以支持到16个数据库，集群模式下只能使用1个数据库空间，即db 0。
>
> 复制结构只支持一层，从节点只能复制主节点，不支持嵌套树状复制结构。
>
> 避免产生hot-key，导致主库节点成为系统的短板。
>
> 避免产生big-key，导致网卡撑爆、慢查询等。
>
> 重试时间应该大于cluster-node-time时间。
>
> Redis Cluster不建议使用pipeline和multi-keys操作，减少max redirect产生的场景。

### 3. Redis高可用方案具体实施

使用官方推荐的哨兵(sentinel)机制就能实现，当主节点出现故障时，由Sentinel自动完成故障发现和转移，并通知应用方，实现高可用性。它有四个主要功能：

集群监控，负责监控Redis master和slave进程是否正常工作。

消息通知，如果某个Redis实例有故障，那么哨兵负责发送消息作为报警通知给管理员。

故障转移，如果master node挂掉了，会自动转移到slave node上。

配置中心，如果故障转移发生了，通知client客户端新的master地址。

### 4. 主从复制原理

#### 快照同步

**Redis 主从工作原理**

1. 如果你为master配置了一个slave,不管这个slave是否是第一次连接上Master,它都会发送一个PSYNC命令给master请求复制数据。
2. master收到PSYNC命令后，会在后台进行数据持久化通过bgsave生成最新的rdb快照文件，持久化期间，master会继续接受客户端的请求，他会把这些可能修改的数据集的请求缓存在buffer中，当持久化进行完毕以后，master会将buffer发送给slave。
3. salve加载之前先要将当前内存的数据清空，之后把收到的数据进行持久化生成rdb,然后再加载到内存中。加载完毕后通知主节点继续进行增量同步。然后，master再将之前缓存在buffer中的命令发送给slave.
4. 当master与slave之间的连接由于某些原因而断开时，slave能够自动重连Master，如果master收到了多个slave并发连接请求，他只会进行一次持久化，而不是一个连接一次，然后再把这一份持久化的数据发送给多个并发连接的slave

#### 增量同步

Redis 同步的是指令流，主节点会将产生修改性影响的指令记录在本地的内存 buffer 中，然后异步将 buffer 中的指令同步到从节点，从节点一边执行同步的指令流来达到和主节点一样的状态，一遍向主节点反馈自己同步到哪里了 (偏移量)。

内存的 buffer 是有限的，所以 Redis 主库不能将所有的指令都记录在内存 buffer 中。Redis 的复制内存 buffer 是一个定长的环形数组，如果数组内容满了，就会从头开始覆盖前面的内容

如果网络状况不好，从节点在短时间内无法和主节点进行同步，那么当网络状况恢复时，Redis 的主节点中那些没有同步的指令在 buffer 中有可能已经被后续的指令覆盖掉了，从节点将无法直接通过指令流来进行同步，这个时候就需要快照同步。

**注意：**

如果全量备份时间过长或buffer过小，都会导致同步期间的增量指令在复制 buffer 中被覆盖，这样就会导致快照同步完成后无法进行增量复制，然后会再次发起快照同步，如此极有可能会陷入快照同步的死循环。以务必配置一个合适的复制 buffer 大小参数，避免快照复制的死循环。

当从节点刚刚加入到集群时，它必须先要进行一次快照同步，同步完成后再继续进行增量同步。

**主从复制流程图：**

![image-20221115154521199](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221115154521199.png)

#### 无盘复制

快照同步时，会进行很重的IO操作，对于非 SSD 磁盘存储时，快照会对系统的负载产生较大影响

当系统正在进行 AOF 的 fsync 操作时如果发生快照，fsync 将会被推迟执行，这就会严重影响主节点的服务效率

无盘复制：

无磁盘化复制是master不会将RDB文件落到本地磁盘，会将RDB文件直接从内存中通过网络传输到slave的内存中。如果我们的服务器使用的是普通的机械硬盘（重点是磁盘的读写效率很低），而且内网的网络带宽又很高（内网网速快），那么完全可以使用这种无磁盘化的复制方式。

```java
# 开启redis的无磁盘化复制，默认是关闭的
repl-diskless-sync yes
# 这一点很重要，因为一旦传输开始，就不可能服务新的从服务器到达，它将排队等待下一次RDB传输，所以服
# 务器等待延迟以便让更多的从节点到达。延迟以秒为单位指定，默认为5秒。禁用它完全只是设置为0秒，传
# 输将尽快开始。
repl-diskless-sync-delay 5
```

**过期key处理**

slave不会过期key，只会等待master过期key。如果master过期了一个key，或者通过LRU淘汰了一个key，那么会模拟一条del命令发送给slave。

**主从复制的断点续传**

从Redis 2.8开始，就支持主从复制的断点续传，如果主从复制过程中，网络连接断掉了，那么可以接着上次复制的地方，继续复制下去，而不是从头开始复制一份

master node会在内存中常见一个backlog，master和slave都会保存一个replica offset还有一个master id，offset就是保存在backlog中的。如果master和slave网络连接断掉了，slave会让master从上次的replica offset开始继续复制

#### wait指令

Redis 的复制是异步进行的，wait 指令可以让异步复制变身同步复制，确保系统的强一致性 (不严格)。wait 指令是 Redis3.0 版本以后才出现的。

```java
> set key value
OK
> wait 1 0
(integer) 1
```

wait 提供两个参数，第一个参数是从库的数量 N，第二个参数是时间 t，以毫秒为单位。它表示等待 wait 指令之前的所有写操作同步到 N 个从库 (也就是确保 N 个从库的同

步没有滞后)，最多等待时间 t。如果时间 t=0，表示无限等待直到 N 个从库同步完成达成一致。

假设此时出现了网络分区，wait 指令第二个参数时间 t=0，主从同步无法继续进行，wait 指令会永远阻塞，Redis 服务器将丧失可用性。

### 5. 主从延迟读取到过期数据怎么处理

1. 通过scan命令扫库：当Redis中的key被scan的时候，相当于访问了该key，同样也会做过期检测，充分发挥Redis惰性删除的策略。这个方法能大大降低了脏数据读取的概率，但缺点也比较明显，会造成一定的数据库压力，否则影响线上业务的效率。

2. Redis3.2之前：读从库并不会判断数据是否过期，所以有可能返回过期数据。

   Redis3.2之后：读从库，如果数据已经过期，则会过滤并返回空值。

### 6. 主从复制的过程中如果因为网络原因停止复制了会怎么样

1. 断开后，会自动重连

2. 2.8之后，有断点续传功能，可以接着上次复制的地方，继续复制下去，而不是从头开始复制一份。

   master如果发现有多个slave node都来重新连接，仅仅会启动一个rdb save操作，用一份数据服务所有slave node。master node会在内存中创建一个 backlog ，master和slave都会保存一个 replica offset ，还有一个 master id ，offset就是保存在backlog中的。如果master和slave网络连接断掉了，slave会让

   master从上次的replica offset开始继续复制。但是如果没有找到对应的offset，那么就会执行一次 resynchronization 全量复制

### 7. Redis主从架构数据会丢失吗

1. 异步复制导致的数据丢失：因为master -> slave的复制是异步的，所以可能有部分数据还没复制到slave，master就宕机了，此时这些部分数据就丢失了。
2. 脑裂导致的数据丢失：某个master所在机器突然脱离了正常的网络，跟其他slave机器不能连接，但是实际上master还运行着，此时哨兵可能就会认为master宕机了，然后开启选举，将其他slave切换成了master。这个时候，集群里就会有两个master，也就是所谓的脑裂。此时虽然某个slave被切换成了master，但是可能client还没来得及切换到新的master，还继续写向旧master的数据可能也丢失了。因此旧master再次恢复的时候，会被作为一个slave挂到新的master上去，自己的数据会清空，重新从新的master复制数据。

如何解决丢失问题？

不可避免，尽量减少。

redis配置：

```java
min-slaves-to-write 1
min-slaves-max-lag 10
```

min-slaves-to-write 默认情况下是0， min-slaves-max-lag 默认情况下是10。

至少有1个slave，数据复制和同步的延迟不能超过10秒。如果说一旦所有的slave，数据复制和同步的延迟都超过了10秒钟，那么这个时候，master就不会再接收任何请求了。

减小 min-slaves-max-lag 参数的值，这样就可以避免在发生故障时大量的数据丢失，一旦发现延迟超过了该值就不会往master中写入数据。

那么对于client，我们可以采取降级措施，将数据暂时写入本地缓存和磁盘中，在一段时间后重新写入master来保证数据不丢失；也可以将数据写入kafka消息队列，隔一段时间去消费kafka中的数据。

### 8. Redis哨兵怎么工作

1. 每个Sentinel以每秒钟一次的频率向它所知的Master，Slave以及其他 Sentinel 实例发送一个PING 命令。

2. 如果一个实例（instance）距离最后一次有效回复 PING 命令的时间超过 down-after-milliseconds选项所指定的值， 则这个实例会被当前 Sentinel 标记为主观下线。

3. 如果一个Master被标记为主观下线，则正在监视这个Master的所有 Sentinel 要以每秒一次的频率确认Master的确进入了主观下线状态。

4. 当有足够数量的 Sentinel（大于等于配置文件指定的值）在指定的时间范围内确认Master的确进入了主观下线状态， 则Master会被标记为客观下线 。

5. 当Master被 Sentinel 标记为客观下线时，Sentinel 向下线的 Master 的所有 Slave 发送 INFO 命令的频率会从 10 秒一次改为每秒一次 （在一般情况下， 每个 Sentinel 会以每 10 秒一次的频率向它已知的所有Master，Slave发送 INFO 命令 ）。

6. 若没有足够数量的 Sentinel 同意 Master 已经下线， Master 的客观下线状态就会变成主观下线。若 Master 重新向 Sentinel 的 PING 命令返回有效回复， Master 的主观下线状态就会被移除。

7. sentinel节点会与其他sentinel节点进行“沟通”，投票选举一个sentinel节点进行故障处理，在从节点中选取一个主节点，其他从节点挂载到新的主节点上自动复制新主节点的数据。

### 9. 故障转移时会从剩下的**slave**选举一个新的**master**，被选举为master的标准是什么？

如果一个master被认为odown了，而且majority哨兵都允许了主备切换，那么某个哨兵就会执行主备切换操作，此时首先要选举一个slave来，会考虑slave的一些信息。

1. 跟master断开连接的时长

   如果一个slave跟master断开连接已经超过了down-after-milliseconds的10倍，外加master宕机的时长，那么slave就被认为不适合选举为master

2. slave优先级

   按照slave优先级进行排序，slave priority越低，优先级就越高

3. 复制offset

   如果slave priority相同，那么看replica offset，哪个slave复制了越多的数据，offset越靠后，优先级就越高

4. run id

   如果上面两个条件都相同，那么选择一个run id比较小的那个slave。

### 10. 为什么**Redis**哨兵集群只有**2**个节点无法正常工作

**quorum:**法定哨兵数量

**majority：**大多数哨兵数量：2的majority=2，3的majority=2，5的majority=3，4的majority=2

只有majority>quorum才能切换master

如果quorum < majority，比如5个哨兵，majority就是3，quorum设置为2，那么就3个哨兵授权就可以执行切换,但是如果quorum >= majority，那么必须quorum数量的哨兵都授权，比如5个哨兵，quorum是5，那么必须5个哨兵都同意授权，才能执行切换。



如果哨兵集群仅仅部署了个2个哨兵实例，quorum=1

master宕机，s1和s2中只要有1个哨兵认为master宕机就可以还行切换，同时s1和s2中会选举出一个哨兵来执行故障转移

同时这个时候，需要majority，也就是大多数哨兵都是运行的，2个哨兵的majority就是2(2的majority=2，3的majority=2，5的majority=3，4的majority=2)，2个哨兵都运行着，就可以允许执行故障转移

但是如果整个M1和S1运行的机器宕机了，那么哨兵只有1个了，此时就没有majority来允许执行故障转移，虽然另外一台机器还有一个R1，但是故障转移不会执行

## 分布式

### 1. 分布式锁

是Java的锁只能保证单机的时候有效，分布式集群环境就无能为力了，这个时候我们就需要用到分布式锁。来控制分布式系统之间同步访问共享资源。

1、互斥性：在任何时刻，对于同一条数据，只有一台应用可以获取到分布式锁；

2、高可用性：在分布式场景下，一小部分服务器宕机不影响正常使用，这种情况就需要将提供分布式锁的服务以集群的方式部署；

3、防止锁超时：如果客户端没有主动释放锁，服务器会在一段时间之后自动释放锁，防止客户端宕机或者网络不可达时产生死锁；

4、独占性：加锁解锁必须由同一台服务器进行，也就是锁的持有者才可以释放锁，不能出现你加的锁，别人给你解锁了。

### 2. 常见的分布式锁解决方案

1. 基于Mysql

   依赖数据库的唯一性来实现资源锁定，比如主键和唯一索引等。

   缺点：

   这把锁强依赖数据库的可用性，数据库是一个单点，一旦数据库挂掉，会导致业务系统不可用。

   这把锁没有失效时间，一旦解锁操作失败，就会导致锁记录一直在数据库中，其他线程无法再获得到锁。

   这把锁只能是非阻塞的，因为数据的insert操作，一旦插入失败就会直接报错。没有获得锁的线程并不会进入排队队列，要想再次获得锁就要再次触发获得锁操作。

   这把锁是非重入的，同一个线程在没有释放锁之前无法再次获得该锁。因为数据中数据已经存在了

2. 基于Redis

   Redis 锁实现简单，理解逻辑简单，性能好，可以支撑高并发的获取、释放锁操作。

   缺点：

   Redis 容易单点故障，集群部署，并不是强一致性的，锁的不够健壮；

   key 的过期时间设置多少不明确，只能根据实际情况调整；

   需要自己不断去尝试获取锁，比较消耗性能。

3. 基于Zookeeper

   优点：

   zookeeper 天生设计定位就是分布式协调，强一致性，锁很健壮。如果获取不到锁，只需要添加一个监听器就可以了，不用一直轮询，性能消耗较小。

   缺点：

   在高请求高并发下，系统疯狂的加锁释放锁，最后 zk 承受不住这么大的压力可能会存在宕机的风险。

### Redis实现分布式锁

使用set is not exits 命令占锁，占锁成功后执行业务，执行完成后释放锁del。

setnx lock true

**问题：死锁**

业务未执行完，发生异常，del没有执行，陷入死锁，锁永远得不到释放。

解决：为锁设置过期时间。

**问题：锁误解**

业务时间过长，锁提前释放。业务再次执行时新添加锁，此时之前的业务执行完毕，将新业务的锁释放掉。

解决：

1. 每个线程加锁时，都设置一个唯一ID，删除的时候，根据id进行删除。
2. 分布式锁不要用于过长时间任务，如果出现了锁现象，数据小范围错乱人工干预。
3. 使用lua脚本，为每次加锁设置一个唯一的随机数，只能此随机数进行锁释放。（设置过期时间的话还有可能造成业务没执行完，锁提前释放问题-使用看门狗机制）

**问题：超时解锁并发**

如果线程 A 成功获取锁并设置过期时间 30 秒，但线程 A 执行时间超过了 30 秒，锁过期自动释放，此时线程 B 获取到了锁，线程 A 和线程 B 并发执行。这种情况是不被允许的。

解决：

1. 过期时间足够长
2. 为获取锁的线程增加守护线程，为将要过期但未释放的锁增加有效时间。

**问题：不可重入**

当线程在持有锁的情况下再次请求加锁，如果一个锁支持一个线程多次加锁，那么这个锁就是可重入的。如果一个不可重入锁被再次加锁，由于该锁已经被持有，再次加锁会失败。Redis 可通过对锁进行重入计数，加锁时加 1，解锁时减 1，当计数归 0 时释放锁

**问题：无法等待锁释放**

上述命令执行都是立即返回的，如果客户端可以等待锁释放就无法使用。

1. 可以通过客户端轮询的方式解决该问题，当未获取到锁时，等待一段时间重新获取锁，直到成功获取锁或等待超时。这种方式比较消耗服务器资源，当并发量比较大时，会影响服务器的效率。

2. 使用 Redis 的发布订阅功能，当获取锁失败时，订阅锁释放消息，获取锁成功后释放时，发送锁释放消息。

**手写可重入锁**

```java
@Component
@Log4j2
public class RedisLock {
  @Resource
  private RedisTemplate redisTemplate;
  private ThreadLocal<String> threadLock = new ThreadLocal<>();
  private ThreadLocal<Integer> threadLocalInteger = new ThreadLocal<Integer>();

  /**
   * @Description 加锁
   * @Date 2022/09/14 14:36
   * @Param [key, timeout, unit]
   * @return boolean
   */
  public boolean tryLock(String key, long timeout, TimeUnit unit) {
    Boolean isLocked = false;
    if (threadLock.get() == null) {
      String uuid = UUID.randomUUID() +"_"+System.currentTimeMillis();
      threadLock.set(uuid);
      isLocked = redisTemplate.opsForValue().setIfAbsent(key, uuid, timeout, unit);
      if(!isLocked){
        //
        for (;;) {
          isLocked = redisTemplate.opsForValue().setIfAbsent(key, uuid, timeout, unit);
          if (isLocked) {
            break;
          }
        }
      }
      //启动新线程来执行定时任务，更新锁过期时间
      new Thread(new UpdateLockTimeoutTask(uuid, redisTemplate, key)).start();
    } else {
      isLocked = true;
    }
    // 重入次数加1
    if (isLocked) {
      Integer count = threadLocalInteger.get() == null ? 0 : threadLocalInteger.get();
      threadLocalInteger.set(count++);
    }
    return isLocked;
  }
  /**
   * @Description 释放锁
   * @Date 2022/09/14 14:36
   * @Param [key]
   * @return void
   */
  public void releaseLock(String key) {
    //当前线程中绑定的uuid与Redis中的uuid相同
    String uuid= (String) redisTemplate.opsForValue().get(key);
    if(threadLock.get().equals(uuid)&&!StringUtils.isEmpty(uuid)){
      Integer count = threadLocalInteger.get();
      // 计数器减为0时才能释放锁
      if (count == null || --count <= 0) {
        redisTemplate.delete(key);
        // 获取更新锁超时时间的线程
        long threadId = (long) redisTemplate.opsForValue().get(uuid);
        Thread updateLockTimeoutThread = getThreadByThreadId(threadId);
        if (updateLockTimeoutThread != null) {
          // 中断更新锁超时时间的线程
          updateLockTimeoutThread.interrupt();
          redisTemplate.delete(uuid);
        }
      }
    }
  }

  /**
   * @Description 根据线程id获取线程对象
   * @Date 2022/09/14 14:36
   * @Param [threadId]
   * @return java.lang.Thread
   */
  public Thread getThreadByThreadId(long threadId) {
    ThreadGroup group = Thread.currentThread().getThreadGroup();
    Thread[] threads = new Thread[(int)(group.activeCount() * 1.2)];
    int current=0;
    if(group != null){
      int count = group.enumerate(threads, true);
      for (int i = 0; i < count; i++){
        if (threadId == threads[i].getId()) {
          current=i;
          break;
        }
      }
    }else {
      return null;
    }
    return threads[current];
  }
}


public class UpdateLockTimeoutTask implements Runnable{
  private String uuid;
  private String key;
  private RedisTemplate redisTemplate;

  //uuid=UUID.randomUUID() +"_"+System.currentTimeMillis();
  //key=userId+标识
  public UpdateLockTimeoutTask(String uuid, RedisTemplate redisTemplate, String key) {
    this.uuid = uuid;
    this.key = key;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void run() {
    // 将以uuid为Key，当前线程Id为Value的键值对保存到Redis中
    redisTemplate.opsForValue().set(uuid, Thread.currentThread().getId());
    while (true) {
      // 更新锁的过期时间，30秒更新一次
      redisTemplate.expire(key, 30, TimeUnit.SECONDS);
      try{
        // 每隔10秒执行一次;
        Thread.sleep(1000*10);
      }catch (InterruptedException e){
        break;
      }
    }
  }
}
```

## 优化

### 1. Redis如何做内存优化

1. 控制key的数量

2. 缩减键值对象：降低Redis内存使用最直接的方式就是缩减键（key）和值（value）的长度。

   key：减少key的长度

   value：删除value中的无效数据

3. 选择合适的数据结构

[Redis进阶不得不了解的内存优化细节 - 腾讯云开发者社区-腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/1162213)

### 2. 如果现在有个读超高并发的系统，用**Redis**来抗住大部分读请求，你会怎么设计？

如果是读高并发的话，先看读并发的数量级是多少，因为Redis单机的读QPS在万级，每秒几万没问题，使用一主多从+哨兵集群的缓存架构来承载每秒10W+的读并发，主从复制，读写分离。

使用哨兵集群主要是提高缓存架构的可用性，解决单点故障问题。主库负责写，多个从库负责读，支持水平扩容，根据读请求的QPS来决定加多少个Redis从实例。如果读并发继续增加的话，只需要增加Redis从实例就行了。

如果需要缓存1T+的数据，选择Redis cluster模式，每个主节点存一部分数据，假设一个master存32G，那只需要n*32G>=1T，n个这样的master节点就可以支持1T+的海量数据的存储了。

> Redis单主的瓶颈不在于读写的并发，而在于内存容量，即使是一主多从也是不能解决该问题，因为一主多从架构下，多个slave的数据和master的完全一样。假如master是10G那slave也只能存10G数据。所以数据量受单主的影响。
>
> 而这个时候又需要缓存海量数据，那就必须得有多主了，并且多个主保存的数据还不能一样。Redis官方给出的 Redis cluster 模式完美的解决了这个问题

## 问题：

1. redis的单线程和多线程

## 真实面试

### 1. Redis过期自动续期，如果一直续期造成死锁怎么办？

1. 检查自己代码逻辑，是否产生死循环
2. 检查数据库，是否有慢SQL或者数据库大量事务没有提交或超时
3. 请求网络资源，网络不稳定，可能造成超时，导致一直重试
   1. 设置最大请求时间
   2. 设置重试次数

整体解决方案：为锁定制最大过期时间或最大续期次数



