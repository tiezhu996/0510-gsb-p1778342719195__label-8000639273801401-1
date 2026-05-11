package com.cardmanager.entity;

import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_user", indexes = {
        @Index(name = "idx_username", columnList = "username, is_deleted", unique = true)
})
@SQLDelete(sql = "UPDATE sys_user SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // admin, operator

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-启用, 1-禁用

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
