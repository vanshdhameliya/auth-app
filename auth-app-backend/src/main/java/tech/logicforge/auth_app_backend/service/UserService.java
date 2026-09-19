package tech.logicforge.auth_app_backend.service;

import tech.logicforge.auth_app_backend.dtos.UserDto;

import java.util.UUID;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UserDto userDto, String userId);

    void deleteUser(String userId);

    UserDto getUserById(String userId);

    Iterable<UserDto> getAllUsers();
}
