package com.hj.hjBi.bismq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

import static com.hj.hjBi.constant.BiMqConstant.*;

/**
 * @author: WHJ
 * @createTime: 2023-09-18 09:45
 * @description:
 */
public class BiMqInit {
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 基础信息
        factory.setHost(BI_MQ_HOST);
//            factory.setUsername(BI_MQ_USERNAME);
//            factory.setHost(BI_MQ_PASSWORD);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 声明交换机
        channel.exchangeDeclare(BI_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(BI_DLX_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // 声明BI分析队列并绑定死信交换机
        Map<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", BI_DLX_EXCHANGE_NAME);
        map.put("x-dead-letter-routing-key", BI_DLX_ROUTING_KEY);
        channel.queueDeclare(BI_QUEUE, true, false, false, map);
        channel.queueBind(BI_QUEUE, BI_EXCHANGE, BI_ROUTINGKEY);

        channel.queueDeclare(BI_DLX_QUEUE_NAME, true, false, false, null);
        channel.queueBind(BI_DLX_QUEUE_NAME, BI_DLX_EXCHANGE_NAME, BI_DLX_ROUTING_KEY);

    }
}
