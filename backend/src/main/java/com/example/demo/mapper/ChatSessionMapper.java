package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.ChatSessionRowDTO;
import com.example.demo.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    @Select("""
            SELECT
                id,
                order_id AS orderId,
                buyer_id AS buyerId,
                seller_id AS sellerId,
                create_time AS createTime
            FROM chat_session
            WHERE order_id = #{orderId}
              AND buyer_id = #{buyerId}
              AND seller_id = #{sellerId}
            LIMIT 1
            """)
    ChatSession selectByOrderBuyerSeller(
            @Param("orderId") Long orderId,
            @Param("buyerId") Long buyerId,
            @Param("sellerId") Long sellerId
    );

    @Select("""
            SELECT
                cs.id AS id,
                cs.order_id AS orderId,
                cs.buyer_id AS buyerId,
                buyer.username AS buyerUsername,
                cs.seller_id AS sellerId,
                seller.username AS sellerUsername,
                latest.latestMessage AS latestMessage,
                latest.latestMessageTime AS latestMessageTime,
                cs.create_time AS createTime
            FROM chat_session cs
            INNER JOIN user buyer ON cs.buyer_id = buyer.id
            INNER JOIN user seller ON cs.seller_id = seller.id
            LEFT JOIN (
                SELECT
                    cm.id AS latestMessageId,
                    cm.session_id AS sessionId,
                    cm.content AS latestMessage,
                    cm.create_time AS latestMessageTime
                FROM chat_message cm
                INNER JOIN (
                    SELECT session_id, MAX(id) AS latestMessageId
                    FROM chat_message
                    GROUP BY session_id
                ) grouped ON grouped.latestMessageId = cm.id
            ) latest ON latest.sessionId = cs.id
            WHERE cs.buyer_id = #{userId}
               OR cs.seller_id = #{userId}
            ORDER BY COALESCE(latest.latestMessageTime, cs.create_time) DESC, cs.id DESC
            """)
    List<ChatSessionRowDTO> selectSessionRowsForUser(@Param("userId") Long userId);

    @Select("""
            SELECT
                cs.id AS id,
                cs.order_id AS orderId,
                cs.buyer_id AS buyerId,
                buyer.username AS buyerUsername,
                cs.seller_id AS sellerId,
                seller.username AS sellerUsername,
                latest.latestMessage AS latestMessage,
                latest.latestMessageTime AS latestMessageTime,
                cs.create_time AS createTime
            FROM chat_session cs
            INNER JOIN user buyer ON cs.buyer_id = buyer.id
            INNER JOIN user seller ON cs.seller_id = seller.id
            LEFT JOIN (
                SELECT
                    cm.id AS latestMessageId,
                    cm.session_id AS sessionId,
                    cm.content AS latestMessage,
                    cm.create_time AS latestMessageTime
                FROM chat_message cm
                INNER JOIN (
                    SELECT session_id, MAX(id) AS latestMessageId
                    FROM chat_message
                    GROUP BY session_id
                ) grouped ON grouped.latestMessageId = cm.id
            ) latest ON latest.sessionId = cs.id
            WHERE cs.buyer_id = #{buyerId}
            ORDER BY COALESCE(latest.latestMessageTime, cs.create_time) DESC, cs.id DESC
            """)
    List<ChatSessionRowDTO> selectSessionRowsByBuyerId(@Param("buyerId") Long buyerId);

    @Select("""
            SELECT
                cs.id AS id,
                cs.order_id AS orderId,
                cs.buyer_id AS buyerId,
                buyer.username AS buyerUsername,
                cs.seller_id AS sellerId,
                seller.username AS sellerUsername,
                latest.latestMessage AS latestMessage,
                latest.latestMessageTime AS latestMessageTime,
                cs.create_time AS createTime
            FROM chat_session cs
            INNER JOIN user buyer ON cs.buyer_id = buyer.id
            INNER JOIN user seller ON cs.seller_id = seller.id
            LEFT JOIN (
                SELECT
                    cm.id AS latestMessageId,
                    cm.session_id AS sessionId,
                    cm.content AS latestMessage,
                    cm.create_time AS latestMessageTime
                FROM chat_message cm
                INNER JOIN (
                    SELECT session_id, MAX(id) AS latestMessageId
                    FROM chat_message
                    GROUP BY session_id
                ) grouped ON grouped.latestMessageId = cm.id
            ) latest ON latest.sessionId = cs.id
            WHERE cs.seller_id = #{sellerId}
            ORDER BY COALESCE(latest.latestMessageTime, cs.create_time) DESC, cs.id DESC
            """)
    List<ChatSessionRowDTO> selectSessionRowsBySellerId(@Param("sellerId") Long sellerId);

    @Select("""
            SELECT
                cs.id AS id,
                cs.order_id AS orderId,
                cs.buyer_id AS buyerId,
                buyer.username AS buyerUsername,
                cs.seller_id AS sellerId,
                seller.username AS sellerUsername,
                cs.create_time AS createTime
            FROM chat_session cs
            INNER JOIN user buyer ON cs.buyer_id = buyer.id
            INNER JOIN user seller ON cs.seller_id = seller.id
            WHERE cs.id = #{sessionId}
            """)
    ChatSessionRowDTO selectSessionRowById(@Param("sessionId") Long sessionId);
}
