package com.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.cafe.JWT.CustomerUserDetailsService;
import com.cafe.JWT.JwtFilter;
import com.cafe.JWT.JwtUtil;
import com.cafe.entities.User;
import com.cafe.constants.CafeConstants;
import com.cafe.repository.UserRepository;
import com.cafe.service.UserService;
import com.cafe.utils.CafeUtil;
import com.cafe.utils.EmailUtil;
import com.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

	

	    @Autowired
	    UserRepository userRepository;

	    @Autowired
	    AuthenticationManager authenticationManager;
	    
	    @Autowired
	    JwtUtil jwtUtil;

	    @Autowired
	    JwtFilter jwtFilter;

	    @Autowired
	    CustomerUserDetailsService customerUserDetailsService;

	    @Autowired
	    EmailUtil emailUtil;

	    @Override
	    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
	        log.info("Inside signup {}", requestMap);
	        try {
	            if (validaSignUpMap(requestMap)) {
	                //System.out.println("inside validaSignUpMap");
	                User user = userRepository.findByEmailId(requestMap.get("email"));
	                if (Objects.isNull(user)) {
	                	userRepository.save(getUserFromMap(requestMap));
	                    //System.out.println("Successfully  Registered.");
	                    return CafeUtil.getResponeEntity("Successfully  Registered.", HttpStatus.OK);
	                } else {
	                    //System.out.println("Email already exits.");
	                    return CafeUtil.getResponeEntity("Email already exits.", HttpStatus.BAD_REQUEST);
	                }
	            } else {
	                //System.out.println(CafeConstants.INVALID_DATA);
	                return CafeUtil.getResponeEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @Override
	    public ResponseEntity<String> login(Map<String, String> requestMap) {
	        log.info("Inside login {}", requestMap);
	        try {
	            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
	            if (auth.isAuthenticated()) {
	                if (customerUserDetailsService.getUserDatails().getStatus().equalsIgnoreCase("true")) {
	                    return new ResponseEntity<String>("{\"token\":\"" + jwtUtil.generateToken(
	                            customerUserDetailsService.getUserDatails().getEmail(), customerUserDetailsService.getUserDatails().getRole()) + "\"}",
	                            HttpStatus.OK);
	                } else {
	                    return new ResponseEntity<String>("{\"message\":\"" + "Wait for Admin Approvel." + "\"}",
	                            HttpStatus.BAD_REQUEST);
	                }
	            }
	        } catch (Exception ex) {
	            log.error("{}", ex);
	        }
	        return new ResponseEntity<String>("{\"message\":\"" + "Bad Credentials." + "\"}",
	                HttpStatus.BAD_REQUEST);
	    }


	    private boolean validaSignUpMap(Map<String, String> requestMap) {
	        if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber") && requestMap.containsKey("email") && requestMap.containsKey("password")) {
	            return true;
	        }
	        return false;
	    }

	    private User getUserFromMap(Map<String, String> requestMap) {
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
	    public ResponseEntity<List<UserWrapper>> getAllUser() {
	        try {
	            if (jwtFilter.isAdmin()) {
	                return new ResponseEntity<>(userRepository.getAllUser(), HttpStatus.OK);
	            } else {
	                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
	            }

	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @Override
	    public ResponseEntity<String> update(Map<String, String> requestMap) {
	        try {
	            if (jwtFilter.isAdmin()) {
	                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get("id")));
	                if (!optional.isEmpty()) {

	                	userRepository.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
	                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userRepository.getAllAdmin());
	                    return CafeUtil.getResponeEntity("User Status is updated Successfully", HttpStatus.OK);

	                } else {
	                    return CafeUtil.getResponeEntity("User id doesn't exist", HttpStatus.OK);
	                }
	            } else {
	                return CafeUtil.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @Override
	    public ResponseEntity<String> checkToken() {
	        return CafeUtil.getResponeEntity("true", HttpStatus.OK);
	    }

	    @Override
	    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
	        try {
	            User user = userRepository.findByEmail(jwtFilter.getCurrentUsername());
	            if (!user.equals(null)) {
	                if (user.getPassword().equals(requestMap.get("oldPassword"))) {
	                    user.setPassword(requestMap.get("newPassword"));
	                    userRepository.save(user);
	                    return CafeUtil.getResponeEntity("Password Updated Successfully", HttpStatus.OK);
	                }
	                return CafeUtil.getResponeEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
	            }
	            return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @Override
	    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
	        System.out.println("inside the forgot password function");
	        try {
	            User user = userRepository.findByEmail(requestMap.get("email"));
	            System.out.println("user email is : " + user.getEmail());
	            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
	                //System.out.println("11");
	                emailUtil.forgetMail(user.getEmail() , "Credentials by Cafe Management System" , user.getPassword());
	                return CafeUtil.getResponeEntity("Check Your mail for Credentials", HttpStatus.OK);
	            }

	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	    }


	    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
	        allAdmin.remove(jwtFilter.getCurrentUsername());
	        if (status != null && status.equalsIgnoreCase("true")) {
	            emailUtil.SendSimpleMessage(jwtFilter.getCurrentUsername(), "Account Approved", "USER:- " + user + "\n is approved by\nADMIN:-" + jwtFilter.getCurrentUsername(), allAdmin);
	        } else {
	            emailUtil.SendSimpleMessage(jwtFilter.getCurrentUsername(), "Account Disabled", "USER:- " + user + "\n is disabled by\nADMIN:-" + jwtFilter.getCurrentUsername(), allAdmin);

	        }
	    }

	}


