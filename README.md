# hmall-data-middleware
# hmall数据中间件
    项目需要，管理员端使用Oracle数据库，客户端使用Redis数据库。所以采用Kafka担当数据中间件。
    然后因为项目需要会有两个Kafka服务器集群，两个Kafka集群中间使用mirrormaker进行同步。
    本教程会介绍kafka集群的使用与集群到数据库的接口使用。

## Chapter 0
### Kafka集群的搭建 

<https://github.com/ettwz/kafka-redis>

使用github把上述项目克隆到本地。

#### Linux：
1. 开启zookeeper服务器。
   进入 kafka_2.10-0.10.1.0 目录，打开终端使用
   `bin/zookeeper-server-start.sh config/zookeeper.properties`
   指令开启服务器。
   如需要在本地开启多个zookeeper服务器请使用不同的配置文件。本地开启多服务器需修改端口、日志目录。参考zookeeper-1.properties文件。

2. 开启kafka服务器。 一个kafka集群可以有多个kafka服务器，但是zookeeper服务器只能有一个。
   进入 kafka_2.10-0.10.1.0 目录，打开终端使用
   `bin/kafka-server-start.sh config/server.properties`
   指令开启服务器。
   如需要在本地开启多个kafka服务器请使用不同的配置文件。本地开启多服务器需修改listeners、broker.id、log.dirs。参考server-1.properties等文件。

#### Windows：
使用流程与Linux大致相同。指令使用方法不太相同，脚本请进入`kafka_2.10-0.10.1.0\bin\windows`进行使用。.sh脚本在windows不能使用，要使用.bat脚本。还需要把配置文件复制进`kafka_2.10-0.10.1.0\bin\windows`文件夹，因为windows执行脚本的配置文件不能带路径。例：`kafka-server-start.bat server.properties`

## Chapter 1
### Kafka集群的使用
`bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test`
在2181端口的zookeeper服务器对应的kafka集群上创建名为test的话题。

`bin/kafka-topics.sh --list --zookeeper localhost:2181`
查看2181端口的zookeeper服务器对应的kafka集群上的话题列表。

`bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test`
在9092端口的kafka服务器上启动以test为话题的消息发送窗口

`bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning`
在9092端口的kafka服务器上启动以test为话题的消息接收窗口


如需在windows上使用参照上一章节把Linux指令切换成windows指令进行使用。

### MirrorMaker的使用
Mirrormaker的配置文件参照kafka_2.10-0.10.1.0下的sourceClusterConsumer.config、targetClusterProducer.config文件来对mirrormaker的源和目的进行配置。

`bin/kafka-run-class.sh kafka.tools.MirrorMaker --consumer.config sourceClusterConsumer.config --num.streams 2 --producer.config targetClusterProducer.config --whitelist=".*"`
//whitelist参数是对白名单的配置，现在的测试环境应该写"test8"

使用上述指令开启mirrormeker服务，白名单可以对通过mirrormaker的话题进行过滤。如果需要集群间互相同步请使用不同话题然后使用白名单进行过滤。

### Redis的使用
进入redis-3.2.5文件夹，使用redis-server redis.conf指令使用自己的配置文件启动服务。

## Chapter 1.1
### 远程使用注意事项
在局域网内远程使用其他主机（服务器）上开启的Kafka服务，需要在启动服务的主机（服务器）上把server.properties配置文件中
`advertised.listeners=PLAINTEXT://10.211.97.247:9092`
这一行启用，地址写成启动服务的主机的ip地址。

而`listeners=PLAINTEXT://0.0.0.0:9092`
这一行中的ip地址必须使用0.0.0.0以便访问服务的机器进行寻址。
然后把目标主机的ip地址与主机名的映射加入自己主机的host文件中。因为kafka服务是通过主机名寻址。

在局域网内远程使用其他主机（服务器）上开启的Redis服务，需要修改启动服务的主机的redis.conf配置文件。
`#bind 127.0.0.1`
这一行一定要加#注释掉
`protected-mode no`
这一行要设定为no

## Chapter 2.1
### 模拟向redis数据库中插入数据
###redissendclient
redissendclient 程序是一个模拟向redis数据库中插入数据的程序，在插入数据的同时使用redis广播机制发送广播（内容为对数据库进行的操作与数据）。

Redissendclient程序是一个Redis数据库操作的简单实例，在董凡的BaseDao基础上加上了Redis的订阅功能，每次对数据库进行更改时会在固定频道发送一条更改信息，以便客户端进行解析、转发。

#### 使用方法：
`mvn clean spring-boot:run`

在网页中输入

- <http://localhost:2229/add/>		进行数据添加
  - <http://localhost:2229/update/>	进行数据更新
  - <http://localhost:2229/delete/{id}>进行数据删除

配置信息位于application.properties中


## Chapter 2.2
### redis接收广播并由Kafka生产者转发
### rdssub-kfkprod
这个是Redis在发生更改时向Kafka同步消息的实例。


rdssub-kfkprod程序是一个Redis客户端和Kafka生产者端融合的程序，程序启动以后会进行Redis指定频道的监听，在监听到信息以后会把收到的信息存成一个变量然后新建一个Kafka生产者线程把接收到的变量转发出去。

使用方法：`mvn clean spring-boot:run` <br>
在网页中输入
<http://localhost:8080/cli/>	进行端口监听

配置信息位于 com.hand.kafka.producer.kafkaProducer 和 com.hand.redis.Constants 中。

## Chapter 2.3
### kafka接收消息并操作数据库
这部分分为两个小部分：
1. kafka接收并操作redis
2. kafka接收并操作oracle

### Chapter 2.3.1
#### kafka接收并操作redis
#### kafka2redis
kafka2redis程序是一个kafka消费者接收消息并解析成数据库操作，再通过jedis操作redis数据库的程序。程序运行后会自动打开kafka消费者监听对应topic的消息队列，接收到消息后再操作数据库。

使用方法：`mvn clean spring-boot:run` <br>
在网页中输入
<http://localhost:2230/add/>	进行端口监听

redis配置位于resources/application.properties中。kafka配置位于com/hand/config/config.java中。

### Chapter 2.3.2
#### kafka接收并操作oracle
#### kafka2oracle
kafka2oracle程序是一个kafka消费者接收消息并解析成数据库操作，再通过spring-data-jpa操作oracle数据库的程序。程序运行后会自动打开kafka消费者监听对应topic的消息队列，接收到消息后再操作数据库。

使用方法：`mvn clean spring-boot:run` <br>
在网页中输入
<http://localhost:2231/add/>	进行端口监听

oracle配置位于resources/application.properties中。kafka配置位于com/hand/config/config.java中。



## Chapter 2.4

###  上述服务在docker上的部署

