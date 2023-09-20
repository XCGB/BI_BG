package com.hj.hjBi.mq;

import com.rabbitmq.client.*;

public class TopicConsumer {

    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();
        Channel channel3 = connection.createChannel();

        channel1.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName1 = "fronted_queue";
        channel1.queueDeclare(queueName1, true, false, false, null);
        channel1.queueBind(queueName1, EXCHANGE_NAME, "#.前端.#");
        channel2.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName2 = "background_queue";
        channel2.queueDeclare(queueName2, true, false, false, null);
        channel2.queueBind(queueName2, EXCHANGE_NAME, "#.后端.#");
        channel3.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName3 = "produce_queue";
        channel3.queueDeclare(queueName3, true, false, false, null);
        channel3.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [前端队列] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [后端队列] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [产品队列] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {
        });
        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });
        channel3.basicConsume(queueName3, true, deliverCallback3, consumerTag -> {
        });
    }
}