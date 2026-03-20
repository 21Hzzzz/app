package com.example.demo.service.impl;

import com.example.demo.dto.ChatMessageRowDTO;
import com.example.demo.dto.ChatSessionRowDTO;
import com.example.demo.dto.CreateChatSessionRequest;
import com.example.demo.dto.SendChatMessageRequest;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatSession;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.ChatSessionMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.ChatService;
import com.example.demo.vo.ChatMessageVO;
import com.example.demo.vo.ChatSessionDetailVO;
import com.example.demo.vo.ChatSessionVO;
import com.example.demo.vo.CreateChatSessionVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public ChatServiceImpl(
            ChatSessionMapper chatSessionMapper,
            ChatMessageMapper chatMessageMapper,
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            UserMapper userMapper
    ) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateChatSessionVO createSession(CreateChatSessionRequest request) {
        if (request.getBuyerId().equals(request.getSellerId())) {
            throw new RuntimeException("buyerId and sellerId cannot be the same");
        }

        User buyer = userMapper.selectById(request.getBuyerId());
        if (buyer == null) {
            throw new RuntimeException("buyer does not exist");
        }

        User seller = userMapper.selectById(request.getSellerId());
        if (seller == null) {
            throw new RuntimeException("seller does not exist");
        }

        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("order does not exist");
        }
        if (!request.getBuyerId().equals(order.getBuyerId())) {
            throw new RuntimeException("order does not belong to buyer");
        }

        int sellerItemCount = orderItemMapper.countSellerItemsInOrder(request.getOrderId(), request.getSellerId());
        if (sellerItemCount <= 0) {
            throw new RuntimeException("seller is not part of this order");
        }

        ChatSession existing = chatSessionMapper.selectByOrderBuyerSeller(
                request.getOrderId(),
                request.getBuyerId(),
                request.getSellerId()
        );
        if (existing != null) {
            return buildCreateSessionResponse(existing.getId());
        }

        ChatSession session = new ChatSession();
        session.setOrderId(request.getOrderId());
        session.setBuyerId(request.getBuyerId());
        session.setSellerId(request.getSellerId());
        session.setCreateTime(LocalDateTime.now());

        try {
            if (chatSessionMapper.insert(session) <= 0 || session.getId() == null) {
                throw new RuntimeException("failed to create chat session");
            }
        } catch (DuplicateKeyException exception) {
            ChatSession duplicated = chatSessionMapper.selectByOrderBuyerSeller(
                    request.getOrderId(),
                    request.getBuyerId(),
                    request.getSellerId()
            );
            if (duplicated != null) {
                return buildCreateSessionResponse(duplicated.getId());
            }
            throw new RuntimeException("failed to create chat session");
        }

        return buildCreateSessionResponse(session.getId());
    }

    @Override
    public List<ChatSessionVO> getAllSessions(Long userId) {
        ensureUserExists(userId);
        return mapSessionRows(chatSessionMapper.selectSessionRowsForUser(userId));
    }

    @Override
    public List<ChatSessionVO> getBuyerSessions(Long buyerId) {
        ensureUserExists(buyerId);
        return mapSessionRows(chatSessionMapper.selectSessionRowsByBuyerId(buyerId));
    }

    @Override
    public List<ChatSessionVO> getSellerSessions(Long sellerId) {
        ensureUserExists(sellerId);
        return mapSessionRows(chatSessionMapper.selectSessionRowsBySellerId(sellerId));
    }

    @Override
    public ChatSessionDetailVO getSessionDetail(Long sessionId, Long userId) {
        ChatSessionRowDTO row = getOwnedSessionRow(sessionId, userId);

        ChatSessionDetailVO response = new ChatSessionDetailVO();
        response.setId(row.getId());
        response.setOrderId(row.getOrderId());
        response.setBuyerId(row.getBuyerId());
        response.setBuyerUsername(row.getBuyerUsername());
        response.setSellerId(row.getSellerId());
        response.setSellerUsername(row.getSellerUsername());
        response.setCreateTime(row.getCreateTime());
        return response;
    }

    @Override
    public List<ChatMessageVO> getMessages(Long sessionId, Long userId) {
        getOwnedSessionRow(sessionId, userId);

        List<ChatMessageRowDTO> rows = chatMessageMapper.selectMessageRowsBySessionId(sessionId);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatMessageVO> response = new ArrayList<>(rows.size());
        for (ChatMessageRowDTO row : rows) {
            ChatMessageVO message = new ChatMessageVO();
            message.setId(row.getId());
            message.setSessionId(row.getSessionId());
            message.setSenderId(row.getSenderId());
            message.setSenderUsername(row.getSenderUsername());
            message.setContent(row.getContent());
            message.setCreateTime(row.getCreateTime());
            response.add(message);
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(SendChatMessageRequest request) {
        ChatSessionRowDTO session = getOwnedSessionRow(request.getSessionId(), request.getSenderId());

        String content = request.getContent().trim();
        if (content.isEmpty()) {
            throw new RuntimeException("content cannot be blank");
        }

        boolean isParticipant = request.getSenderId().equals(session.getBuyerId())
                || request.getSenderId().equals(session.getSellerId());
        if (!isParticipant) {
            throw new RuntimeException("sender is not part of this session");
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(request.getSessionId());
        message.setSenderId(request.getSenderId());
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());

        if (chatMessageMapper.insert(message) <= 0) {
            throw new RuntimeException("failed to send message");
        }
    }

    private CreateChatSessionVO buildCreateSessionResponse(Long sessionId) {
        CreateChatSessionVO response = new CreateChatSessionVO();
        response.setSessionId(sessionId);
        return response;
    }

    private void ensureUserExists(Long userId) {
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }
        if (userMapper.selectById(userId) == null) {
            throw new RuntimeException("user does not exist");
        }
    }

    private ChatSessionRowDTO getOwnedSessionRow(Long sessionId, Long userId) {
        ensureUserExists(userId);
        if (sessionId == null) {
            throw new RuntimeException("sessionId is required");
        }

        ChatSessionRowDTO row = chatSessionMapper.selectSessionRowById(sessionId);
        if (row == null) {
            throw new RuntimeException("chat session does not exist");
        }

        boolean isParticipant = userId.equals(row.getBuyerId()) || userId.equals(row.getSellerId());
        if (!isParticipant) {
            throw new RuntimeException("user cannot access this session");
        }
        return row;
    }

    private List<ChatSessionVO> mapSessionRows(List<ChatSessionRowDTO> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatSessionVO> response = new ArrayList<>(rows.size());
        for (ChatSessionRowDTO row : rows) {
            ChatSessionVO session = new ChatSessionVO();
            session.setId(row.getId());
            session.setOrderId(row.getOrderId());
            session.setBuyerId(row.getBuyerId());
            session.setBuyerUsername(row.getBuyerUsername());
            session.setSellerId(row.getSellerId());
            session.setSellerUsername(row.getSellerUsername());
            session.setLatestMessage(row.getLatestMessage());
            session.setLatestMessageTime(row.getLatestMessageTime());
            session.setCreateTime(row.getCreateTime());
            response.add(session);
        }
        return response;
    }
}
