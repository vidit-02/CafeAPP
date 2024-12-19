package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.JWT.CustomerUserDetailsService;
import com.example.CafeAPP.JWT.JwtFilter;
import com.example.CafeAPP.JWT.JwtUtil;
import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.UserDao;
import com.example.CafeAPP.model.User;
import com.example.CafeAPP.service.UserService;
import com.example.CafeAPP.utils.CafeUtils;
import com.example.CafeAPP.utils.EmailUtils;
import com.example.CafeAPP.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j //for logging purposes
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        //log.info("Inside signUp {}", requestMap);
        try{
            if(validateSignUpMap(requestMap)){
                User user = userDao.findByEmailId(requestMap.get("email"));
                if(Objects.isNull(user)){
                    userDao.save(getUserFromMap(requestMap));  //will directly save user to DB using jpa repos dependency no need to write query
                    return CafeUtils.getResponseEntity(CafeConstants.SUCCESSFULLY_REGISTERED, HttpStatus.OK);
                }
                else{
                    return CafeUtils.getResponseEntity(CafeConstants.USER_ALREADY_EXIST,HttpStatus.BAD_REQUEST);
                }
            }
            else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String,String> requestMap){
        return requestMap.containsKey("name") && requestMap.containsKey("contactNumber") && requestMap.containsKey("email") && requestMap.containsKey("password");
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            Authentication authentication = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password")));
            if(authentication.isAuthenticated()){
                if(customerUserDetailsService.getUserDetails().getStatus().equalsIgnoreCase("true")) {//if true means the user is authenticated
                    return new ResponseEntity<String>("{\"token\" : \"" + jwtUtil.generateToken(customerUserDetailsService.getUserDetails().getEmail(),customerUserDetailsService.getUserDetails().getRole()) + "\"}", HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"message\" : \"" + "Wait for admin approval." + "\"}", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception ex){
            log.error("{}",ex);
        }
        return CafeUtils.getResponseEntity(CafeConstants.BAD_CREDENTIALS,HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers(){
        log.info("Inside getAllUsers");
        try{
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userDao.getAllUsers(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>( new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        log.info("Inside update status function");
        try{
            if(jwtFilter.isAdmin()){
                Optional<User> optionalUser = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if(optionalUser.isPresent()){
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmins(requestMap.get("status"),optionalUser.get().getEmail(),userDao.getAllAdminsEmail());
                    return CafeUtils.getResponseEntity(CafeConstants.USER_STATUS_UPDATED_SUCCESSFULLY, HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.USER_NOT_FOUND,HttpStatus.OK);
                }

            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> checkToken() {  //will be used when trying to access different pages based on user role
        log.info("Inside check token function");
        return CafeUtils.getResponseEntity("true",HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        log.info("Inside changePassword function");
        try{
            User user = userDao.findByEmailId(jwtFilter.getCurrentUser());
            if(user != null){
                if(user.getPassword().equals(requestMap.get("oldPassword"))){
                    user.setPassword(requestMap.get("newPassword"));
                    userDao.save(user);
                    return CafeUtils.getResponseEntity(CafeConstants.PASSWORD_CHANGED_SUCCESSFULLY, HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(CafeConstants.INCORRECT_OLD_PASSWORD, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        log.info("Inside forgotPassword function");
        try{
            User user = userDao.findByEmailId(requestMap.get("email"));
            if(user != null){
                emailUtils.forgotMail(user.getEmail(),"Credentials by Cafe Managment System",user.getPassword());
            }
            return CafeUtils.getResponseEntity("Check you mail for credentials", HttpStatus.OK); //outside if block so that even if user not registered it sends the same link
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmins(String status, String email, List<String> allAdminsEmail) {
        log.info("Inside send mail function");
        allAdminsEmail.remove(jwtFilter.getCurrentUser());  //to remove the admin from the cc list of all admins, as his name will already be there in the destination
        if(status!= null && status.equalsIgnoreCase("true")){  //user is enabled
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"Account Approved", "user : "+email+"\n is approved by \nADMIN:"+jwtFilter.getCurrentUser(),allAdminsEmail);
        } else { //in case user is disabled
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"Account Disabled", "user : "+email+"\n is disabled by \nADMIN:"+jwtFilter.getCurrentUser(),allAdminsEmail);
        }
    }
}
