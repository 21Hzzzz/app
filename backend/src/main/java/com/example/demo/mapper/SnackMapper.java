package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Snack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SnackMapper extends BaseMapper<Snack> {

    @Select("""
            SELECT id, name, image_url, price, stock, description, seller_id, status, create_time
            FROM snack
            WHERE id = #{id}
            FOR UPDATE
            """)
    Snack selectByIdForUpdate(@Param("id") Long id);

    @Update("""
            UPDATE snack
            SET stock = #{stock}, status = #{status}
            WHERE id = #{id}
            """)
    int updateStockAndStatus(@Param("id") Long id, @Param("stock") Integer stock, @Param("status") String status);
}
