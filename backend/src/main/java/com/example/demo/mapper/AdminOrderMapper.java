package com.example.demo.mapper;

import com.example.demo.dto.AdminOrderRowDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminOrderMapper {

    @Select("""
            SELECT
                o.id AS orderId,
                o.buyer_id AS buyerId,
                buyer.username AS buyerUsername,
                o.total_amount AS totalAmount,
                o.create_time AS createTime,
                oi.id AS itemId,
                oi.snack_id AS snackId,
                s.name AS snackName,
                s.image_url AS snackImage,
                s.seller_id AS sellerId,
                seller.username AS sellerUsername,
                oi.quantity AS quantity,
                oi.price AS price,
                oi.subtotal AS subtotal
            FROM orders o
            LEFT JOIN user buyer ON o.buyer_id = buyer.id
            LEFT JOIN order_item oi ON o.id = oi.order_id
            LEFT JOIN snack s ON oi.snack_id = s.id
            LEFT JOIN user seller ON s.seller_id = seller.id
            ORDER BY o.create_time DESC, o.id DESC, oi.id ASC
            """)
    List<AdminOrderRowDTO> selectAllAdminOrderRows();
}
