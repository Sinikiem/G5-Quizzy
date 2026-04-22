package com.quizzy.backend.service;

import com.quizzy.backend.model.Student;
import com.quizzy.backend.model.User;
import com.quizzy.backend.repository.StudentRepository;
import com.quizzy.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public UserService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
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

        // 2. If it's a student (default), create the Student profile
        if ("STUDENT".equalsIgnoreCase(savedUser.getRole())) {
            Student student = new Student();
            // Use the generated user_id for the student table link
            student.setUserId(savedUser.getUserId());
            student.setTotalScore(0);
            studentRepository.save(student);
        }

        return savedUser;
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> {
                    String storedPassword = user.getPasswordHash() != null ? user.getPasswordHash() : user.getPassword();
                    return password.equals(storedPassword);
                });
    }
}
