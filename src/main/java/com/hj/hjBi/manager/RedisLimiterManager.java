package com.hj.hjBi.manager;

import com.hj.hjBi.common.ErrorCode;
import com.hj.hjBi.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author: WHJ
 * @createTime: 2023-09-15 10:28
 * @description: 提供 Redis Limiter 限流基础服务
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作--令牌桶
     *
     * @param key 区分不同的限流器，比如不同的用户ID应该分别统计
     */
    public void doRateLimit(String key)  {
        // 获取Rate Limiter
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 设置Rate Limit: 每个用户每秒只能访问2次
        rateLimiter.trySetRate(RateType.OVERALL, 2,1, RateIntervalUnit.SECONDS);

        boolean canOp = rateLimiter.tryAcquire(1);
        if (!canOp) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }


}
