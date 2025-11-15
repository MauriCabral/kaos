package org.example.kaos.service.implementation;

import org.example.kaos.entity.User;
import org.example.kaos.repository.UserRepository;
import org.example.kaos.service.UserService;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository = new UserRepository();

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
