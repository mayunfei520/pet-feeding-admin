package com.petfeeding.platform.module.review;

import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.review.entity.Review;
import com.petfeeding.platform.module.review.mapper.ReviewMapper;
import com.petfeeding.platform.module.review.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private OrderMapper orderMapper;
    @Mock private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Order completedOrder;

    @BeforeEach
    void setUp() {
        completedOrder = new Order();
        completedOrder.setId(1L);
        completedOrder.setOwnerId(2L);
        completedOrder.setFeederId(4L);
        completedOrder.setStatus("COMPLETED");
    }

    @Test
    void createReview_completedOrder_success() {
        Review review = new Review();
        review.setOrderId(1L);
        review.setRating(5);
        review.setContent("Great service!");

        when(orderMapper.selectById(1L)).thenReturn(completedOrder);
        when(reviewMapper.exists(any())).thenReturn(false);
        when(reviewMapper.insert(any())).thenReturn(1);

        Review result = reviewService.createReview(review, 2L);

        assertNotNull(result);
        assertEquals(2L, result.getOwnerId());
        assertEquals(4L, result.getFeederId());
        verify(reviewMapper).insert(review);
    }

    @Test
    void createReview_orderNotFound_throws() {
        Review review = new Review();
        review.setOrderId(999L);
        review.setRating(5);

        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> reviewService.createReview(review, 2L));
    }

    @Test
    void createReview_orderNotCompleted_throws() {
        completedOrder.setStatus("ACCEPTED");
        Review review = new Review();
        review.setOrderId(1L);
        review.setRating(5);

        when(orderMapper.selectById(1L)).thenReturn(completedOrder);

        assertThrows(BusinessException.class, () -> reviewService.createReview(review, 2L));
    }

    @Test
    void createReview_wrongOwner_throws() {
        Review review = new Review();
        review.setOrderId(1L);
        review.setRating(5);

        when(orderMapper.selectById(1L)).thenReturn(completedOrder);

        assertThrows(BusinessException.class, () -> reviewService.createReview(review, 99L));
    }

    @Test
    void createReview_alreadyReviewed_throws() {
        Review review = new Review();
        review.setOrderId(1L);
        review.setRating(5);

        when(orderMapper.selectById(1L)).thenReturn(completedOrder);
        when(reviewMapper.exists(any())).thenReturn(true);

        assertThrows(BusinessException.class, () -> reviewService.createReview(review, 2L));
    }

    @Test
    void listByFeeder_returnsOrderedList() {
        when(reviewMapper.selectList(any())).thenReturn(java.util.List.of());

        var result = reviewService.listByFeeder(4L);

        assertNotNull(result);
        verify(reviewMapper).selectList(any());
    }
}