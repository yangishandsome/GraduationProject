package com.yxc.item.controller;

import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.common.utils.UserContext;
import com.yxc.item.domain.dto.OrderDetail;
import com.yxc.item.domain.dto.SaveItemDTO;
import com.yxc.item.domain.dto.UpdateItemDTO;
import com.yxc.item.domain.po.Item;
import com.yxc.item.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/item")
public class ItemController {
    @Resource
    private ItemService itemService;

    @PostMapping("/saveItem")
    public Result<Long> saveItem(@RequestBody SaveItemDTO saveItemDTO) {
        return itemService.saveItem(saveItemDTO);
    }

    @PostMapping("/updateItem")
    public Result<Long> updateItem(@RequestBody UpdateItemDTO updateItemDTO) {
        return itemService.updateItem(updateItemDTO);
    }

    @GetMapping("/pageQuery")
    public Result<PageVO<Item>> pageQuery(PageQuery pageQuery) {
        return itemService.pageQuery(pageQuery);
    }


    @PostMapping("/changeItemStatus/{id}")
    public Result<Long> changeItemStatus(@PathVariable(value = "id") Long id) {
        return itemService.changeItemStatus(id);
    }

    @PostMapping("deleteItem/{id}")
    public Result<Long> deleteItem(@PathVariable(value = "id") Long id) {
        itemService.removeById(id);
        return Result.ok(id);
    }

    /**
     * FeignApi接口
     */
    @GetMapping("/getByIds")
    List<Item> getItemByIds(@RequestParam("ids") List<Long> ids) {
        return itemService.listByIds(ids);
    }

    @PutMapping("/deductStock")
    void deductStock(@RequestBody List<OrderDetail> items) {
        itemService.deductStock(items);
    }
}
