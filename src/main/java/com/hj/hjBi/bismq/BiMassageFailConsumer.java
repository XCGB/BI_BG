package com.hj.hjBi.bismq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hj.hjBi.common.ErrorCode;
import com.hj.hjBi.exception.BusinessException;
import com.hj.hjBi.model.entity.Chart;
import com.hj.hjBi.model.enums.ChartStatusEnum;
import com.hj.hjBi.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hj.hjBi.constant.BiMqConstant.BI_DLX_QUEUE_NAME;

/**
 * @author: WHJ
 * @createTime: 2023-09-18 12:29
 * @description:
 */
@Component
@Slf4j
public class BiMassageFailConsumer {

    @Resource
    private ChartService chartService;


    /**
     * 监听死信队列
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {BI_DLX_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 接收到失败的信息，
        log.info("dead_queue receive message : {}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表不存在");
        }
        // 把图表标为失败
        chart.setChartStatus(ChartStatusEnum.FAILED.getValue());
        boolean updateById = chartService.updateById(chart);
        if (!updateById) {
            log.info("处理死信队列消息失败,失败图表id:{}", chart.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 确认消息
        channel.basicAck(deliveryTag, false);
    }

}
