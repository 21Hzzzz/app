package com.example.demo.service;

import com.example.demo.dto.CreateChatSessionRequest;
import com.example.demo.dto.SendChatMessageRequest;
import com.example.demo.vo.ChatMessageVO;
import com.example.demo.vo.ChatSessionDetailVO;
import com.example.demo.vo.ChatSessionVO;
import com.example.demo.vo.CreateChatSessionVO;

import java.util.List;

public interface ChatService {
    CreateChatSessionVO createSession(CreateChatSessionRequest request);

    List<ChatSessionVO> getAllSessions(Long userId);

    List<ChatSessionVO> getBuyerSessions(Long buyerId);

    List<ChatSessionVO> getSellerSessions(Long sellerId);

    ChatSessionDetailVO getSessionDetail(Long sessionId, Long userId);

    List<ChatMessageVO> getMessages(Long sessionId, Long userId);

    void sendMessage(SendChatMessageRequest request);
}
