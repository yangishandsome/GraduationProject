package com.yxc.common.domain;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.yxc.common.utils.BeanUtils;
import com.yxc.common.utils.CollUtils;
import com.yxc.common.utils.Convert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    protected Long total;
    protected Long pages;
    protected List<T> list;

    public static <T> PageVO<T> empty(Long total, Long pages) {
        return new PageVO<>(total, pages, CollUtils.emptyList());
    }
    public static <T> PageVO<T> empty(Page<?> page) {
        return new PageVO<>(page.getTotal(), page.getPages(), CollUtils.emptyList());
    }

    public static <T> PageVO<T> of(Page<T> page) {
        if(page == null){
            return new PageVO<>();
        }
        if (CollUtils.isEmpty(page.getRecords())) {
            return empty(page);
        }
        return new PageVO<>(page.getTotal(), page.getPages(), page.getRecords());
    }
    public static <T,R> PageVO<T> of(Page<R> page, Function<R, T> mapper) {
        if(page == null){
            return new PageVO<>();
        }
        if (CollUtils.isEmpty(page.getRecords())) {
            return empty(page);
        }
        return new PageVO<>(page.getTotal(), page.getPages(),
                page.getRecords().stream().map(mapper).collect(Collectors.toList()));
    }
    public static <T> PageVO<T> of(Page<?> page, List<T> list) {
        return new PageVO<>(page.getTotal(), page.getPages(), list);
    }

    public static <T, R> PageVO<T> of(Page<R> page, Class<T> clazz) {
        return new PageVO<>(page.getTotal(), page.getPages(), BeanUtils.copyList(page.getRecords(), clazz));
    }

    public static <T, R> PageVO<T> of(Page<R> page, Class<T> clazz, Convert<R, T> convert) {
        return new PageVO<>(page.getTotal(), page.getPages(), BeanUtils.copyList(page.getRecords(), clazz, convert));
    }
}
