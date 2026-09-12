package com.petfeeding.platform.module.sms;

import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.sms.service.impl.SmsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceImplTest {

    private SmsServiceImpl smsService;
    private Feeder testFeeder;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        smsService = new SmsServiceImpl();

        testFeeder = new Feeder();
        testFeeder.setId(1L);
        testFeeder.setUserId(4L);
        testFeeder.setRealName("Test Feeder");
        testFeeder.setPhone("13800000001");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOwnerId(2L);
        testOrder.setPetId(1L);
        testOrder.setServiceDate(LocalDate.of(2026, 8, 10));
        testOrder.setServicePeriod("AM");
        testOrder.setAddress("Beijing");
        testOrder.setPrice(new BigDecimal("80"));
    }

    @Test
    void sendOrderNotify_alwaysReturnsTrue() {
        boolean result = smsService.sendOrderNotify(testFeeder, testOrder);
        assertTrue(result);
    }

    @Test
    void sendOrderNotify_differentPeriods_success() {
        testOrder.setServicePeriod("PM");
        assertTrue(smsService.sendOrderNotify(testFeeder, testOrder));

        testOrder.setServicePeriod("EVENING");
        assertTrue(smsService.sendOrderNotify(testFeeder, testOrder));

        testOrder.setServicePeriod("UNKNOWN");
        assertTrue(smsService.sendOrderNotify(testFeeder, testOrder));
    }

    @Test
    void sendOrderNotify_nullPeriodUsesDefault() {
        testOrder.setServicePeriod(null);
        boolean result = smsService.sendOrderNotify(testFeeder, testOrder);
        assertTrue(result);
    }
}