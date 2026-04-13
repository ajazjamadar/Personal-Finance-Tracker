package com.qburst.training.personalfinancetracker.service.user;

import com.qburst.training.personalfinancetracker.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto.Response createUser(UserDto.Request request);
    UserDto.Response getUserById(Long id);
    UserDto.Response updateUser(Long id, UserDto.UpdateRequest request);
    List<UserDto.Response> getAllUsers();
}
