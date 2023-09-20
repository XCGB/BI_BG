package com.hj.hjBi.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: WHJ
 * @createTime: 2023-09-13 10:46
 * @description: BI 返回结果
 */
@Data
public class BiResponseVO implements Serializable {
    private static final long serialVersionUID = -68281035579845169L;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 图表ID
     */
    private Long chartId;

}
