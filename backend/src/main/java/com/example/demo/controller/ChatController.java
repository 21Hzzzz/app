package com.example.demo.controller;

import com.example.demo.dto.CreateChatSessionRequest;
import com.example.demo.dto.MarkChatSessionReadRequest;
import com.example.demo.dto.Result;
import com.example.demo.dto.SendChatMessageRequest;
import com.example.demo.service.ChatService;
import com.example.demo.vo.ChatMessageVO;
import com.example.demo.vo.ChatSessionDetailVO;
import com.example.demo.vo.ChatSessionVO;
import com.example.demo.vo.CreateChatSessionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    public Result<CreateChatSessionVO> createSession(@Valid @RequestBody CreateChatSessionRequest request) {
        CreateChatSessionVO response = chatService.createSession(request);
        return Result.success("success", response);
    }

    @PostMapping("/sessions/{sessionId}/read")
    public Result<Void> markSessionRead(
            @PathVariable Long sessionId,
            @Valid @RequestBody MarkChatSessionReadRequest request
    ) {
        chatService.markSessionRead(sessionId, request.getUserId());
        return Result.success("success", null);
    }

    @GetMapping("/sessions/all")
    public Result<List<ChatSessionVO>> getAllSessions(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }

        List<ChatSessionVO> response = chatService.getAllSessions(userId);
        return Result.success("success", response);
    }

    @GetMapping("/sessions/buyer")
    public Result<List<ChatSessionVO>> getBuyerSessions(@RequestParam(required = false) Long buyerId) {
        if (buyerId == null) {
            throw new RuntimeException("buyerId is required");
        }

        List<ChatSessionVO> response = chatService.getBuyerSessions(buyerId);
        return Result.success("success", response);
    }

    @GetMapping("/sessions/seller")
    public Result<List<ChatSessionVO>> getSellerSessions(@RequestParam(required = false) Long sellerId) {
        if (sellerId == null) {
            throw new RuntimeException("sellerId is required");
        }

        List<ChatSessionVO> response = chatService.getSellerSessions(sellerId);
        return Result.success("success", response);
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<ChatSessionDetailVO> getSessionDetail(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long userId
    ) {
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }

        ChatSessionDetailVO response = chatService.getSessionDetail(sessionId, userId);
        return Result.success("success", response);
    }

    @GetMapping("/messages")
    public Result<List<ChatMessageVO>> getMessages(
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long userId
    ) {
        if (sessionId == null) {
            throw new RuntimeException("sessionId is required");
        }
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }

        List<ChatMessageVO> response = chatService.getMessages(sessionId, userId);
        return Result.success("success", response);
    }

    @PostMapping("/messages")
    public Result<Void> sendMessage(@Valid @RequestBody SendChatMessageRequest request) {
        chatService.sendMessage(request);
        return Result.success("success", null);
    }
}
