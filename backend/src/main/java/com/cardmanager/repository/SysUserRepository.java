package com.cardmanager.repository;

import com.cardmanager.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query(value = "SELECT * FROM sys_user WHERE username = :username AND is_deleted = 1", nativeQuery = true)
    Optional<SysUser> findDeletedByUsername(String username);

    @Modifying
    @Query(value = "DELETE FROM sys_user WHERE username = :username AND is_deleted = 1", nativeQuery = true)
    void hardDeleteByUsername(String username);
}
