package com.petfeeding.platform.module.order;

import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.order.service.impl.OrderServiceImpl;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
import com.petfeeding.platform.module.sms.service.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private FeederService feederService;
    @Mock private PetService petService;
    @Mock private SmsService smsService;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private Pet approvedPet;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("PF202608050001");
        testOrder.setOwnerId(2L);
        testOrder.setFeederId(4L);
        testOrder.setPetId(1L);
        testOrder.setStatus("PENDING");

        approvedPet = new Pet();
        approvedPet.setId(1L);
        approvedPet.setUserId(2L);
        approvedPet.setStatus(Pet.STATUS_APPROVED);
    }

    @Test
    void createOrder_approvedPet_success() {
        when(petService.getById(1L)).thenReturn(approvedPet);
        when(orderMapper.insert(any())).thenReturn(1);

        Order result = orderService.createOrder(testOrder, 2L);

        assertEquals("PENDING", result.getStatus());
        assertEquals(2L, result.getOwnerId());
        assertNotNull(result.getOrderNo());
        assertNull(result.getPrice());
        verify(orderMapper).insert(any());
    }

    @Test
    void createOrder_petNotFound_throws() {
        when(petService.getById(999L)).thenReturn(null);
        testOrder.setPetId(999L);

        assertThrows(BusinessException.class, () -> orderService.createOrder(testOrder, 2L));
    }

    @Test
    void createOrder_petNotApproved_throws() {
        approvedPet.setStatus(Pet.STATUS_PENDING);
        when(petService.getById(1L)).thenReturn(approvedPet);

        assertThrows(BusinessException.class, () -> orderService.createOrder(testOrder, 2L));
    }

    @Test
    void quoteOrder_validTransition_success() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.quoteOrder(1L, 4L, new BigDecimal("100.00"));

        assertEquals("QUOTED", testOrder.getStatus());
        assertEquals(new BigDecimal("100.00"), testOrder.getPrice());
    }

    @Test
    void quoteOrder_wrongStatus_throws() {
        testOrder.setStatus("ACCEPTED");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.quoteOrder(1L, 4L, new BigDecimal("100")));
    }

    @Test
    void quoteOrder_wrongFeeder_throws() {
        testOrder.setFeederId(99L);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.quoteOrder(1L, 4L, new BigDecimal("100")));
    }

    @Test
    void quoteOrder_invalidPrice_throws() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.quoteOrder(1L, 4L, BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> orderService.quoteOrder(1L, 4L, new BigDecimal("-10")));
        assertThrows(BusinessException.class, () -> orderService.quoteOrder(1L, 4L, null));
    }

    @Test
    void confirmOrder_validTransition_success() {
        testOrder.setStatus("QUOTED");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.confirmOrder(1L, 2L);

        assertEquals("ACCEPTED", testOrder.getStatus());
    }

    @Test
    void confirmOrder_wrongOwner_throws() {
        testOrder.setStatus("QUOTED");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.confirmOrder(1L, 99L));
    }

    @Test
    void confirmOrder_notQuoted_throws() {
        testOrder.setStatus("PENDING");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.confirmOrder(1L, 2L));
    }

    @Test
    void rejectOrder_validTransition_resetsToPending() {
        testOrder.setStatus("QUOTED");
        testOrder.setPrice(new BigDecimal("100"));
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.rejectOrder(1L, 2L);

        assertEquals("PENDING", testOrder.getStatus());
        assertNotNull(testOrder.getPrice());
    }

    @Test
    void completeOrder_validTransition_success() {
        testOrder.setStatus("ACCEPTED");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.completeOrder(1L, 2L);

        assertEquals("COMPLETED", testOrder.getStatus());
    }

    @Test
    void completeOrder_wrongStatus_throws() {
        testOrder.setStatus("PENDING");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.completeOrder(1L, 2L));
    }

    @Test
    void startOrder_validTransition_success() {
        testOrder.setStatus("ACCEPTED");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.startOrder(1L, 4L);

        assertEquals("IN_PROGRESS", testOrder.getStatus());
    }

    @Test
    void cancelOrder_pendingStatus_success() {
        testOrder.setStatus("PENDING");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.cancelOrder(1L, 2L);

        assertEquals("CANCELLED", testOrder.getStatus());
    }

    @Test
    void cancelOrder_quotedStatus_success() {
        testOrder.setStatus("QUOTED");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.cancelOrder(1L, 2L);

        assertEquals("CANCELLED", testOrder.getStatus());
    }

    @Test
    void cancelOrder_completedStatus_throws() {
        testOrder.setStatus("COMPLETED");
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, 2L));
    }

    @Test
    void assignOrder_approvedFeeder_success() {
        Feeder feeder = new Feeder();
        feeder.setId(4L);
        feeder.setUserId(4L);
        feeder.setStatus("APPROVED");

        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(feederService.getById(4L)).thenReturn(feeder);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.assignOrder(1L, 4L);

        assertEquals(4L, testOrder.getFeederId());
        verify(smsService).sendOrderNotify(feeder, testOrder);
    }

    @Test
    void assignOrder_feederNotApproved_throws() {
        Feeder feeder = new Feeder();
        feeder.setId(4L);
        feeder.setUserId(4L);
        feeder.setStatus("PENDING");

        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(feederService.getById(4L)).thenReturn(feeder);

        assertThrows(BusinessException.class, () -> orderService.assignOrder(1L, 4L));
        verify(smsService, never()).sendOrderNotify(any(), any());
    }

    @Test
    void orderNotFound_throws() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> orderService.quoteOrder(999L, 4L, new BigDecimal("100")));
        assertThrows(BusinessException.class, () -> orderService.confirmOrder(999L, 2L));
        assertThrows(BusinessException.class, () -> orderService.cancelOrder(999L, 2L));
    }
}