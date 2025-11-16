package org.example.kaos.service;

import org.example.kaos.entity.User;

public interface IUserService {
    User login(String username, String password);
}
