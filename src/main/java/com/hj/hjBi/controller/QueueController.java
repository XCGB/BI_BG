package com.hj.hjBi.controller;

import cn.hutool.json.JSONUtil;
import com.hj.hjBi.config.ThreadPoolExecutorConfig;
import io.reactivex.rxjava3.core.Completable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图标控制层
 *
 * @author: WHJ
 *
 */
@Slf4j
@RestController
@RequestMapping("/queue")
@Profile({"dev","local"})
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @PostMapping("/add")
    public void add(String name) {
        CompletableFuture.runAsync(() -> {
            log.info("任务执行中：" + name + ",执行人" + Thread.currentThread().getName());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get() {
        HashMap<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度", size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数", taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成任务总数", completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作线程数", activeCount);
        return JSONUtil.toJsonStr(map);
    }

}
