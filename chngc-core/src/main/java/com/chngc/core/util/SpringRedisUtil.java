package com.chngc.core.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 *
 * @FileName: RedisUtil
 * @Version: 1.0
 * @Author: yanpeng
 * @Date: 2015/11/5 16:27
 */
public class SpringRedisUtil {

    private static Logger logger = LogManager.getLogger(SpringRedisUtil.class);

    /**
     * 加锁
     *
     * @param _key     key
     * @param _timeout 超时时间 必须设置
     * @return
     */
    public static boolean lock(RedisTemplate redisTemplate, String _key, long _timeout, TimeUnit _unit) {

        boolean lockFlg = false;

        final String key = _key;
        final long timeout = _timeout;
        final TimeUnit unit = _unit;

        boolean result = (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                long nano = System.nanoTime();

                try{
                    byte[] keyBytes = key.getBytes();
                    do {
                        Boolean setNx = connection.setNX(keyBytes, keyBytes);
                        if (setNx != null && setNx) {
                            connection.expire(key.getBytes(), unit.convert(timeout, unit));
                            return Boolean.TRUE;
                        }

                        if (timeout == 0) {
                            break;
                        }
                        Thread.sleep(300);

                    }  while ((System.nanoTime() - nano) < unit.toNanos(timeout));

                    return Boolean.FALSE;
                } catch (Exception e){
                    logger.error("加锁异常", e);
                    return Boolean.FALSE;
                }
            }
        });

        return result;
    }

    /**
     * 解锁/删除key
     * @param _key
     */
    public static void unlock(RedisTemplate redisTemplate, String _key) {

        final String key = _key;

        redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try{
                    byte[] keyBytes = key.getBytes();
                    connection.del(keyBytes);
                    return Boolean.TRUE;
                } catch (Exception e){
                    logger.error("加锁异常", e);
                    return Boolean.FALSE;
                }
            }
        });
    }
}
