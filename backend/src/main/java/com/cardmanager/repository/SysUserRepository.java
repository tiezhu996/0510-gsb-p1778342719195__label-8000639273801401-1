package com.cardmanager.repository;

import com.cardmanager.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Modifying
    @Query(value = "UPDATE sys_user SET username = CONCAT(username, '_deleted_', :timestamp), is_deleted = 1 WHERE id = :id", nativeQuery = true)
    int deleteUserAndReleaseUsername(@Param("id") Long id, @Param("timestamp") Long timestamp);
}
