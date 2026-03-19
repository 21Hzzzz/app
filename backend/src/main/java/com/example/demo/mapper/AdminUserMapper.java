package com.example.demo.mapper;

import com.example.demo.dto.AdminUserRowDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface AdminUserMapper {

    @Select("""
            SELECT
                id AS id,
                username AS username,
                role AS role,
                create_time AS createTime
            FROM user
            ORDER BY create_time DESC, id DESC
            """)
    List<AdminUserRowDTO> selectAllAdminUsers();

    @Select("""
            SELECT COUNT(*)
            FROM snack
            WHERE seller_id = #{userId}
            """)
    Long countSnacksByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM orders
            WHERE buyer_id = #{userId}
            """)
    Long countOrdersByUserId(@Param("userId") Long userId);

    @Delete("""
            DELETE FROM user
            WHERE id = #{userId}
              AND role = 'user'
            """)
    int deleteNormalUserById(@Param("userId") Long userId);
}
