package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.SoldOrderRowDTO;
import com.example.demo.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("""
            SELECT
                o.id AS orderId,
                o.buyer_id AS buyerId,
                u.username AS buyerUsername,
                o.create_time AS createTime,
                s.id AS snackId,
                s.name AS snackName,
                s.image_url AS snackImage,
                oi.quantity AS quantity,
                oi.price AS price,
                oi.subtotal AS subtotal
            FROM order_item oi
            INNER JOIN orders o ON oi.order_id = o.id
            INNER JOIN snack s ON oi.snack_id = s.id
            INNER JOIN user u ON o.buyer_id = u.id
            WHERE s.seller_id = #{sellerId}
            ORDER BY o.create_time DESC, o.id DESC, oi.id ASC
            """)
    List<SoldOrderRowDTO> selectSoldOrderRows(@Param("sellerId") Long sellerId);
}
