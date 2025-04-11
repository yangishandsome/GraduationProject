package com.yxc.api.client.fallback;

import com.yxc.api.client.ItemClient;
import com.yxc.api.po.Item;
import com.yxc.api.po.OrderDetail;
import com.yxc.common.exception.BizIllegalException;
import com.yxc.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<Item> getItemByIds(Collection<Long> ids) {
                log.error("查询商品失败！", cause);
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetail> items) {
                log.error("扣减商品库存失败！", cause);
                throw new BizIllegalException(cause);
            }
        };
    }
}
