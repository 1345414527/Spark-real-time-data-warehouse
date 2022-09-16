package top.codekiller.gmall.realtime.util

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
 * Redis工具类，用于获取redis连接，操作redis
 */
object MyRedisUtils {

  var jedisPool : JedisPool = null

  def getJedisFromPool(): Jedis = {
    if (jedisPool == null){
      //创建连接池对象
      val jedisPoolConfig = new JedisPoolConfig()
      jedisPoolConfig.setMaxTotal(100) //最大连接数
      jedisPoolConfig.setMaxIdle(20) //最大空闲
      jedisPoolConfig.setMinIdle(20) //最小空闲
      jedisPoolConfig.setBlockWhenExhausted(true) //忙碌时是否等待
      jedisPoolConfig.setMaxWaitMillis(5000) //忙碌时等待时长 毫秒
      jedisPoolConfig.setTestOnBorrow(true) //每次获得连接的进行测试

      val host:String = MyPropsUtils(MyConfig.REDIS_HOST)
      val port:String = MyPropsUtils(MyConfig.REDIS_PORT)
      val pwd:String = MyPropsUtils(MyConfig.REDIS_PWD)

      jedisPool = new JedisPool(jedisPoolConfig,host,port.toInt,5000,pwd)
    }
    jedisPool.getResource
  }

}
