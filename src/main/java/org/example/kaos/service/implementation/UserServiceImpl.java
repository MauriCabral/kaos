package org.example.kaos.service.implementation;

import org.example.kaos.entity.User;
import org.example.kaos.repository.UserRepository;
import org.example.kaos.service.IUserService;
import org.example.kaos.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository = new UserRepository();

    @Override
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login attempt with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Login attempt with null or empty password for username: {}", username);
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        User user = userRepository.findByUsername(username);
        if (user != null) {
            String storedPassword = user.getPassword();
            boolean passwordMatches = false;
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // Hashed password
                passwordMatches = PasswordUtil.verifyPassword(password, storedPassword);
            } else {
                // Plain text password (for initial data)
                passwordMatches = password.equals(storedPassword);
            }
            if (passwordMatches) {
                logger.info("Successful login for user: {}", username);
                return user;
            } else {
                logger.warn("Failed login attempt for user: {} - invalid password", username);
            }
        } else {
            logger.warn("Failed login attempt - user not found: {}", username);
        }
        return null;
    }

    /**
     * Creates a new user with hashed password.
     *
     * @param user the user to create, password should be plain text
     * @return the created user
     */
    public User createUser(User user) {
        if (user == null || user.getPassword() == null) {
            throw new IllegalArgumentException("User and password cannot be null");
        }
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }
}
