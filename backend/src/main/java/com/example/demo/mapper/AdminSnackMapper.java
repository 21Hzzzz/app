package com.example.demo.mapper;

import com.example.demo.dto.AdminSnackRowDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminSnackMapper {

    @Select("""
            SELECT
                s.id AS id,
                s.name AS name,
                s.image_url AS image,
                s.price AS price,
                s.stock AS stock,
                s.description AS description,
                s.seller_id AS sellerId,
                u.username AS sellerUsername,
                s.status AS status,
                s.create_time AS createTime
            FROM snack s
            LEFT JOIN user u ON s.seller_id = u.id
            ORDER BY s.create_time DESC, s.id DESC
            """)
    List<AdminSnackRowDTO> selectAllAdminSnacks();
}
