package com.web.service;


import com.tnsoft.web.model.Result;

public interface RegisterService {

    boolean isUsernameAble(String username); //查询用户名是否存在

    boolean isMobileAble(String mobile);    //查询手机号是否存在

    Result sendCode(String mobile);

    Result creatNewUserAndDomain(String username, String password, String phone, String roleId);
}
