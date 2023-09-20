package com.hj.hjBi.mq;

import com.rabbitmq.client.*;

public class FanoutCosumer {
    private static final String EXCHANGE_NAME = "fanout-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();

        String queueName1 = "wang_queue";
        channel1.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel1.queueDeclare(queueName1, true, false, false, null);
        channel1.queueBind(queueName1, EXCHANGE_NAME, "");

        String queueName2 = "zhang_queue";
        channel2.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel2.queueDeclare(queueName2, true, false, false, null);
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
              System.out.println(" [小王队列] Received '" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小张队列] Received '" + message + "'");
        };
        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });
        channel1.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
    }
}