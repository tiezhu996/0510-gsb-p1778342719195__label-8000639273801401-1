package com.cardmanager.controller;

import com.cardmanager.common.Result;
import com.cardmanager.dto.UserSaveRequest;
import com.cardmanager.entity.SysUser;
import com.cardmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<Page<SysUser>> queryUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.queryUsers(username, realName, role, page, size));
    }

    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping("/save")
    public Result<SysUser> saveUser(@Valid @RequestBody UserSaveRequest request) {
        try {
            return Result.success(userService.saveUser(request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
