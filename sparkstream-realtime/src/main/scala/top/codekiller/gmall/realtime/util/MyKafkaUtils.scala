package top.codekiller.gmall.realtime.util

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

import java.util
import scala.collection.mutable

/*
Kafka工具类，用于生产数据和消费数据
 */
object MyKafkaUtils {

  /**
   * 消费者配置
   */
  private val consumerConfigs: mutable.Map[String,Object] = mutable.Map[String,Object](
    //kafka集群配置
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> MyPropsUtils(MyConfig.KAFKA_BOOTSTRAP_SERVERS),
    // kv反序列化器
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
    // groupId
    // offset提交  自动 手动
    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "true",
    //自动提交的时间间隔
    //ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG
    // offset重置  "latest"  "earliest"
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest"
    // .....
  )

  /**
   * 基于SparkStreaming消费，获取到KafkaDStream
   * @param ssc
   * @param topic
   * @param groupId
   * @return
   */
  def getKafkaDStream(ssc : StreamingContext,topic: String,groupId : String) ={
    consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG , groupId)

    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](Array(topic),consumerConfigs))

    kafkaDStream
  }

  /**
   * 基于SparkStreaming消费 ,获取到KafkaDStream , 使用指定的offset
   */
  def getKafkaDStream(ssc : StreamingContext , topic: String  , groupId:String ,  offsets: Map[TopicPartition, Long]  ) ={
    consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG , groupId)

    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array(topic), consumerConfigs , offsets))
    kafkaDStream
  }

  var producer : KafkaProducer[String,String] = createProducer()

  /**
   * 创建生产者对象
   */
  def createProducer():KafkaProducer[String,String] = {
    val producerConfigs : util.HashMap[String, AnyRef] = new util.HashMap[String,AnyRef]
    //生产者配置类 ProducerConfig
    //kafka集群位置
    //producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"hadoop102:9092,hadoop103:9092,hadoop104:9092")
    //producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,MyPropsUtils("kafka.bootstrap-servers"))
    producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,MyPropsUtils(MyConfig.KAFKA_BOOTSTRAP_SERVERS))
    //kv序列化器
    producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG , "org.apache.kafka.common.serialization.StringSerializer")
    producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG , "org.apache.kafka.common.serialization.StringSerializer")
    //acks
    producerConfigs.put(ProducerConfig.ACKS_CONFIG , "all")
    //batch.size  16kb
    //linger.ms   0
    //retries
    //幂等配置
    producerConfigs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG , "true")

    val producer: KafkaProducer[String, String] = new KafkaProducer[String,String](producerConfigs)
    producer
  }

  /**
   * 生产（按照key进行分区）
   * @param topic
   * @param key
   * @param msg
   */
  def send(topic:String,key:String,msg :String): Unit ={
    producer.send(new ProducerRecord[String,String](topic,key,msg))
  }

  /**
   * 生产（按照默认的黏性分区策略）
   */
  def send(topic : String  , msg : String ):Unit = {
    producer.send(new ProducerRecord[String,String](topic , msg ))
  }

  /**
   * 刷写 ，将缓冲区的数据刷写到磁盘
   *
   */
  def flush(): Unit ={
    producer.flush()
  }

}
