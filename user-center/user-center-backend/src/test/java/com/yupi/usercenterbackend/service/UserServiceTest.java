package com.yupi.usercenterbackend.service;
import java.util.Date;

import com.yupi.usercenterbackend.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author sonder
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("路飞");
        user.setUserAccount("lufei");
        user.setAvatarUrl("https://image.baidu.com/search/detail?ct=503316480&z=0&tn=baiduimagedetail&ipn=d&cl=2&cm=1&sc=0&sa=vs_ala_img_datu&lm=-1&ie=utf8&pn=3&rn=1&di=7500620934571622401&ln=0&word=%E8%B7%AF%E9%A3%9E%E5%A4%B4%E5%83%8F&os=127839022,1739150230&cs=3921601399,3208517994&objurl=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2Fb0d634f9-209e-49a1-97eb-cff44f9d01c7%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&bdtype=0&simid=3921601399,3208517994&pi=0&adpicid=0&timingneed=&spn=0&is=127839022,1739150230&lid=831aac2600754f21");
        user.setGender(0);
        user.setUserPassword("123");
        user.setEmail("1234@qq.com");
        user.setPhone("123456789");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        assertTrue(result);
    }

    @Test
    void userRegister() {
        // 密码不能为空
        String userAccount = "admin";
        String userPassword = "";
        String checkPassword = "12345678";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        assertEquals(-1, result);

        // 账户长度不能少于4位
        userAccount = "luo";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        assertEquals(-1, result);

        // 密码长度不能少于8位
        userAccount = "luobin";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        assertEquals(-1, result);

        // 账户不能包含特殊字符
        userAccount = "luo bin";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        assertEquals(-1, result);

        // 密码和校验密码必须相同
        userAccount = "luobin";
        userPassword = "12345678";
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        assertEquals(-1, result);

        // 账户不能重复
        userAccount = "lufei";
        userPassword = "12345678";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        assertEquals(-1, result);

        // 测试成功
        userAccount = "admin";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        assertTrue(result > 0);

    }
}