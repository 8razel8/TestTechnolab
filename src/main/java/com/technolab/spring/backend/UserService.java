package com.technolab.spring.backend;

import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userService")
public class UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findUserByName(String name) {
        return userRepository.findByName(name);
    }

    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
        Broadcaster.broadcast(new CrudMessage(CrudMessage.DML.DELETE, user));
    }

    public void updateUser(User user) {
        user = userRepository.save(user);
        Broadcaster.broadcast(new CrudMessage(CrudMessage.DML.UPDATE, user));
    }

    public void insertUser(User user) {
        user = userRepository.save(user);
        Broadcaster.broadcast(new CrudMessage(CrudMessage.DML.INSERT, user));
    }
}