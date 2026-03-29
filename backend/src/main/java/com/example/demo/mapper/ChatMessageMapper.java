package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.ChatMessageRowDTO;
import com.example.demo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("""
            SELECT
                cm.id AS id,
                cm.session_id AS sessionId,
                cm.sender_id AS senderId,
                sender.username AS senderUsername,
                cm.content AS content,
                cm.create_time AS createTime
            FROM chat_message cm
            INNER JOIN user sender ON cm.sender_id = sender.id
            WHERE cm.session_id = #{sessionId}
            ORDER BY cm.create_time ASC, cm.id ASC
            """)
    List<ChatMessageRowDTO> selectMessageRowsBySessionId(@Param("sessionId") Long sessionId);

    @Select("""
            SELECT cm.id
            FROM chat_message cm
            WHERE cm.session_id = #{sessionId}
            ORDER BY cm.id DESC
            LIMIT 1
            """)
    Long selectLatestMessageIdBySessionId(@Param("sessionId") Long sessionId);
}
