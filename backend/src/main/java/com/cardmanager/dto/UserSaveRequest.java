package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserSaveRequest {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "角色不能为空")
    private String role;

    private Integer status = 0;
}
