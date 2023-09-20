package com.hj.hjBi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hj.hjBi.model.dto.user.UserQueryRequest;
import com.hj.hjBi.model.dto.user.UserUpdateMyRequest;
import com.hj.hjBi.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.hjBi.model.vo.LoginUserVO;
import com.hj.hjBi.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author: WHJ
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-09-11 15:07:56
*/
public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    User getLoginUserPermitNull(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User user);

    boolean userLogout(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    UserVO getUserVO(User user);

    List<UserVO> getUserVO(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request);
}
