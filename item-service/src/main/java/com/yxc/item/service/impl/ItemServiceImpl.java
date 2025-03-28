package com.yxc.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.item.domain.dto.SaveItemDTO;
import com.yxc.item.domain.po.Item;
import com.yxc.item.mapper.ItemMapper;
import com.yxc.item.service.ItemService;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Override
    public Result<Long> saveItem(SaveItemDTO saveItemDTO) {
        String name = saveItemDTO.getName();
        Item item = lambdaQuery().eq(StrUtil.isNotEmpty(name), Item::getName, name).one();
        if(item != null) {
            return Result.error("该商品已存在");
        }
        item = new Item();
        item.setName(name);
        item.setPrice(saveItemDTO.getPrice());
        item.setCapacity(saveItemDTO.getCapacity());
        item.setImageUrl(saveItemDTO.getImageUrl());
        save(item);
        return Result.ok(item.getItemId());
    }

    @Override
    public Result<PageVO<Item>> pageQuery(PageQuery pageQuery) {
        Page<Item> page = lambdaQuery().page(new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize()));
        PageVO<Item> vo = new PageVO<>();
        vo.setPages(page.getPages());
        vo.setTotal(page.getTotal());
        vo.setList(page.getRecords());
        return Result.ok(vo);
    }
}
