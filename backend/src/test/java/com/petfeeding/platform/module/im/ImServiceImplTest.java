package com.petfeeding.platform.module.im;

import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.feeder.mapper.FeederMapper;
import com.petfeeding.platform.module.im.mapper.ConversationMapper;
import com.petfeeding.platform.module.im.mapper.MessageMapper;
import com.petfeeding.platform.module.im.service.impl.ImServiceImpl;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
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
class ImServiceImplTest {

    @Mock private ConversationMapper conversationMapper;
    @Mock private MessageMapper messageMapper;
    @Mock private OrderMapper orderMapper;
    @Mock private FeederMapper feederMapper;
    @Mock private UserMapper userMapper;
    @Mock private com.baomidou.mybatisplus.core.conditions.Wrapper wrapper;

    @InjectMocks
    private ImServiceImpl imService;

    private Order testOrder;
    private User ownerUser;
    private User feederUser;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOwnerId(2L);
        testOrder.setFeederId(4L);

        ownerUser = new User();
        ownerUser.setId(2L);
        ownerUser.setUsername("owner");
        ownerUser.setRole("OWNER");

        feederUser = new User();
        feederUser.setId(4L);
        feederUser.setUsername("feeder");
        ownerUser.setRole("FEEDER");
    }

    @Test
    void getOrCreateByOrder_orderNotFound_throws404() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> imService.getOrCreateByOrder(999L, ownerUser));

        assertEquals(404, ex.getCode());
    }

    @Test
    void getOrCreateByOrder_noFeederAssigned_throws400() {
        testOrder.setFeederId(null);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> imService.getOrCreateByOrder(1L, ownerUser));

        assertEquals(400, ex.getCode());
    }

    @Test
    void getOrCreateByOrder_nonMemberAccess_throws403() {
        User outsider = new User();
        outsider.setId(99L);
        outsider.setUsername("outsider");

        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> imService.getOrCreateByOrder(1L, outsider));

        assertEquals(403, ex.getCode());
    }

    @Test
    void sendMessage_orderNotFound_throws404() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> imService.sendMessage(1L, "TEXT", "hello", ownerUser));
    }

    @Test
    void markRead_orderNotFound_throws404() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> imService.markRead(1L, ownerUser));
    }

    @Test
    void listConversations_userWithNoConversations_returnsEmpty() {
        when(conversationMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        var result = imService.listConversations(ownerUser, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listMessages_orderNotFound_throws() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> imService.listMessages(1L, null, 20, ownerUser));
    }
}