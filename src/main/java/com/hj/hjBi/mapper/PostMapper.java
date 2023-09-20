package com.hj.hjBi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hj.hjBi.model.entity.Post;
import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作
 *
 * @author: WHJ
 * @createTime: 2023-09-13 09:48
 * @description:
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}




