package com.yxc.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxc.item.domain.dto.OrderDetail;
import com.yxc.item.domain.po.Item;
import org.apache.ibatis.annotations.Update;

public interface ItemMapper extends BaseMapper<Item> {
    @Update("update item set capacity = capacity - #{num} where item_id = #{itemId}")
    void updateStock(OrderDetail orderDetail);
}
