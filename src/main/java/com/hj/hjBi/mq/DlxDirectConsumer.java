package com.hj.hjBi.mq;

import com.rabbitmq.client.*;

import java.util.HashMap;
import java.util.Map;

public class DlxDirectConsumer {

    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_logs";
    private static final String WORK_EXCHANGE = "work_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Channel channel1 = connection.createChannel();
        channel.exchangeDeclare(WORK_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel1.exchangeDeclare(WORK_EXCHANGE, BuiltinExchangeType.DIRECT);



        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", "waibao");
        String queueName = "xiaodog_queue";
        channel.queueDeclare(queueName, false, false, false, args);
        channel.queueBind(queueName, WORK_EXCHANGE, "xiaodog");

        HashMap<String, Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key", "laoban");
        String queueName1 = "xiaodcat_queue";
        channel.queueDeclare(queueName1, false, false, false, args);
        channel1.queueBind(queueName1, WORK_EXCHANGE, "xiaodcat");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false, false);
            System.out.println(" [xiaodog] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel1.basicNack(delivery.getEnvelope().getDeliveryTag(),false, false);
            System.out.println(" [xiaodcat] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
        });
        channel1.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
        });
    }
}