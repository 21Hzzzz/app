package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.BoughtOrderRowDTO;
import com.example.demo.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("""
            SELECT
                o.id AS orderId,
                o.buyer_id AS buyerId,
                o.total_amount AS totalAmount,
                o.create_time AS createTime,
                oi.id AS itemId,
                oi.snack_id AS snackId,
                s.name AS snackName,
                s.image_url AS snackImage,
                oi.quantity AS quantity,
                oi.price AS price,
                oi.subtotal AS subtotal
            FROM orders o
            LEFT JOIN order_item oi ON o.id = oi.order_id
            LEFT JOIN snack s ON oi.snack_id = s.id
            WHERE o.buyer_id = #{buyerId}
            ORDER BY o.create_time DESC, o.id DESC, oi.id ASC
            """)
    List<BoughtOrderRowDTO> selectBoughtOrderRows(@Param("buyerId") Long buyerId);
}
