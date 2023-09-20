package com.hj.hjBi.bismq;

import com.hj.hjBi.common.ErrorCode;
import com.hj.hjBi.exception.BusinessException;
import com.hj.hjBi.manager.AiManager;
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

import static com.hj.hjBi.constant.BiMqConstant.BI_QUEUE;
import static com.hj.hjBi.constant.ChartConstant.BI_MODEL_ID;
import static com.hj.hjBi.constant.ChartConstant.GEN_ITEM_NUM;

/**
 * @author: WHJ
 * @createTime: 2023-09-18 09:24
 * @description:
 */
@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {BI_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("bi_queue receive message : {}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        // 获取到图表的id
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表为空");
        }
        // 等待-->执行中--> 成功/失败
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
        boolean updateChartById = chartService.updateById(updateChart);
        if (!updateChartById) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
            chartService.updateById(updateChartFailed);
            chartService.handleChartUpdateError(chart.getId(), "更新图表·执行中状态·失败");
            return;
        }
        // 调用AI
        String aiResult = aiManager.doChat(BI_MODEL_ID, buildUserInput(chart));
        String[] splits = aiResult.split("【【【");
        if (splits.length < GEN_ITEM_NUM) {
            // 将消息拒绝
            channel.basicNack(deliveryTag, false, false);
            chartService.handleChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }
        String genChart = splits[1];
        String genResult = splits[2];

        // 生成的最终结果-成功
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            channel.basicNack(deliveryTag, false, false);
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chart.getId());
            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
            chartService.updateById(updateChartFailed);
            chartService.handleChartUpdateError(chart.getId(), "更新图表·成功状态·失败");
        }
        // 成功，则确认消息
        channel.basicAck(deliveryTag, false);
    }

    private String buildUserInput (Chart chart) {
        String goal = chart.getGoal();
        String csvData = chart.getChartData();
        String chartType = chart.getChartType();
        // 用户输入
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n").append(userGoal).append("\n");
        // 压缩数据
        userInput.append("原始数据:").append("\n").append(csvData).append("\n");
        return userInput.toString();
    }

}
