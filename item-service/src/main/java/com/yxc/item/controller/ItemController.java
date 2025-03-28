package com.yxc.item.controller;

import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.item.domain.dto.SaveItemDTO;
import com.yxc.item.domain.po.Item;
import com.yxc.item.service.ItemService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Resource
    private ItemService itemService;

    @PostMapping("/saveItem")
    public Result<Long> saveItem(@RequestBody SaveItemDTO saveItemDTO) {
        return itemService.saveItem(saveItemDTO);
    }

    @GetMapping("/pageQuery")
    public Result<PageVO<Item>> pageQuery(PageQuery pageQuery) {
        return itemService.pageQuery(pageQuery);
    }

}
