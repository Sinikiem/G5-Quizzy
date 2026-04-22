package com.quizzy.backend.service;

import com.quizzy.backend.model.Guardian;
import com.quizzy.backend.model.Student;
import com.quizzy.backend.model.User;
import com.quizzy.backend.repository.GuardianRepository;
import com.quizzy.backend.repository.StudentRepository;
import com.quizzy.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;

    public UserService(UserRepository userRepository, StudentRepository studentRepository, GuardianRepository guardianRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.guardianRepository = guardianRepository;
    }

    @Transactional
    public User registerUser(User user) throws Exception {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new Exception("This username already exists");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new Exception("Email is required");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("This email already exists");
        }

        // Mapping 'password' to 'password_hash' column
        user.setPasswordHash(user.getPassword());
        
        // 1. Save User
        User savedUser = userRepository.save(user);

        // 2. Create profile based on role
        if ("STUDENT".equalsIgnoreCase(savedUser.getRole())) {
            Student student = new Student();
            student.setUserId(savedUser.getUserId());
            student.setTotalScore(0);
            studentRepository.save(student);
        } else if ("GUARDIAN".equalsIgnoreCase(savedUser.getRole())) {
            Guardian guardian = new Guardian();
            guardian.setUserId(savedUser.getUserId());
            guardianRepository.save(guardian);
        }

        return savedUser;
    }

    public User login(String username, String password) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("This account doesn't exist"));

        String storedPassword = user.getPasswordHash() != null ? user.getPasswordHash() : user.getPassword();
        if (!password.equals(storedPassword)) {
            throw new Exception("Incorrect password");
        }
        return user;
    }
}
