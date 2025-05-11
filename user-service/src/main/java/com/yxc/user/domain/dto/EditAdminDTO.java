package com.yxc.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditAdminDTO {
    private Long adminId;

    private String username;

    private Integer permission;
}
