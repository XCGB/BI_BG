package com.hj.hjBi.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.hjBi.bismq.BiMessageProducer;
import com.hj.hjBi.common.ErrorCode;
import com.hj.hjBi.exception.BusinessException;
import com.hj.hjBi.exception.ThrowUtils;
import com.hj.hjBi.manager.AiManager;
import com.hj.hjBi.manager.RedisLimiterManager;
import com.hj.hjBi.model.dto.chart.GenChartByAiRequest;
import com.hj.hjBi.model.entity.Chart;
import com.hj.hjBi.model.entity.User;
import com.hj.hjBi.model.enums.ChartStatusEnum;
import com.hj.hjBi.model.vo.BiResponseVO;
import com.hj.hjBi.service.ChartService;
import com.hj.hjBi.mapper.ChartMapper;
import com.hj.hjBi.service.UserService;
import com.hj.hjBi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.hj.hjBi.constant.ChartConstant.*;
import static com.hj.hjBi.constant.RedisConstant.RATE_LIMIT_PREFIX;

/**
* @author WHJ
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-09-11 15:07:56
*/
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{
    final List<String> valdFileSuffixList = Arrays.asList("xlsx","xls");

    @Resource
    private AiManager aiManager;

    @Resource
    private UserService userService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private BiMessageProducer biMessageProducer;

    /**
     * 图表生成（同步）
     *
     * @param multipartFile       用户上传的文件信息
     * @param genChartByAiRequest 用户的需求
     * @param request             http request
     * @return
     */
    @Override
    public BiResponseVO genChartByAi(MultipartFile multipartFile, HttpServletRequest request, GenChartByAiRequest genChartByAiRequest) {

        String chartName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);
        checkParameter(chartName, goal, chartType, loginUser);
        checkFile(multipartFile);
        Long currentUserId = loginUser.getId();

        // 限流判断
        redisLimiterManager.doRateLimit(RATE_LIMIT_PREFIX + currentUserId);

        // 用户输入
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n").append(userGoal).append("\n");
        // 压缩数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据:").append("\n").append(csvData).append("\n");

        String aiResult = aiManager.doChat(BI_MODEL_ID, userInput.toString());
        String[] splits = aiResult.split("【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        String genChart = splits[1];
        String genResult = splits[2];

        Chart chart = new Chart();
        chart.setName(chartName);
        chart.setGoal(goal);
        chart.setUserId(currentUserId);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setGenChart(genChart);
        biResponseVO.setGenResult(genResult);
        biResponseVO.setChartId(chart.getId());
        return biResponseVO;
    }

    /**
     * 异步图表生成-线程池
     *
     * @param multipartFile       用户上传的文件信息
     * @param request             http request
     * @param genChartByAiRequest 用户的需求
     * @return
     */
    @Override
    public BiResponseVO genChartByAiAsync(MultipartFile multipartFile, HttpServletRequest request, GenChartByAiRequest genChartByAiRequest) {
        String chartName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);
        checkParameter(chartName, goal, chartType, loginUser);
        checkFile(multipartFile);
        Long currentUserId = loginUser.getId();
        // 限流判断
        redisLimiterManager.doRateLimit(RATE_LIMIT_PREFIX + currentUserId);

        // 用户输入
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n").append(userGoal).append("\n");
        // 压缩数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据:").append("\n").append(csvData).append("\n");

        Chart chart = new Chart();
        chart.setName(chartName);
        chart.setGoal(goal);
        chart.setUserId(currentUserId);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        CompletableFuture completableFuture = CompletableFuture.runAsync(() -> {
            // 等待-->执行中--> 成功/失败
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
            boolean updateChartById = this.updateById(updateChart);
            if (!updateChartById) {
                Chart updateChartFailed = new Chart();
                updateChartFailed.setId(chart.getId());
                updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
                this.updateById(updateChartFailed);
                handleChartUpdateError(chart.getId(), "更新图表·执行中状态·失败");
            }
            String aiResult = aiManager.doChat(BI_MODEL_ID, userInput.toString());
            String[] splits = aiResult.split("【【【");
            if (splits.length < GEN_ITEM_NUM) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
            }
            String genChart = splits[1];
            String genResult = splits[2];

            // 生成的最终结果-成功
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
            boolean updateResult = this.updateById(updateChartResult);
            if (!updateResult) {
                Chart updateChartFailed = new Chart();
                updateChartFailed.setId(chart.getId());
                updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
                this.updateById(updateChartFailed);
                handleChartUpdateError(chart.getId(), "更新图表·成功状态·失败");
            }
        }, threadPoolExecutor);

//        // 等待太久了，抛异常，超时时间
//        try {
//            completableFuture.get(60, TimeUnit.SECONDS);
//        } catch (Exception e) {
//            // 超时失败了
//            Chart updateChartFailed = new Chart();
//            updateChartFailed.setId(chart.getId());
//            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
//            this.updateById(updateChartFailed);
//            log.info("AI生成超时");
//            throw new RuntimeException(e);
//        }

        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setChartId(chart.getId());

        return biResponseVO;
    }

    /**
     * 异步生成图表（MQ）
     * @param multipartFile
     * @param request
     * @param genChartByAiRequest
     * @return
     */
    @Override
    public BiResponseVO genChartByAiAsyncMq(MultipartFile multipartFile, HttpServletRequest request, GenChartByAiRequest genChartByAiRequest) {
        String chartName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);
        checkParameter(chartName, goal, chartType, loginUser);
        checkFile(multipartFile);
        Long currentUserId = loginUser.getId();
        // 限流判断
        redisLimiterManager.doRateLimit(RATE_LIMIT_PREFIX + currentUserId);

        Chart chart = new Chart();
        chart.setName(chartName);
        chart.setGoal(goal);
        chart.setUserId(currentUserId);
        chart.setChartData(ExcelUtils.excelToCsv(multipartFile));
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        long chartId =  chart.getId();
        biMessageProducer.sendMessage(String.valueOf(chartId));

        BiResponseVO biResponseVO = new BiResponseVO();
        biResponseVO.setChartId(chartId);

        return biResponseVO;
    }


    private void checkParameter (String chartName, String goal, String chartType, User loginUser){
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "图表分析目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 200, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
    }
    private void checkFile (@NotNull MultipartFile multipartFile){
        // 校验文件
        long fileSize = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        ThrowUtils.throwIf(fileSize > FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过 1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!valdFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "不支持该类型文件");
    }

    /**
     * 图表更新错误
     *
     * @param chartId     图表ID
     * @param execMessage
     */
    @Override
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setChartStatus(ChartStatusEnum.FAILED.getValue());
        updateChartResult.setExecMessage("图表更新失败！！");
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }

}




