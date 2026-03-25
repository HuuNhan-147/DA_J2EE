package com.example.DACK_J2EE.service;

import com.example.DACK_J2EE.dto.*;
import com.example.DACK_J2EE.entity.User;
import com.example.DACK_J2EE.repository.UserRepository;
import com.example.DACK_J2EE.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponseDTO createUser(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .isAdmin(false)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateTokenFromId(savedUser.getId().toString());

        return AuthResponseDTO.builder()
                .message("Account created successfully!")
                .accessToken(accessToken)
                .user(convertToProfileDTO(savedUser))
                .build();
    }

    public AuthResponseDTO loginUser(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Account does not exist!"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password is incorrect!");
        }

        String accessToken = jwtTokenProvider.generateTokenFromId(user.getId().toString());

        return AuthResponseDTO.builder()
                .message("Login successful!")
                .accessToken(accessToken)
                .user(convertToProfileDTO(user))
                .build();
    }

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist!"));

        return convertToProfileDTO(user);
    }

    public UserProfileDTO getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist!"));

        return convertToProfileDTO(user);
    }

    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    public List<UserProfileDTO> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        
        String searchKeyword = "%" + keyword.toLowerCase() + "%";
        return userRepository.findAll().stream()
                .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                               user.getPhone().contains(keyword))
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    public UserProfileDTO updateUserProfile(Long userId, UserUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist!"));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return convertToProfileDTO(updatedUser);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist!"));
        userRepository.delete(user);
    }

    public UserProfileDTO updateUserByAdmin(Long userId, UserUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist!"));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return convertToProfileDTO(updatedUser);
    }

    private UserProfileDTO convertToProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isAdmin(user.getIsAdmin())
                .build();
    }
}
