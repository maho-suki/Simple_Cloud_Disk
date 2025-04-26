package com.maho.simple_network_disk;

import com.maho.simple_network_disk.entity.User;
import com.maho.simple_network_disk.repository.UserRepository;
import com.maho.simple_network_disk.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.attribute.UserPrincipal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SimpleNetworkDiskApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserService userService;

    @Test
    public void test_DeleteUser() {
        User testUser1 = userService.registerUser("test_Username1", "test_password", "test_email1");
        User testUser2 = userService.registerUser("test_username2", "test_password", "test_email2");
        //不会喵，好像是创建测试专用的数据库用的，问ai写的测试代码喵
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        long UserId1 = testUser1.getUserId();
        long UserId2 = testUser2.getUserId();
        //assertThat(userService.DeleteUser(UserId1,"test_password")).isTrue();
        //断言，测试用的，通过了就不报错
    }
}