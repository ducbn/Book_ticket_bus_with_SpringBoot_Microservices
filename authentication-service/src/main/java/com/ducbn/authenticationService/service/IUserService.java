package com.ducbn.authenticationService.service;


import com.ducbn.authenticationService.dto.UserRegisterDTO;
import com.ducbn.authenticationService.model.User;

public interface IUserService {
    User registerUser(UserRegisterDTO userDTO) throws Exception;
    String login(String phoneNumber, String password, Long roleId) throws Exception;
}