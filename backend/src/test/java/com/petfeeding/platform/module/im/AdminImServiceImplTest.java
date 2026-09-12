package com.petfeeding.platform.module.im;

import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.im.dto.AdminConversationVO;
import com.petfeeding.platform.module.im.dto.AdminMessagePageVO;
import com.petfeeding.platform.module.im.entity.Conversation;
import com.petfeeding.platform.module.im.entity.Message;
import com.petfeeding.platform.module.im.mapper.ConversationMapper;
import com.petfeeding.platform.module.im.mapper.MessageMapper;
import com.petfeeding.platform.module.im.service.impl.AdminImServiceImpl;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminImServiceImplTest {

    @Mock private ConversationMapper conversationMapper;
    @Mock private MessageMapper messageMapper;
    @Mock private UserService userService;
    @Mock private FeederService feederService;
    @Mock private OrderService orderService;

    @InjectMocks
    private AdminImServiceImpl adminImService;

    private Conversation testConversation;
    private Order testOrder;
    private User testOwner;
    private User testFeeder;
    private Feeder testFeederEntity;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setOrderId(1L);
        testConversation.setOwnerId(2L);
        testConversation.setFeederUserId(4L);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("PF202608050001");
        testOrder.setOwnerId(2L);

        testOwner = new User();
        testOwner.setId(2L);
        testOwner.setUsername("owner");
        testOwner.setRealName("Owner Name");

        testFeeder = new User();
        testFeeder.setId(4L);
        testFeeder.setUsername("feeder");
        testFeeder.setRealName("Feeder Name");

        testFeederEntity = new Feeder();
        testFeederEntity.setId(1L);
        testFeederEntity.setUserId(4L);
    }

    @Test
    void listConversations_emptyDb_returnsEmptyList() {
        when(conversationMapper.selectList(any())).thenReturn(List.of());

        List<AdminConversationVO> result = adminImService.listConversations(1, 20, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listConversations_withData_returnsVOs() {
        when(conversationMapper.selectList(any())).thenReturn(List.of(testConversation));
        when(orderService.getById(1L)).thenReturn(testOrder);
        when(userService.getById(2L)).thenReturn(testOwner);
        when(userService.getById(4L)).thenReturn(testFeeder);

        List<AdminConversationVO> result = adminImService.listConversations(1, 20, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PF202608050001", result.get(0).getOrderNo());
    }

    @Test
    void listConversations_withKeyword_filters() {
        when(conversationMapper.selectList(any())).thenReturn(List.of(testConversation));
        when(orderService.getById(1L)).thenReturn(testOrder);
        when(userService.getById(2L)).thenReturn(testOwner);
        when(userService.getById(4L)).thenReturn(testFeeder);

        List<AdminConversationVO> result = adminImService.listConversations(1, 20, "nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listMessages_withData_returnsPage() {
        Message msg = new Message();
        msg.setId(1L);
        msg.setConversationId(1L);
        msg.setSenderId(2L);
        msg.setSenderRole("OWNER");
        msg.setSenderName("Owner");
        msg.setContent("Hello");
        msg.setCreateTime(LocalDateTime.now());

        when(messageMapper.selectList(any())).thenReturn(List.of(msg));

        AdminMessagePageVO result = adminImService.listMessages(1L, null, 50);

        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals(1, result.getList().size());
        assertEquals("Hello", result.getList().get(0).getContent());
    }

    @Test
    void listMessages_noCursor_returnsAll() {
        when(messageMapper.selectList(any())).thenReturn(List.of());

        AdminMessagePageVO result = adminImService.listMessages(1L, null, 20);

        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
        assertNull(result.getNextCursor());
    }
}