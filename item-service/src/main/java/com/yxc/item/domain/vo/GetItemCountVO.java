package com.yxc.item.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetItemCountVO {
    private Long shelveCount;

    private Long unshelveCount;
}
