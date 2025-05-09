package com.yxc.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserDataVO {

    private List<Long> totalCounts;

    private List<Long> newCounts;
}
