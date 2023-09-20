package com.hj.hjBi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-09-12 14:17
 * @description:
 */
@Data
public class GenChartByAiRequest implements Serializable {


    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;
}