package com.hj.hjBi.mq;

import com.rabbitmq.client.*;

import java.util.Scanner;

public class DlxDirectProducer {

    private static final String DEAD_EXCHANGE_NAME = "dlx_direct_logs";
    private static final String WORK_EXCHANGE_NAME = "work_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明死信交换机队列
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            // 声明死信队列
            String queueName = "dlx_laoban_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, DEAD_EXCHANGE_NAME, "laoban");
            String queueName2 = "dlx_waibao_queue";
            channel.queueDeclare(queueName2, true, false, false, null);
            channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false, false);
                System.out.println(" [laoban] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };
            DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false, false);
                System.out.println(" [waibao] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
            });
            channel.basicConsume(queueName2, false, deliverCallback1, consumerTag -> {
            });

            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
                String userInput = sc.nextLine();
                String[] strings = userInput.split(" ");
                if (strings.length < 1) {
                    continue;
                }
                String message = strings[0];
                String routeingKey = strings[1];
                channel.basicPublish(WORK_EXCHANGE_NAME, routeingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "with routing:" + routeingKey + "'");
            }

        }
    }
}