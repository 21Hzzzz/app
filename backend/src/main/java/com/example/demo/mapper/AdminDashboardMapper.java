package com.example.demo.mapper;

import com.example.demo.dto.AdminDashboardRecentOrderRowDTO;
import com.example.demo.dto.AdminDashboardSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    @Select("""
            SELECT
                (SELECT COUNT(*) FROM user) AS totalUsers,
                (SELECT COUNT(*) FROM snack) AS totalSnacks,
                (SELECT COUNT(*) FROM orders) AS totalOrders,
                COALESCE((SELECT SUM(total_amount) FROM orders), 0) AS totalAmount,
                (SELECT COUNT(*) FROM snack WHERE status = 'on_sale') AS onSaleSnacks,
                (SELECT COUNT(*) FROM snack WHERE status = 'sold_out') AS soldOutSnacks,
                (SELECT COUNT(*) FROM snack WHERE status = 'off_shelf') AS offShelfSnacks,
                (SELECT COUNT(*) FROM user WHERE role = 'user') AS normalUsers,
                (SELECT COUNT(*) FROM user WHERE role = 'admin') AS adminUsers
            """)
    AdminDashboardSummaryDTO selectDashboardSummary();

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
                oi.quantity AS quantity,
                oi.price AS price,
                oi.subtotal AS subtotal
            FROM (
                SELECT id, buyer_id, total_amount, create_time
                FROM orders
                ORDER BY create_time DESC, id DESC
                LIMIT 5
            ) o
            LEFT JOIN user buyer ON o.buyer_id = buyer.id
            LEFT JOIN order_item oi ON o.id = oi.order_id
            LEFT JOIN snack s ON oi.snack_id = s.id
            ORDER BY o.create_time DESC, o.id DESC, oi.id ASC
            """)
    List<AdminDashboardRecentOrderRowDTO> selectRecentOrderRows();
}
