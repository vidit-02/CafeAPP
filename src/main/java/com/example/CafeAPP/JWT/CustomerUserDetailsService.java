package com.example.CafeAPP.JWT;

import com.example.CafeAPP.dao.UserDao;
import com.example.CafeAPP.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Objects;

@Component
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    UserDao userDao;

    private User userDetails;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
       userDetails = userDao.findByEmailId(email);
       if(!Objects.isNull(userDetails)){
           return new org.springframework.security.core.userdetails.User(userDetails.getEmail(),userDetails.getPassword(),new ArrayList<>());
       }
       else{
           throw new UsernameNotFoundException("user not found");
       }
    }

    public User getUserDetails(){
        return userDetails;
    }
}
