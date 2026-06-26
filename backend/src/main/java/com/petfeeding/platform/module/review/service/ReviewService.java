package com.petfeeding.platform.module.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petfeeding.platform.module.review.entity.Review;

import java.util.List;

/**
 * 评价服务接口
 */
public interface ReviewService extends IService<Review> {

    /**
     * 创建评价
     */
    Review createReview(Review review, Long ownerId);

    /**
     * 查喂养员的评价列表
     */
    List<Review> listByFeeder(Long feederId);
}
