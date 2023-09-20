package com.hj.hjBi.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: WHJ
 * @createTime: 2023-09-15 10:19
 * @description:
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {

    private Integer database;
    private Integer port;
    private String host;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s",host,port);

        config.useSingleServer()
                .setDatabase(database)
                .setAddress(redisAddress);
        RedissonClient redisson = Redisson.create(config);
        return redisson;

//        config.useClusterServers()
//                // use "rediss://" for SSL connection
//                .addNodeAddress("redis://127.0.0.1:7181");
    }


}
