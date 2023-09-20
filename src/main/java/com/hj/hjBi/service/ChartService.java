package com.hj.hjBi.service;

import com.hj.hjBi.model.dto.chart.GenChartByAiRequest;
import com.hj.hjBi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.hjBi.model.vo.BiResponseVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author WHJ
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-09-11 15:07:56
*/
public interface ChartService extends IService<Chart> {

    BiResponseVO genChartByAi(MultipartFile multipartFile, HttpServletRequest request, GenChartByAiRequest genChartByAiRequest);

    BiResponseVO genChartByAiAsync(MultipartFile multipartFile, HttpServletRequest request, GenChartByAiRequest genChartByAiRequest);

    BiResponseVO genChartByAiAsyncMq(MultipartFile multipartFile, HttpServletRequest request, GenChartByAiRequest genChartByAiRequest);

    void handleChartUpdateError(long chartId, String execMessage);
}
