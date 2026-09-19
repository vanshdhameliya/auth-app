package tech.logicforge.auth_app_backend.service;

import tech.logicforge.auth_app_backend.dtos.UserDto;

public interface AuthService {

    UserDto registerUser(UserDto userDto);


}
