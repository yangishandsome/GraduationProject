package com.yxc.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.item.domain.dto.SaveItemDTO;
import com.yxc.item.domain.dto.UpdateItemDTO;
import com.yxc.item.domain.po.Item;

public interface ItemService extends IService<Item> {
    Result<Long> saveItem(SaveItemDTO saveItemDTO);

    Result<PageVO<Item>> pageQuery(PageQuery pageQuery);

    Result<Long> updateItem(UpdateItemDTO updateItemDTO);

    Result<Long> changeItemStatus(Long id);
}
