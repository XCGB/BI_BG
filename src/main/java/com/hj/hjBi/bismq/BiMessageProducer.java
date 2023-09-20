package com.hj.hjBi.bismq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hj.hjBi.constant.BiMqConstant.BI_EXCHANGE;
import static com.hj.hjBi.constant.BiMqConstant.BI_ROUTINGKEY;

/**
 * @author: WHJ
 * @createTime: 2023-09-18 09:22
 * @description:
 */
@Component
public class BiMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BI_EXCHANGE, BI_ROUTINGKEY, message);
    }
}
