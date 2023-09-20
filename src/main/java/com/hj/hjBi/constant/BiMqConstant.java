package com.hj.hjBi.constant;

/**
 * @author: WHJ
 * @createTime: 2023-09-18 10:16
 * @description:
 */
public interface BiMqConstant {
    String BI_EXCHANGE = "bi_exchange";
    String BI_QUEUE = "bi_queue";
    String BI_ROUTINGKEY = "bi_routingKey";
    String BI_MQ_HOST = "localhost";
//    String BI_MQ_USERNAME = "guest";
//    String BI_MQ_PASSWORD = "guest";

    /**
     * 死信队列交换机
     */
    String BI_DLX_EXCHANGE_NAME = "bi-dlx-exchange";

    /**
     * 死信队列
     */
    String BI_DLX_QUEUE_NAME = "bi_dlx_queue";

    /**
     * 死信队列路由键
     */
    String BI_DLX_ROUTING_KEY = "bi_dlx_routingKey";
}
