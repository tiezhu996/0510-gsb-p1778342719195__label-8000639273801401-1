package com.cardmanager.service;

import com.cardmanager.dto.LoginRequest;
import com.cardmanager.dto.UserSaveRequest;
import com.cardmanager.entity.SysUser;
import com.cardmanager.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (user.getStatus() == 1) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser saveUser(UserSaveRequest request) {
        SysUser user;
        if (request.getId() != null) {
            user = userRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setRealName(request.getRealName());
            user.setRole(request.getRole());
            user.setStatus(request.getStatus());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        } else {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user = new SysUser();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setRole(request.getRole());
            user.setStatus(request.getStatus());
        }
        return userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        String originalUsername = user.getUsername();
        
        int affectedRows = userRepository.deleteUserAndReleaseUsername(id, System.currentTimeMillis());
        if (affectedRows == 0) {
            throw new RuntimeException("删除用户失败");
        }
        
        if (userRepository.existsByUsername(originalUsername)) {
            throw new RuntimeException("删除失败，用户名未释放");
        }
    }

    public Page<SysUser> queryUsers(String username, String realName, String role, Integer page, Integer size) {
        Specification<SysUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (realName != null && !realName.isEmpty()) {
                predicates.add(cb.like(root.get("realName"), "%" + realName + "%"));
            }
            if (role != null && !role.isEmpty()) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return userRepository.findAll(spec, pageable);
    }

    public SysUser getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
