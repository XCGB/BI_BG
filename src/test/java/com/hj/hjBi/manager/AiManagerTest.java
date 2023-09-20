package com.hj.hjBi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: WHJ
 * @createTime: 2023-09-13 09:56
 * @description:
 */
@SpringBootTest
class AiManagerTest {
    @Resource
    private  AiManager aiManager;
    @Test
    void doChat() {
        String answer = aiManager.doChat(1701510459494473730L,
                "分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "11号,110\n" +
                "12号,130\n" +
                "13号,180");
        System.out.println(answer);
    }
}