package com.hj.hjBi.bismq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author: WHJ
 * @createTime: 2023-09-18 10:00
 * @description:
 */
@SpringBootTest
class BiMessageProducerTest {
    @Resource
    private BiMessageProducer biMessageProducer;

    @Test
    void sendMessage() {
        biMessageProducer.sendMessage("你好，这里是测试程序");
    }
}