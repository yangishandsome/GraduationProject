package com.yxc.item.controller;

import com.yxc.item.service.ItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Resource
    private ItemService itemService;
}
