package tech.logicforge.auth_app_backend.service;

import tech.logicforge.auth_app_backend.paylod.UserDto;

public interface AuthService {

    UserDto registerUser(UserDto userDto);


}
