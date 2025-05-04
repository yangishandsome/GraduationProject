package com.yxc.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodayUserDataVO {
    private Long activeUser;

    private Long newUser;
}
