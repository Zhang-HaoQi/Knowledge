# Redis内部结构

## 1. 字符串

### SDS结构

redis没有使用C语言的字符串(以空字符结尾的字符数组)，而是自己构建的简单动态字符串（SDS），是redis的默认字符串。

rredis中，c字符串只用于一些无需对字符串修改的字面量，如日志打印。

当redis需要可被修改的字符串时，使用的是SDS。

```java
SET msg "hello world"
键值对的键是一个字符串对象，对象的底层实现是一个保存着字符串“msg”的SDS
键值对的值也是一个字符串对象，对象的底层实现是一个保存着字符串“hello world”的SDS
```

保存数据库中的字符串值之外，SDS还被用作缓冲区（buffer）：AOF模块中的AOF缓冲区，以及客户端状态中的输入缓冲区，都是由SDS实现的

SDS结构：

```c
struct sds {
    int len;// buf 中已占用字节数
    int free;// buf 中剩余可用字节数
    char buf[];// 数据空间
};
```

![image-20221117155613059](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117155613059.png)

·free属性的值为0，表示这个SDS没有分配任何未使用空间。

·len属性的值为5，表示这个SDS保存了一个五字节长的字符串。

·buf属性是一个char类型的数组，数组的前五个字节分别保存了'R'、'e'、'd'、'i'、's'五个字符，而最后一个字节则保存了空字符'\0'。

SDS遵循C字符串以空字符结尾的惯例，保存空字符的1字节空间不计算在SDS的len属性里面，并且为空字符分配额外的1字节空间，以及添加空字符到字符串末尾等操作，都是由SDS函数自动完成的，所以这个空字符对于SDS的使用者来说是完全透明的。遵循空字符结尾这一惯例的好处是，SDS可以直接重用一部分C字符串函数库里面的函数。

### SDS和C字符串区别

#### 1. 字符串长度记录

C语言使用长度为N+1的字符数组来表示长度为N的字符串，并且字符数组的最后一个元素总是空字符'\0'。

C语言使用的这种简单的字符串表示方式，并不能满足Redis对字符串在安全性、效率以及功能方面的要求。

C字符串并不记录自身的长度信息，所以为了获取一个C字符串的长度，程序必须遍历整个字符串，对遇到的每个字符进行计数，直到遇到代表字符串结尾的空字符为止，这个操作的复杂度为O（N）。

![image-20221117160505475](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117160505475.png)

SDS在len属性中记录了SDS本身的长度，所以获取一个SDS长度的复杂度仅为O（1）。

**通过使用SDS而不是C字符串，Redis将获取字符串长度所需的复杂度从O（N）降低到了O（1），这确保了获取字符串长度的工作不会成为Redis的性能瓶颈。**

#### 2. 杜绝缓冲区溢出

C字符串不记录自身长度带来的另一个问题是容易造成缓冲区溢出（buffer overflow）。

char *strcat(char *dest, const char *src);为拼接字符串 可以将src拼接到dest末尾。

因为C字符串不记录自身的长度，所以strcat假定用户在执行这个函数时，已经为dest分配了足够多的内存，可以容纳src字符串中的所有内容，而一旦这个假定不成立时，就会产生缓冲区溢出。

假设程序里有两个在内存中紧邻着的C字符串s1和s2，其中s1保存了字符串"Redis"，而s2则保存了字符串"MongoDB"

![image-20221117162222722](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117162222722.png)

**使用c字符串拼接**

执行：strcat(s1, " Cluster");

将s1的内容修改为"Redis Cluster"，忘了在执行strcat之前为s1分配足够的空间，那么在strcat函数执行之后，s1的数据将溢出到s2所在的空间中，导致s2保存的内容被意外地修改。

![image-20221117162906989](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117162906989.png)

SDS的空间分配策略完全杜绝了发生缓冲区溢出的可能性：当SDS API需要对SDS进行修改时，

API会先检查SDS的空间是否满足修改所需的要求，如果不满足的话，API会自动将SDS的空间扩展至执行修改所需的大小，然后才执行实际的修改操作，所以使用SDS既不需要手动修改SDS的空间大小，也不会出现前面所说的缓冲区溢出问题。

```c
int main ()
{
    //c创建字符串：char site[7] = {'R', 'U', 'N', 'O', 'O', 'B', '\0'};
    
   char src[50], dest[50];
 
   strcpy(src,  "This is source");      //将This is source放入src
   strcpy(dest, "This is destination"); //将This is destination放入dest
 
   strcat(dest, src);//将src拼接到dest后面
  
   printf("最终的目标字符串： |%s|", dest); // |This is destinationThis is source|
   
   return(0);
}
```

**使用SDS拼接**

将s1的内容修改为"Redis Cluster"，执行：strcat(s1, " Cluster");

sdscat将在执行拼接操作之前检查s的长度是否足够，在发现s目前的空间不足以拼接"Cluster"之后，sdscat就会先扩展s的空间，然后才执行拼接"Cluster"的操作

![image-20221117162536702](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117162536702.png)

拼接完成后，len为13，并且还多分配了13个字节的未使用空间。

#### 3. 减少修改字符串时带来的内存重分配次数

C字符串并不记录自身的长度，所以对于一个包含了N个字符的C字符串来说，这个C字符串的底层实现总是一个N+1个字符长的数组（额外的一个字符空间用于保存空字符）。因为C字符串的长度和底层数组的长度之间存在着这种关联性，所以每次增长或者缩短一个C字符串，程序都总要对保存这个C字符串的数组进行一次内存重分配操作：

·如果程序执行的是增长字符串的操作，比如拼接操作（append），那么在执行这个操作之前，程序需要先通过内存重分配来扩展底层数组的空间大小——如果忘了这一步就会产生缓冲区溢出。

·如果程序执行的是缩短字符串的操作，比如截断操作（trim），那么在执行这个操作之后，程序需要通过内存重分配来释放字符串不再使用的那部分空间——如果忘了这一步就会产生内存泄漏。

举例：

s：redis

将s的值改为 redis cluster；执行strcat(s, " Cluster")，此时需要对s内存空间重新分配，扩容s

再将redis cluster 改为 redis cluster Tutorial;执行 strcat(s, " Tutorial")，此时需要对s内存空间重新分配，扩容s

如果对字符串频繁修改，就需要进行频繁的扩容或缩容，这个操作比较耗时。

**一般程序中，字符串很少发生变化，可以容忍内存空间的扩展，但redis对速度要求严苛、数据被频繁修改的场合，如果每次修改数据都进行扩容或缩容，会大大降低性能**

**sds实现**

SDS通过未使用空间解除了字符串长度和底层数组长度之间的关联：在SDS中，buf数组的长度不一定就是字符数量加一，数组里面可以包含未使用的字节，而这些字节的数量就由SDS的free属性记录。

SDS实现了空间预分配和惰性空间释放两种优化策略：

1. 空间预分配

   空间预分配用于优化SDS的字符串增长操作：当SDS的API对一个SDS进行修改，并且需要对SDS进行空间扩展的时候，**程序不仅会为SDS分配修改所必须要的空间，还会为SDS分配额外的未使用空间。**

   如果对SDS进行修改之后，SDS的长度（也即是len属性的值）将小于1MB，那么程序分配和len属性同样大小的未使用空间，这时SDS len属性的值将和free属性的值相同。举个例子，如果进行修改之后，SDS的len将变成13字节，那么程序也会分配13字节的未使用空间，SDS的buf数组的实际长度将变成13+13+1=27字节（额外的一字节用于保存空字符）。

   ·如果对SDS进行修改之后，SDS的长度将大于等于1MB，那么程序会分配1MB的未使用空间。举个例子，如果进行修改之后，SDS的len将变成30MB，那么程序会分配1MB的未使用空间，SDS的buf数组的实际长度将为30MB+1MB+1byte。

   **通过空间预分配策略，Redis可以减少连续执行字符串增长操作所需的内存重分配次数。**

   在扩展SDS空间之前，SDS API会先检查未使用空间是否足够，如果足够的话，API就会直接使用未使用空间，而无须执行内存重分配。

   ![image-20221117170058188](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117170058188.png)

2. 惰性空间释放

   惰性空间释放用于优化SDS的字符串缩短操作：当SDS的API需要缩短SDS保存的字符串时，程序并不立即使用内存重分配来回收缩短后多出来的字节，而是使用free属性将这些字节的数量记录起来，并等待将来使用。

   sdstrim函数接受一个SDS和一个C字符串作为参数，移除SDS中所有在C字符串中出现过的字符。

   ![image-20221117171931668](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117171931668.png)

   sdstrim(s, "XY"); // 移除SDS 字符串中的所有'X' 和'Y'  

   ![image-20221117172232379](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117172232379.png)

   sdstrim之后的SDS并没有释放多出来的8字节空间，而是将这8字节空间作为未使用空间保留在了SDS里面，如果将来要对SDS进行增长操作的话，这些未使用空间就可能会派上用场。

   sdscat(s, " Redis");那么完成这次sdscat操作将不需要执行内存重分配：因为SDS里面预留的8字节空间已经足以拼接6个字节长的"Redis"

   ![image-20221117172324555](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117172324555.png)

   **通过惰性空间释放策略，SDS避免了缩短字符串时所需的内存重分配操作，并为将来可能有的增长操作提供了优化。SDS也提供了相关API，在有需要的时候真正释放SDS空间**

#### 4. 二进制安全

C字符串中的字符必须符合某种编码（比如ASCII），并且除了字符串的末尾之外，字符串里面不能包含空字符，否则最先被程序读入的空字符将被误认为是字符串结尾，这些限制使得C字符串**只能保存文本数据，而不能保存像图片、音频、视频、压缩文件这样的二进制数据**。

如果有一种使用空字符来分割多个单词的特殊数据格式，那么这种格式就不能使用C字符串来保存，因为C字符串所用的函数只会识别出其中的"Redis"，而忽略之后的"Cluster"。

![image-20221117194804815](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117194804815.png)

为了确保Redis可以适用于各种不同的使用场景，SDS的API都是二进制安全的（binary-safe），所有SDS API都会以处理二进制的方式来处理SDS存放在buf数组里的数据，程序不会对其中的数据做任何限制、过滤、或者假设，数据在写入时是什么样的，它被读取时就是什么样。

Redis使用buf数组来保存二进制数据，而非保存字符。

使用SDS来保存之前提到的特殊数据格式就没有任何问题，因为SDS使用len属性的值而不是空字符来判断字符串是否结束。

![image-20221117195751997](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117195751997.png)

使用二进制安全的SDS，而不是C字符串，使得Redis不仅可以保存文本数据，还可以保存任意格式的二进制数据。

#### 5. 兼容部分C字符串函数

SDS的API是二进制安全的，但是遵循C字符串以空字符结尾的惯例：这些API总会将SDS保存的数据的末尾设置为空字符，并且总会在为buf数组分配空间时多分配一个字节来容纳这个空字符，这是为了让那些保存文本数据的SDS可以重用一**部分定义的函数**。

#### 总结：二者区别，同时sds字符串优点

1. SDS字符串获取长度复杂度为O（1），c字符串为O（n）(sds记录了字符串长度，c没有记录，需要遍历)
2. c字符串拼接需要程序员事先分配好内存，如果内存不够会发生内存覆盖。sds会动态分配内存
3. c字符串分配的内存是固定的，如果字符串经常修改，会导致数据频繁的扩大和缩小，严重影响效率。sds发生数据改变时，如果改变后已有内存不满足使用，则进行扩容，扩容后，如果数据大小小于1M，则多申请出一倍已占内存大小，如果大于1M，则多申请1M的内存空间。
4. c字符串保存的是字符数据，遇到空格则认为读取结束，只能存储文本。sds保存的是字节数据，存储二进制流。
5. c字符串可以使用<string.h>库中所有的函数，sds只能使用部分函数。

## 2. 字典

Redis的数据库就是使用字典来作为底层实现的，对数据库的增、删、查、改操作也是构建在对字典的操作之上的。

SET msg "hello world"

在数据库中创建一个键为"msg"，值为"hello world"的键值对时，这个键值对就是保存在代表数据库的字典里面的。

字典还是哈希键的底层实现之一，**当一个哈希键包含的键值对比较多，或者键值对中的元素都是比较长的字符串时**，Redis就会使用字典作为哈希键的底层实现。

### 字典的实现

Redis的字典使用哈希表作为底层实现，一个哈希表里面可以有多个哈希表节点，而每个哈希表节点就保存了字典中的一个键值对。

![image-20221118092255063](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118092255063.png)

**hash表**

```c
typedef struct dictht {
    // 哈希表数组
    dictEntry **table;
    // 哈希表大小
    unsigned long size;
    //哈希表大小掩码，用于计算索引值
    //总是等于size-1
    unsigned long sizemask;
    // 该哈希表已有节点的数量
    unsigned long used;
} dictht;
```

table属性是一个数组，数组中的每个元素都是一个指向dict.h/dictEntry结构的指针，每个dictEntry结构保存着一个键值对。

size属性记录了哈希表的大小，也即是table数组的大小

used属性则记录了哈希表目前已有节点（键值对）的数量。

sizemask属性的值总是等于size-1，这个属性和哈希值一起决定一个键应该被放到table数组的哪个索引上面。

一个大小为4的空哈希表（没有包含任何键值对）。

![image-20221118092533453](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118092533453.png)

**hash表节点**

哈希表节点使用dictEntry结构表示，每个**dictEntry结构都保存着一个键值对**

```c
typedef struct dictEntry {
    // 键
    void *key;
    // 值
    union{
        void *val;
        uint64_tu64;
        int64_ts64;
    } v;
    // 指向下个哈希表节点，形成链表
    struct dictEntry *next;
} dictEntry;
```

key属性保存着键值对中的键，而v属性则保存着键值对中的值，其中键值对的值可以是一个指针，或者是一个uint64_t整数，又或者是一个int64_t整数。

next属性是指向另一个哈希表节点的指针，这个指针可以将多个哈希值相同的键值对连接在一次，以此来解决键冲突（collision）的问题。

![image-20221118092834473](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118092834473.png)

**字典**

```c
typedef struct dict {
    // 类型特定函数
    dictType *type;
    // 私有数据
    void *privdata;
    // 哈希表
    dictht ht[2];
    // rehash索引
    //当rehash不在进行时，值为-1
    in trehashidx; /* rehashing not in progress if rehashidx == -1 */
} dict;
```

type属性和privdata属性是针对不同类型的键值对，为创建多态字典而设置的：

type属性是一个指向dictType结构的指针，每个dictType结构保存了一簇用于**操作特定类型键值对的函数**，Redis会为用途不同的字典设置不同的类型特定函数。

privdata属性则保存了需要传给那些类型特定函数的可选参数。

```c
typedef struct dictType {
    // 计算哈希值的函数
    unsigned int (*hashFunction)(const void *key);
    // 复制键的函数
    void *(*keyDup)(void *privdata, const void *key);
    // 复制值的函数
    void *(*valDup)(void *privdata, const void *obj);
    // 对比键的函数
    int (*keyCompare)(void *privdata, const void *key1, const void *key2);
    // 销毁键的函数
    void (*keyDestructor)(void *privdata, void *key);
    // 销毁值的函数
    void (*valDestructor)(void *privdata, void *obj);
} dictType;
```

ht属性是一个包含两个项的数组，数组中的每个项都是一个dictht哈希表，一般情况下，字典只使用ht[0]哈希表，ht[1]哈希表只会在对ht[0]哈希表进行rehash时使用。

除了ht[1]之外，另一个和rehash有关的属性就是rehashidx，它记录了rehash目前的进度，如果目前没有在进行rehash，那么它的值为-1。

一个普通状态下（没有进行rehash）的字典。

![image-20221118093937163](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118093937163.png)

### hash算法

当要将一个新的键值对添加到字典里面时，程序需要先根据键值对的键计算出哈希值和索引值，然后再根据索引值，将包含新键值对的哈希表节点放到哈希表数组的指定索引上面。

Redis计算哈希值和索引值的方法

```c
#使用字典设置的哈希函数，计算键key的哈希值
hash = dict->type->hashFunction(key);
#使用哈希表的sizemask属性和哈希值，计算出索引值
#根据情况不同，ht[x]可以是ht[0]或者ht[1]
index = hash & dict->ht[x].sizemask;  # 两个位都为1时，结果才为1
```

将一个键值对k0和v0添加到字典，假如sizemask为3，size为4

1. hash = dict->type->hashFunction(k0);假设计算得出的哈希值为8
2. index = hash&dict->ht[0].sizemask = 8 & 3 = 0;
3. 计算出键k0的索引值0，这表示包含键值对k0和v0的节点应该被放置到哈希表数组的索引0位置

当字典被用作数据库的底层实现，或者哈希键的底层实现时，Redis使用MurmurHash2算法来计算键的哈希值。

算法的优点在于，即使输入的键是有规律的，算法仍能给出一个很好的随机分布性，并且算法的计算速度也非常快。

Redis使用的是MurmurHash2,[(382条消息) 哈希MurmurHash算法详解_yjgithub的博客-CSDN博客_murmurhash](https://blog.csdn.net/yjgithub/article/details/120447399)

### 键冲突

当有两个或以上数量的键被分配到了哈希表数组的同一个索引上面时，我们称这些键发生了冲突

Redis的哈希表使用链地址法（separate chaining）来解决键冲突，每个哈希表节点都有一个next指针，多个哈希表节点可以用next指针构成一个单向链表，被分配到同一个索引上的多个节点可以用这个单向链表连接起来，这就解决了键冲突的问题。

因为dictEntry节点组成的链表没有指向链表表尾的指针，所以为了速度考虑，程序总是将新节点添加到链表的表头位置（复杂度为O（1）），排在其他已有节点的前面。

![image-20221118095627042](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118095627042.png)

### rehash

随着操作的不断执行，哈希表保存的键值对会逐渐地增多或者减少，为了让哈希表的负载因子（load factor）维持在一个合理的范围之内，当哈希表保存的键值对数量太多或者太少时，程序需要对哈希表的大小进行相应的扩展或者收缩。扩展和收缩哈希表的工作可以通过执行rehash（重新散列）操作来完成。

执行步骤：

1. 为字典的ht[1]哈希表分配空间，这个哈希表的空间大小取决于要执行的操作，以及**ht[0]当前包含的键值对数量（也即是ht[0].used属性的值）**
   1. 如果执行的是扩展操作，那么ht[1]的大小为**第一个大于等于ht[0].used*2**的（2的n次方幂）；
   2. 如果执行的是收缩操作，那么ht[1]的大小为**第一个大于等于ht[0].used**的2的n次方幂）；
2. 将保存在ht[0]中的所有键值对rehash到ht[1]上面：**rehash指的是重新计算键的哈希值和索引值，**然后将键值对放置到ht[1]哈希表的指定位置上。
3. 当ht[0]包含的所有键值对都迁移到了ht[1]之后（ht[0]变为空表），**释放ht[0]，将ht[1]设置为ht[0]，并在ht[1]新创建一个空白哈希表**，为下一次rehash做准备。

**哈希表的扩展与收缩条件**

当以下条件中的任意一个被满足时，程序会自动开始对哈希表执行扩展操作：

1. 服务器目前没有在执行BGSAVE命令或者BGREWRITEAOF命令，并且哈希表的负载因子大于等于1。

   Redis Bgsave 命令用于在后台异步保存当前数据库的数据到磁盘。

   Redis Bgrewriteaof 命令用于异步执行一个 AOF（AppendOnly File） 文件重写操作。重写会创建一个当前 AOF 文件的体积优化版本。

2. 服务器目前正在执行BGSAVE命令或者BGREWRITEAOF命令，并且哈希表的负载因子大于等于5。

```c
# 负载因子 = 哈希表已保存节点数量/ 哈希表大小
load_factor = ht[0].used / ht[0].size
```

根据BGSAVE命令或BGREWRITEAOF命令是否正在执行，服务器执行扩展操作所需的负载因子并不相同，这是因为在执行BGSAVE命令或BGREWRITEAOF命令的过程中，Redis需要创建当前服务器进程的子进程，而大多数操作系统都采用写时复制（copy-on-write）技术来优化子进程的使用效率，所以在子进程存在期间，服务器会提高执行扩展操作所需的负载因子，从而尽可能地避免在子进程存在期间进行哈希表扩展操作，这可以避免不必要的内存写入操作，最大限度地节约内存。

写时复制：子线程向磁盘写数据时，会读取主存中的数据，如果主存中的数据没有改变不影响，如果改变了，则拷贝一个副本，将副本写入磁盘。如果在BGSAVE过程中，发生了rehash，那么数据的内存空间发生变化，需要拷贝大量数据的副本，浪费内存空间。

另一方面，当哈希表的负载因子小于0.1时，程序自动开始对哈希表执行收缩操作。

### 渐进式rehash

扩展或收缩哈希表需要将ht[0]里面的所有键值对rehash到ht[1]里面，但是，这个rehash动作并不是一次性、集中式地完成的，而是分多次、渐进式地完成的。

如果ht[0]里只保存着四个键值对，那么服务器可以在瞬间就将这些键值对全部rehash到ht[1]；但是，如果哈希表里保存的键值对数量不是四个，而是四百万、四千万甚至四亿个键值对，那么要一次性将这些键值对全部rehash到ht[1]的话，庞大的计算量可能会导致服务器在一段时间内停止服务。

为了避免rehash对服务器性能造成影响，服务器不是一次性将ht[0]里面的所有键值对全部rehash到ht[1]，而是分多次、渐进式地将ht[0]里面的键值对慢慢地rehash到ht[1]。

渐进式rehash步骤：

1. 为ht[1]分配空间，让字典同时持有ht[0]和ht[1]两个哈希表。
2. 在字典中维持一个索引计数器变量rehashidx，并将它的值设置为0，表示rehash工作正式开始。
3. 在rehash进行期间，**每次对字典执行添加、删除、查找或者更新操作时，程序除了执行指定的操作以外，还会顺带将ht[0]哈希表在rehashidx索引上的所有键值对rehash到ht[1]，当rehash工作完成之后，程序将rehashidx属性的值增一。**
4. 随着字典操作的不断执行，**最终在某个时间点上，ht[0]的所有键值对都会被rehash至ht[1]，这时程序将rehashidx属性的值设为-1**，表示rehash操作已完成。

渐进式rehash的好处在于它采取分而治之的方式，将rehash键值对所需的计算工作均摊到对字典的每个添加、删除、查找和更新操作上，从而避免了集中式rehash而带来的庞大计算量。

**渐进式rehash操作**

因为在进行渐进式rehash的过程中，字典会同时使用ht[0]和ht[1]两个哈希表，所以在渐进式rehash进行期间，字典的删除（delete）、查找（find）、更新（update）等操作会在两个哈希表上进行。例如，要在字典里面查找一个键的话，程序会先在ht[0]里面进行查找，如果没找到的话，就会继续到ht[1]里面进行查找，诸如此类。

另外，在渐进式rehash执行期间，新添加到字典的键值对一律会被保存到ht[1]里面，而ht[0]则不再进行任何添加操作，这一措施保证了ht[0]包含的键值对数量会只减不增，并随着rehash操作的执行而最终变成空表。

查找：先找ht[o]，找到了将ht[0]的搬迁到ht[1]，并删除ht[0]

删除：ht[o]有直接删除，没有则在ht[1]删除

增加：直接新增到ht[1]

更新：将ht[o]搬到ht[1]，再进行更新。

### 总结

·字典被广泛用于实现Redis的各种功能，其中包括数据库和哈希键。

·Redis中的字典使用哈希表作为底层实现，每个字典带有两个哈希表，一个平时使用，另一个仅在进行rehash时使用。

·当字典被用作数据库的底层实现，或者哈希键的底层实现时，Redis使用MurmurHash2算法来计算键的哈希值。

·哈希表使用链地址法来解决键冲突，被分配到同一个索引上的多个键值对会连接成一个单向链表。

·在对哈希表进行扩展或者收缩操作时，程序需要将现有哈希表包含的所有键值对rehash到新哈希表里面，并且这个rehash过程并不是一次性地完成的，而是渐进式地完成的。

## 3. 链表

C语言没有内置的链表结构，redis进行了自己的链表实现。

当一个列表键包含了数量比较多的元素，又或者列表中包含的元素都是比较长的字符串时，Redis就会使用链表作为列表键的底层实现。

使用场景：链表键，发布与订阅、慢查询、监视器等功能也用到了链表，Redis服务器本身还使用链表来保存多个客户端的状态信息，以及使用链表来构建客户端输出缓冲区

### 链表的结构

```c
typedef struct listNode {
    // 前置节点
    struct listNode * prev;
    // 后置节点
    struct listNode * next;
    // 节点的值
    void * value;
}listNode
```

多个listNode可以通过prev和next指针组成双端链表

![image-20221117202500348](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221117202500348.png)

```c
typedef struct list {
    // 表头节点
    listNode * head;
    // 表尾节点
    listNode * tail;
    // 链表所包含的节点数量
    unsigned long len;
    // 节点值复制函数
    void *(*dup)(void *ptr);
    // 节点值释放函数
    void (*free)(void *ptr);
    // 节点值对比函数
    int (*match)(void *ptr,void *key);
} list;
```

list结构为链表提供了表头指针head、表尾指针tail，以及链表长度计数器len，而dup、free和match成员则是用于实现多态链表所需的类型特定函数：

·dup函数用于复制链表节点所保存的值；

·free函数用于释放链表节点所保存的值；

·match函数则用于对比链表节点所保存的值和另一个输入值是否相等。

Redis的链表实现的特性：

·双端：链表节点带有prev和next指针，获取某个节点的前置节点和后置节点的复杂度都是O（1）。

·无环：表头节点的prev指针和表尾节点的next指针都指向NULL，对链表的访问以NULL为终点。

·带表头指针和表尾指针：通过list结构的head指针和tail指针，程序获取链表的表头节点和表尾节点的复杂度为O（1）。

·带链表长度计数器：程序使用list结构的len属性来对list持有的链表节点进行计数，程序获取链表中节点数量的复杂度为O（1）。

·多态：链表节点使用void*指针来保存节点值，并且可以通过list结构的dup、free、match三个属性为节点值设置类型特定函数，所以链表可以用于保存各种不同类型的值。

## 4. 压缩链表

压缩链表是Redis 为了**节约内存空间**使用，由一系列特殊编码的连续内存块组成的**顺序型**（sequential）数据结构。

zset 和 hash 容器（set（也是hash实现）和hash）**对象在元素个数较少，并且每个列表项要么就是小整数值，要么就是长度比较短的字符串的时候，采用压缩列表 (ziplist) 进行存储**。压缩列表是一块连续的内存空间，元素之间紧挨着存储，没有任何冗余空隙。

```java
127.0.0.1:6379> zadd programmings 1.0 go 2.0 python 3.0 java
(integer) 3
127.0.0.1:6379> OBJECT ENCODING programmings
"ziplist"
    
127.0.0.1:6379> HMSET runoobkey-one name "redis tutorial" description "redis basic commands for caching" likes 20 visitors 23000
OK
127.0.0.1:6379> OBJECT ENCODING runoobkey-one
"ziplist"
```

```c
struct ziplist<T> {
 int32 zlbytes; // 整个压缩列表占用字节数
 int32 zltail_offset; // 最后一个元素距离压缩列表起始位置的偏移量，用于快速定位到最后一个节点
 int16 zllength; // 元素个数
 T[] entries; // 元素内容列表，挨个挨个紧凑存储
 int8 zlend; // 标志压缩列表的结束，值恒为 0xFF
}
```

![image-20221118130204884](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118130204884.png)

压缩列表为了支持双向遍历，所以才会有 ztail_offset 这个字段，用来快速定位到最后一个元素，然后倒着遍历。

entry 块随着容纳的元素类型不同，也会有不一样的结构。

```c
struct entry {
 int<var> prevlen; // 前一个 entry 的字节长度
 int<var> encoding; // 元素类型编码
 optional byte[] content; // 元素内容
}
```

**它的 prevlen 字段表示前一个 entry 的字节长度**，当压缩列表倒着遍历时，需要通过这个字段来快速定位到下一个元素的位置。

prevlen 是一个变长的整数，**当字符串长度小于254(0xFE) 时，使用一个字节表示**；如果达到或超出 254(0xFE) 那就使用 5 个字节来表示。第一个字节是 0xFE(254)，剩余四个字节表示字符串长度。

encoding 字段存储了元素内容的编码类型信息，ziplist 通过这个字段来决定后面的content 内容的形式。

### 添加元素

因为 ziplist 都是紧凑存储，没有冗余空间 (对比一下 Redis 的字符串结构)。意味着插入一个新的元素就需要调用 realloc 扩展内存。取决于内存分配器算法和当前的 ziplist 内存大小，**realloc 可能会重新分配新的内存空间**，**并将之前的内容一次性拷贝到新的地址，也可能在原有的地址上进行扩展，这时就不需要进行旧内容的内存拷贝。**

如果 ziplist 占据内存太大，重新分配内存和拷贝内存就会有很大的消耗。所以 ziplist 不适合存储大型字符串，存储的元素也不宜过多。

### 级联更新

每个 entry 都会有一个 prevlen 字段存储前一个 entry 的长度。如果内容小于254 字节，prevlen 用 1 字节存储，否则就是 5 字节。这意味着如果某个 entry 经过了修改

操作从 253 字节变成了 254 字节，那么它的下一个 entry 的 prevlen 字段就要更新，从 1 个字节扩展到 5 个字节；如果这个 entry 的长度本来也是 253 字节，那么后面 entry 的prevlen 字段还得继续更新。

如果 ziplist 里面每个 entry 恰好都存储了 253 字节的内容，那么第一个 entry 内容的修改就会导致后续所有 entry 的级联更新，这就是一个比较耗费计算资源的操作。

## 5.小整数集合

当一个集合只包含整数值元素，并且这个集合的元素数量不多时，Redis就会使用整数集合作为集合键的底层实现。

Redis Sadd 命令将一个或多个成员元素加入到集合中，已经存在于集合的成员元素将被忽略。

```java
127.0.0.1:6379> SADD codeholes 1 2 3
(integer) 3
127.0.0.1:6379> OBJECT ENCODING codeholes
"intset"
    
如果添加的是字符串，不是整数，那么类型是hashtable
127.0.0.1:6379> sadd school zhangsan
(integer) 1
127.0.0.1:6379> OBJECT ENCODING school
"hashtable"
```

### 实现方式

![image-20221118132636945](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118132636945.png)

```c
struct intset<T> {
 int32 encoding; // 决定整数位宽是 16 位、32 位还是 64 位
 int32 length; // 元素个数
 int<T> contents; // 整数数组，可以是 16 位、32 位和 64 位
}
```

·整数集合是集合键的底层实现之一。

·整数集合的底层实现为数组，这个数组以有序、无重复的方式保存集合元素，在有需要时，程序会根据新添加元素的类型，改变这个数组的类型。

·升级操作为整数集合带来了操作上的灵活性，并且尽可能地节约了内存。

·整数集合只支持升级操作，不支持降级操作。

更多内容查看书籍：redis设计与实现。

## 6.快速列表

Redis 早期版本存储 list 列表数据结构使用的是压缩列表 ziplist 和普通的双向链表linkedlist，也就是元素少时用 ziplist，元素多时用 linkedlist。

目前list使用的是qucklist，即快速链表。

```java
127.0.0.1:6379> rpush name 1 2 3
(integer) 8
127.0.0.1:6379> OBJECT ENCODING name
"quicklist"
```

```java
// 链表的节点
struct listNode<T> {
 listNode* prev;
 listNode* next;
 T value;
}
// 链表
struct list {
 listNode *head;
 listNode *tail;
 long length;
}
```

链表的附加空间相对太高，prev 和 next 指针就要占去 16 个字节 (64bit 系统的指针是 8 个字节)，另外每个节点的内存都是单独分配，会加剧内存的碎片化，影响内存管理效率。后续版本对列表数据结构进行了改造，使用 quicklist 代替了 ziplist 和 linkedlist。

```c
127.0.0.1:6379> rpush name 1 2 3 4
(integer) 5
127.0.0.1:6379> OBJECT ENCODING name
"quicklist"
```

### 实现

![image-20221118133538643](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118133538643.png)

```c
struct ziplist<T> {
 int32 zlbytes; // 整个压缩列表占用字节数
 int32 zltail_offset; // 最后一个元素距离压缩列表起始位置的偏移量，用于快速定位到最后一个节点
 int16 zllength; // 元素个数
 T[] entries; // 元素内容列表，挨个挨个紧凑存储
 int8 zlend; // 标志压缩列表的结束，值恒为 0xFF
}
struct ziplist_compressed {
 int32 size;
 byte[] compressed_data;
}
struct quicklistNode {
 quicklistNode* prev;
 quicklistNode* next;
 ziplist* zl; // 指向压缩列表
 int32 size; // ziplist 的字节总数
 int16 count; // ziplist 中的元素数量
 int2 encoding; // 存储形式 2bit，原生字节数组还是 LZF 压缩存储
 ...
}
struct quicklist {
 quicklistNode* head;
 quicklistNode* tail;
 long count; // 元素总数
 int nodes; // ziplist 节点的个数
 int compressDepth; // LZF 算法压缩深度
 ...
}
```

为了进一步节约空间，Redis 还会对ziplist 进行压缩存储，使用 LZF 算法压缩，可以选择压缩深度。

### 每个ziplist存多少元素

quicklist 内部默认单个 ziplist 长度为 8k 字节，超出了这个字节数，就会新起一个ziplist。ziplist 的长度由配置参数 list-max-ziplist-size 决定。

> \# -5: max size: 64 Kb <-- not recommended for normal workloads
>
> \# -4: max size: 32 Kb <-- not recommended
>
> \# -3: max size: 16 Kb <-- probably not recommended
>
> \# -2: max size: 8 Kb <-- good
>
> \# -1: max size: 4 Kb <-- good

### 压缩深度

![image-20221118134300913](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118134300913.png)

quicklist 默认的压缩深度是 0，也就是不压缩。压缩的实际深度由配置参数 listcompress-depth 决定。为了支持快速的 push/pop 操作，quicklist 的首尾两个 ziplist 不压缩，此时深度就是 1。如果深度为 2，就表示 quicklist 的首尾第一个 ziplist 以及首尾第二个 ziplist 都不压缩。

### 性能对比

[Redis Quicklist - From a More Civilized Age (matt.sh)](https://matt.sh/redis-quicklist)

## 7.紧凑链表

### 结构实现

Redis 5.0 又引入了一个新的数据结构 listpack，它是对 ziplist 结构的改进，在存储空间上会更加节省，而且结构上也比 ziplist 要精简。

```c
struct listpack<T> {
 int32 total_bytes; // 占用的总字节数
 int16 size; // 元素个数
 T[] entries; // 紧凑排列的元素列表
 int8 end; // 同 zlend 一样，恒为 0xFF
}
```

![image-20221118134617413](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118134617413.png)

 

listpack 跟 ziplist 的结构几乎一摸一样，只是少了一个 zltail_offset 字段。ziplist 通过这个字段来定位出最后一个元素的位置，用于逆序遍历。

```c
struct lpentry {
 int<var> encoding;
 optional byte[] content;
 int<var> length;
}
```

元素的结构和 ziplist 的元素结构也很类似，都是包含三个字段。不同的是长度字段放在了元素的尾部，而且存储的不是上一个元素的长度，是当前元素的长度。正是因为长度放在了尾部，所以可以省去了 zltail_offset 字段来标记最后一个元素的位置，这个位置可以通过total_bytes 字段和最后一个元素的长度字段计算出来。

长度字段使用 varint 进行编码，不同于 skiplist 元素长度的编码为 1 个字节或者 5 个字节，listpack 元素长度的编码可以是 1、2、3、4、5 个字节。

Redis 为了让 listpack 元素支持很多类型，它对 encoding 字段也进行了较为复杂的设计

### 级联更新

listpack 的设计彻底消灭了 ziplist 存在的级联更新行为，元素与元素之间完全独立，不会因为一个元素的长度变长就导致后续的元素内容会受到影响。

### 取代ziplist

listpack 的设计的目的是用来取代 ziplist，不过当下还没有做好替换 ziplist 的准备，因为有很多兼容性的问题需要考虑，ziplist 在 Redis 数据结构中使用太广泛了，替换起来复杂度会非常之高。它目前只使用在了新增加的 Stream 数据结构中。

## 8.跳表

[Redis内部数据结构详解(6)——skiplist - 铁蕾的个人博客 (zhangtielei.com)](http://zhangtielei.com/posts/blog-redis-skiplist.html)

[数据结构与算法——跳表 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/68516038)

Redis 的 zset 是一个复合结构，一方面它需要一个 hash 结构来存储 value 和 score 的对应关系，另一方面需要提供按照 score 来排序的功能，还需要能够指定 score 的范围来获取 value 列表的功能，这就需要另外一个结构「跳跃列表」。

zset 的内部实现是一个 hash 字典加一个跳跃列表 (skiplist)。

![image-20221118104404064](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118104404064.png)

![image-20221118104507501](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118104507501.png)

Redis 的跳跃表共有 64 层，意味着最

多可以容纳 2^64 次方个元素。

### 结构

```c
节点
struct zslnode {
 string value;
 double score;
 zslnode*[] forwards; // 多层连接指针
 zslnode* backward; // 回溯指针
}
链表
struct zsl {
 zslnode* header; // 跳跃列表头指针
 int maxLevel; // 跳跃列表当前的最高层
 map<string, zslnode*> ht; // hash 结构的所有键值对
}
```

每一个 kv 块对应着一个zslnode 结构，kv header 也是这个结构，只不过 value 字段是 null 值——无效的。

score 是Double.MIN_VALUE，用来垫底的。

kv 之间使用指针串起来形成了双向链表结构，它们是有序 排列的，从小到大。不同的 kv 层高可能不一样，层数越高的 kv 越少。同一层的 kv 会使用指针串起来。每一个层元素的遍历都是从 kv header 出发。

### 查找过程

如果跳表只有一层，需要挨个遍历，复杂度为O（n）

跳跃列表有了多层结构之后，这个定位的算法复杂度将会降到O(lg(n))。

![image-20221118110235865](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118110235865.png)

我们要定位到那个紫色的 kv，需要从 header 的最高层开始遍历找到第一个节点 (最后一个比「我」小的元素)，然后从这个节点开始降一层再遍历找到第二个节点 (最

后一个比「我」小的元素)，然后一直降到最底层进行遍历就找到了期望的节点 (最底层的最后一个比我「小」的元素)

这样，就可以快速找到要查找的元素。

### 添加元素

插入的时候，需要定位到位置，然后插入元素，但是新插入的节点，有多少层，需要使用算法分配。跳跃列表使用的是随机算法。

对于每一个新插入的节点，都需要调用一个随机算法给它分配一个合理的层数。直观上期望的目标是 50% 的 Level1，25% 的 Level2，12.5% 的 Level3，一直到最顶层 2^-63，因为这里每一层的晋升概率是 50%。每个跳跃表节点的层高都是1至32之间的随机数。

跳跃列表会记录一下当前的最高层数 maxLevel，遍历时从这个 maxLevel 开始遍历性能就会提高很多。

首先我们在搜索合适插入点的过程中将「搜索路径」摸出来了，然后就可以开始创建新节点了，创建的时候需要给这个节点随机分配一个层数，再将搜索路径上的节点和这个新节点通过前向后向指针串起来。如果分配的新节点的高度高于当前跳跃列表的最大高度，就需要更新一下跳跃列表的最大高度。

![zset](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/zset.png)

### 删除元素

删除过程和插入过程类似，都需先把这个「搜索路径」找出来。然后对于每个层的相关节点都重排一下前向后向指针就可以了。同时还要注意更新一下最高层数 maxLevel。

### 更新元素

我们调用 zadd 方法时，如果对应的 value 不存在，那就是插入过程。如果这个value 已经存在了，只是调整一下 score 的值，那就需要走一个更新的流程。

更新策略：

1. 假设这个新的score 值不会带来排序位置上的改变，那么就不需要调整位置，直接修改元素的 score 值就可以了。但是如果排序位置改变了，那就要调整位置。
2. score发生改变，位置也改变，删除key，再新增。

### score值都一样

 在一个极端的情况下，zset 中所有的 score 值都是一样的，zset 的查找性能会退化为O(n)，zset 的排序元素不只看 score 值，如果score 值相同还需要再比较 value 值 (字符串比较)

### 计算排名

Redis Zrank 返回有序集中指定成员的排名。其中有序集成员按分数值递增(从小到大)顺序排列。

```sql
//添加元素
ZADD runkey 1 redis 2 mysql 3 java 4 go
//获取排名
ZRANK runkey redis
//获取所有排名a
ZRANGE runoobkey 0 -1 WITHSCORES
```

Redis 在 skiplist 的 forward 指针上进行了优化，给每一个 forward 指针都增加了 span 属性，span 是「跨度」的意思，表示从前一个节点沿着当前层的 forward 指针跳到当前这个节点中间会跳过多少个节点。Redis 在插入删除操作时会更新 span 值的大小。

当我们要计算一个元素的排名时，只需要将「搜索路径」上的经过的所有节点的跨度 span 值进行叠加就可以算出元素的最终 rank 值

```c
typedef struct zskiplistNode {
    // 层
    struct zskiplistLevel {
        // 前进指针
        struct zskiplistNode *forward;
        // 跨度
        unsigned int span;
    } level[];
    // 后退指针
    struct zskiplistNode *backward;
    // 分值
    double score;
    // 成员对象
    robj *obj;
} zskiplistNode;
```

level是一个数组，记录了当前节点拥有的所有层级节点。

![image-20221118114717889](https://mynotepicbed.oss-cn-beijing.aliyuncs.com/img/image-20221118114717889.png)

更详细的查看书籍：redis设计与实现。

## 总结

各数据类型使用结构

1. String：SDS
2. List：
   1. 存储整数使用的是intset。
   2. 存储其他数据在3.2之前，如果是整数或者是小字符串，使用ziplist，如果数据多使用的是普通链表。3.2之后使用的是快速链表
3. hash：数据+链表。链表规则和list一样
4. set：底层实现是hash，和hash保持一致
5. zset：hash+跳表
6. stream：实现是紧凑链表



