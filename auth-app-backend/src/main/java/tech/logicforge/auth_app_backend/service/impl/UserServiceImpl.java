package tech.logicforge.auth_app_backend.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import tech.logicforge.auth_app_backend.dtos.UserDto;
import tech.logicforge.auth_app_backend.entity.Provider;
import tech.logicforge.auth_app_backend.entity.User;
import tech.logicforge.auth_app_backend.exception.ResourceNotFoundException;
import tech.logicforge.auth_app_backend.helper.UserHelper;
import tech.logicforge.auth_app_backend.repository.RoleRepository;
import tech.logicforge.auth_app_backend.repository.UserRepository;
import tech.logicforge.auth_app_backend.service.UserService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with given email already exists");
        }

        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        //TODO:
        //assign the default role
//        Role role = roleRepository.findByName("ROLE_" + AppConstants.GUEST_ROLE).orElse(null);
//        user.getRoles().add(role);


        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given email "+email+" id"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {

        UUID uId = UserHelper.parseUUID(userId);

        User existingUser = userRepository
                .findById(uId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with given "+uId+" id"));

        if (userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if (userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());

        //TODO: change password updation logic...
        if (userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());

        existingUser.setEnable(userDto.isEnable());
        existingUser.setUpdatedAt(Instant.now());

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {

        UUID uId = UserHelper.parseUUID(userId);

        User user = userRepository.findById(uId).orElseThrow(() ->
                new ResourceNotFoundException("User not found with given "+uId+" id"));

        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {

        User user = userRepository.findById(UserHelper.parseUUID(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given "+userId+" id"));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}
