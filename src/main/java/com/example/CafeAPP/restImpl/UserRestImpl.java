package com.example.CafeAPP.restImpl;

import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.rest.UserRest;
import com.example.CafeAPP.service.UserService;
import com.example.CafeAPP.utils.CafeUtils;
import com.example.CafeAPP.wrapper.UserWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class UserRestImpl implements UserRest {

    @Autowired
    UserService userService;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        return userService.signUp(requestMap);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
            return userService.login(requestMap);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        return userService.getAllUsers();
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestmap) {
        return userService.updateStatus(requestmap);
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return userService.checkToken();
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        return userService.changePassword(requestMap);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        return userService.forgotPassword(requestMap);
    }
}
