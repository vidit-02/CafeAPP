package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.JWT.CustomerUserDetailsService;
import com.example.CafeAPP.JWT.JwtFilter;
import com.example.CafeAPP.JWT.JwtUtil;
import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.UserDao;
import com.example.CafeAPP.exception.CafeException;
import com.example.CafeAPP.model.User;
import com.example.CafeAPP.service.UserService;
import com.example.CafeAPP.utils.CafeUtils;
import com.example.CafeAPP.utils.EmailUtils;
import com.example.CafeAPP.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j //for logging purposes
@Service
public class UserServiceImpl implements UserService {

    //private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
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
                    throw new CafeException(CafeConstants.USER_ALREADY_EXIST,HttpStatus.BAD_REQUEST);
                }
            }
            else {
                throw new CafeException(CafeConstants.INVALID_DATA,HttpStatus.BAD_REQUEST);
            }

        }  catch (DataAccessException ex) {

            log.error("Database error while trying to save user", ex);

            throw new CafeException(
                    "Unable to sign up user",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while trying to sign up", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
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
        log.info("inside login :: {}", requestMap);

        try {

            // Validate request input
            if (requestMap == null
                    || requestMap.get("email") == null
                    || requestMap.get("password") == null
                    || requestMap.get("email").trim().isEmpty()
                    || requestMap.get("password").trim().isEmpty()) {

                throw new CafeException(
                        CafeConstants.INVALID_DATA,
                        HttpStatus.BAD_REQUEST
                );
            }

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    requestMap.get("email"),
                                    requestMap.get("password")
                            )
                    );

            // Approved user check
            if (!customerUserDetailsService
                    .findUserByEmail(requestMap.get("email"))
                    .getStatus()
                    .equalsIgnoreCase("true")) {

                throw new CafeException(
                        "Wait for admin approval.",
                        HttpStatus.FORBIDDEN
                );
            }

            String token =
                    jwtUtil.generateToken(
                            customerUserDetailsService
                                    .findUserByEmail(requestMap.get("email"))
                                    .getEmail(),

                            customerUserDetailsService
                                    .findUserByEmail(requestMap.get("email"))
                                    .getRole()
                    );

            return ResponseEntity.ok(
                    "{\"token\":\"" + token + "\"}"
            );

        }  catch (BadCredentialsException ex) {

            throw new CafeException(
                    CafeConstants.BAD_CREDENTIALS,
                    HttpStatus.UNAUTHORIZED
            );

        } catch (DisabledException ex) {

            throw new CafeException(
                    "Account disabled",
                    HttpStatus.FORBIDDEN
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error during login", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        log.info("inside getAllUsers");
        //to get users with user role only..

        try {

            if (!jwtFilter.isAdmin()) {
                throw new CafeException(
                        CafeConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED
                );
            }

            return ResponseEntity.ok(
                    userDao.getAllUsers()
            );

        } catch (DataAccessException ex) {

            log.error("Database error while fetching users", ex);

            throw new CafeException(
                    "Unable to fetch users",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while fetching users", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        log.info("inside updateStatus :: {}", requestMap);

        try {

            if (!jwtFilter.isAdmin()) {
                throw new CafeException(
                        CafeConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED
                );
            }

            Integer id = Integer.parseInt(requestMap.get("id"));

            Optional<User> optionalUser = userDao.findById(id);

            if (optionalUser.isEmpty()) {
                throw new CafeException(
                        CafeConstants.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }

            userDao.updateStatus(
                    requestMap.get("status"),
                    id
            );

            sendMailToAllAdmins(
                    requestMap.get("status"),
                    optionalUser.get().getEmail(),
                    userDao.getAllAdminsEmail()
            );

            return CafeUtils.getResponseEntity(
                    CafeConstants.USER_STATUS_UPDATED_SUCCESSFULLY,
                    HttpStatus.OK
            );

        } catch (NumberFormatException ex) {

            throw new CafeException(
                    "Invalid user id",
                    HttpStatus.BAD_REQUEST
            );

        } catch (DataAccessException ex) {

            log.error("Database error while updating user status", ex);

            throw new CafeException(
                    "Unable to update user status",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while updating user status", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {  //will be used when trying to access different pages based on user role
        log.info("Inside check token function");
        return CafeUtils.getResponseEntity("true",HttpStatus.OK);
    }


    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        log.info("inside changePassword");

        try {

            User user = userDao.findByEmailId(
                    jwtFilter.getCurrentUser()
            );

            if (user == null) {
                throw new CafeException(
                        CafeConstants.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }

            if (!user.getPassword().equals(
                    requestMap.get("oldPassword"))) {

                throw new CafeException(
                        CafeConstants.INCORRECT_OLD_PASSWORD,
                        HttpStatus.BAD_REQUEST
                );
            }

            user.setPassword(
                    requestMap.get("newPassword")
            );

            userDao.save(user);

            return CafeUtils.getResponseEntity(
                    CafeConstants.PASSWORD_CHANGED_SUCCESSFULLY,
                    HttpStatus.OK
            );

        } catch (DataAccessException ex) {

            log.error("Database error while changing password", ex);

            throw new CafeException(
                    "Unable to change password",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while changing password", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        log.info("inside forgotPassword :: {}", requestMap);

        try {

            User user = userDao.findByEmailId(
                    requestMap.get("email")
            );

            if (user != null) {
                emailUtils.forgotMail(
                        user.getEmail(),
                        "Credentials by Cafe Management System",
                        user.getPassword()
                );
            }

            return CafeUtils.getResponseEntity(
                    "Check your mail for credentials",
                    HttpStatus.OK
            );

        } catch (MailException ex) {

            log.error("Email error while forgot password", ex);

            throw new CafeException(
                    "Unable to send email",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {

            log.error("Unexpected error while forgot password", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void sendMailToAllAdmins(
            String status,
            String email,
            List<String> allAdminsEmail) {

        log.info("inside sendMailToAllAdmins");

        try {

            allAdminsEmail.remove(
                    jwtFilter.getCurrentUser()
            );

            if ("true".equalsIgnoreCase(status)) {

                emailUtils.sendSimpleMessage(
                        jwtFilter.getCurrentUser(),
                        "Account Approved",
                        "user : " + email
                                + "\n is approved by \nADMIN:"
                                + jwtFilter.getCurrentUser(),
                        allAdminsEmail
                );

            } else {

                emailUtils.sendSimpleMessage(
                        jwtFilter.getCurrentUser(),
                        "Account Disabled",
                        "user : " + email
                                + "\n is disabled by \nADMIN:"
                                + jwtFilter.getCurrentUser(),
                        allAdminsEmail
                );
            }

        } catch (Exception ex) {

            log.error("Error while sending admin mail", ex);

            throw new CafeException(
                    "Unable to send notification email",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
