package com.yxc.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.common.exception.BizIllegalException;
import com.yxc.common.utils.RedisIdWork;
import com.yxc.item.domain.dto.OrderDetail;
import com.yxc.item.domain.dto.SaveItemDTO;
import com.yxc.item.domain.dto.UpdateItemDTO;
import com.yxc.item.domain.po.Item;
import com.yxc.item.domain.vo.GetItemCountVO;
import com.yxc.item.mapper.ItemMapper;
import com.yxc.item.service.ItemService;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private RedisIdWork redisIdWork;

    @Override
    public Result<Long> saveItem(SaveItemDTO saveItemDTO) {
        String name = saveItemDTO.getName();
        Item item = lambdaQuery().eq(StrUtil.isNotEmpty(name), Item::getName, name).one();
        if(item != null) {
            return Result.error("该商品已存在");
        }
        item = new Item();
        item.setItemId(redisIdWork.nextId("itemId"));
        item.setName(name);
        item.setPrice(saveItemDTO.getPrice());
        item.setCapacity(saveItemDTO.getCapacity());
        item.setImageUrl(saveItemDTO.getImageUrl());
        save(item);
        return Result.ok(item.getItemId());
    }

    @Override
    public Result<PageVO<Item>> pageQuery(PageQuery pageQuery) {
        String likeName = pageQuery.getLikeName();
        Page<Item> page = lambdaQuery()
                .orderBy(true, true, Item::getStatus)
                .orderBy(true, false, Item::getItemId)
                .like(StrUtil.isNotEmpty(likeName), Item::getName, likeName)
                .page(new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize()));
        PageVO<Item> vo = new PageVO<>();
        vo.setPages(page.getPages());
        vo.setTotal(page.getTotal());
        vo.setList(page.getRecords());
        return Result.ok(vo);
    }

    @Override
    public Result<PageVO<Item>> shelvesPageQuery(PageQuery pageQuery) {
        String likeName = pageQuery.getLikeName();
        Page<Item> page = lambdaQuery()
                .eq(Item::getStatus, 0)
                .orderBy(true, false, Item::getCapacity)
                .like(StrUtil.isNotEmpty(likeName), Item::getName, likeName)
                .page(new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize()));
        PageVO<Item> vo = new PageVO<>();
        vo.setPages(page.getPages());
        vo.setTotal(page.getTotal());
        vo.setList(page.getRecords());
        return Result.ok(vo);
    }

    @Override
    public Result<Long> updateItem(UpdateItemDTO updateItemDTO) {
        Long itemId = updateItemDTO.getItemId();
        String name = updateItemDTO.getName();
        Item item = getById(itemId);
        if(item == null) {
            return Result.error("要修改的商品已被删除");
        }
        item.setName(name);
        item.setPrice(updateItemDTO.getPrice());
        item.setCapacity(updateItemDTO.getCapacity());
        item.setImageUrl(updateItemDTO.getImageUrl());
        updateById(item);
        return Result.ok(itemId);
    }

    @Override
    public Result<Long> changeItemStatus(Long id) {
        Item item = getById(id);
        item.setStatus((short) ((item.getStatus() + 1) % 2));
        updateById(item);
        return Result.ok(id);
    }

    @Override
    public void deductStock(List<OrderDetail> items) {
        String sqlStatement = "com.yxc.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            throw new BizIllegalException("更新库存异常，可能是库存不足!", e);
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
        itemMapper.updateStock(items.get(0));
    }

    @Override
    public Result<GetItemCountVO> getItemCount() {
        List<Item> items = lambdaQuery().list();
        Map<Integer, Long> itemMap = items.stream()
                .collect(Collectors.groupingBy(item -> Short.toUnsignedInt(item.getStatus()), Collectors.counting()));
        GetItemCountVO vo = new GetItemCountVO();
        vo.setShelveCount(itemMap.get(0));
        vo.setUnshelveCount(itemMap.get(1) == null ? 0 : itemMap.get(1));
        return Result.ok(vo);
    }
}
