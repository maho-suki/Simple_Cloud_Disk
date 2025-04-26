package com.maho.simple_network_disk.controller;

import com.maho.simple_network_disk.dto.UserLoginDto;
import com.maho.simple_network_disk.security.JwtTokenProvider;
import com.maho.simple_network_disk.dto.UserRegistrationDto;
import com.maho.simple_network_disk.entity.User;
import com.maho.simple_network_disk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto dto) {
        try {
            User user = userService.registerUser(dto);
            return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "message", "注册成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto loginDto) {
        try {
            // 验证用户名密码
            User user;
            try {
                user = userService.authenticate(loginDto.getUsername(), loginDto.getPassword());
            } catch (AuthenticationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }

            // 生成JWT token
            String token = jwtTokenProvider.createToken(user.getUsername(), user.getUserId());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getUserId(),
                    "username", user.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "用户名或密码错误"));
        }
    }

    // 获取用户信息
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // 用户退出登录
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of(
            "message", "退出登录成功"
        ));
    }

    // 注销账号
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
        @PathVariable Long userId,
        @RequestBody Map<String, String> request,
        @RequestHeader("Authorization") String token) {
        try {
            String password = request.get("password");
            if (password == null) {
                throw new IllegalArgumentException("密码不能为空");
            }
            userService.DeleteUser(userId, password);
            return ResponseEntity.ok(Map.of(
                "message", "用户已注销"));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 重置密码
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String newPassword = request.get("newPassword");
            
            if (username == null) {
                throw new IllegalArgumentException("用户名不能为空");
            }
            if (newPassword == null) {
                throw new IllegalArgumentException("新密码不能为空");
            }
            if (email == null) {
                throw new IllegalArgumentException("邮箱不能为空");
            }

            // 根据用户名查找用户
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isEmpty()) {
                throw new IllegalArgumentException("用户不存在");
            }

            User user = userOptional.get();
            // 验证邮箱是否匹配
            if (!email.equals(user.getEmail())) {
                throw new IllegalArgumentException("邮箱与用户不匹配");
            }

            // 重置密码
            userService.resetPassword(user.getUserId(), newPassword);
            return ResponseEntity.ok(Map.of(
                "message", "密码重置成功"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
