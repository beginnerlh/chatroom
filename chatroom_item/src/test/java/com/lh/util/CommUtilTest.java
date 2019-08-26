package com.lh.util;

import com.lh.client.entity.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static com.lh.util.CommUtil.obj2Json;

public class CommUtilTest {

    @Test
    public void loadProperties() {
        Properties pros = CommUtil.loadProperties("db.properties");
        Assert.assertNotNull(pros);
    }

    @Test
    public void obj2JsonTest(){
        User user = new User();
        user.setId(1);
        user.setUserName("12");
        user.setPassword("123");
        user.setBrief("dd");
        System.out.println(obj2Json(user));
    }

    @Test
    public void json2ObjTest(){
        User user =(User) CommUtil.json2obj("{\"id\":1,\"userName\":\"12\",\"password\":\"123\",\"brief\":\"dd\"}", User.class);
        System.out.println(user);
    }
}