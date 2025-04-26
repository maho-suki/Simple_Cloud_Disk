package com.maho.simple_network_disk.service;

import com.maho.simple_network_disk.dto.UserRegistrationDto;
import com.maho.simple_network_disk.entity.User;
import com.maho.simple_network_disk.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.io.FileUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.naming.AuthenticationException;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.storage.root}")
    private String storageRoot;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String UserName, String password, String email) {
        // 检查用户名、密码、邮箱是否为空
        if (UserName == null || UserName.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // 检查用户名、邮箱是否已存在
        if (userRepository.findByUsername(UserName).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }
        User user = new User();
        user.setUsername(UserName);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        return userRepository.save(user);
    }

    public void DeleteUser(Long userId, String password) {
        // 获取用户
        User user = getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 删除用户的物理文件
        Path userStorageDir = Paths.get(storageRoot).resolve(userId.toString());
        try {
            if (Files.exists(userStorageDir)) {
                FileUtils.deleteDirectory(userStorageDir.toFile());
            }
        } catch (IOException e) {
            throw new IllegalStateException("删除用户文件失败", e);
        }

        // 删除用户（由于配置了级联删除，会自动删除关联的文件记录）
        userRepository.delete(user);
    }

    public boolean ChangeUserName(Long UserId, String NewUsername) {
        Optional<User> existingUser = userRepository.findById(UserId);
        // 检查两个用户名是否为空
        if (NewUsername == null || NewUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("NewUsername cannot be empty");
        }
        //检查新用户名是否已经存在
        if (userRepository.findByUsername(NewUsername).isPresent()) {
            throw new IllegalStateException("NewUsername already exists");
        }
        if (existingUser.isEmpty()) {
            throw new IllegalStateException("Username not found");
        }
        User currentUser = existingUser.get();
        currentUser.setUsername(NewUsername);
        userRepository.save(currentUser);
        return true;
    }

    public boolean ChangePassword(Long UserId, String oldPassword, String newPassword) {
        // 检查是否存在该用户
        Optional<User> user = userRepository.findById(UserId);
        if(user.isEmpty()){
            throw new IllegalStateException("User not found");
        }
        User currentUser = user.get();
        //检查旧密码是否正确
        if(!passwordEncoder.matches(oldPassword, currentUser.getPassword())){
            throw new IllegalStateException("Old password does not match");
        }
        //检查新旧密码是否相同
        if(oldPassword.equals(newPassword)){
            throw new IllegalStateException("The new password is the same as the old password");
        }

        //设置新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(encodedPassword);
        userRepository.save(currentUser);
        return true;
    }

    public boolean ChangeEmail(Long userId, String email) {
        Optional<User> user = userRepository.findById(userId);
        // 检查是否存在该用户
        if(user.isEmpty()){
            throw new IllegalStateException("User not found");
        }
        // 检测邮箱输入是否为空
        if(email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        // 检测新邮箱是否已经被使用
        Optional<User> existingUser = userRepository.findByEmail(email);
        if(existingUser.isPresent()){
            throw new IllegalStateException("Email already has been used");
        }
        User currentUser = user.get();
        currentUser.setEmail(email);
        userRepository.save(user.get());
        return true;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserById(Long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isEmpty()) {
            throw new IllegalStateException("User not found");
        }
        return existingUser.get();
    }

    //DTO接口，据说是软件开发常用的
    public User registerUser(UserRegistrationDto dto) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 密码加密
        user.setEmail(dto.getEmail());
        user.setRegisterTime(new Date());
        return userRepository.save(user);
    }

    public User authenticate(String username, String password) throws AuthenticationException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));
        
        // 验证密码（假设密码已经加密存储）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("用户名或密码错误");
        }
        
        return user;
    }
    
    public boolean resetPassword(Long userId, String newPassword) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            throw new IllegalStateException("User not found");
        }
        
        User currentUser = user.get();
        String encodedPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(encodedPassword);
        userRepository.save(currentUser);
        return true;
    }
}