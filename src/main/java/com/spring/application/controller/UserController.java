package com.spring.application.controller;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.spring.application.entities.Role;
import com.spring.application.entities.RoleName;
import com.spring.application.entities.User;
import com.spring.application.repositories.RoleRepository;
import com.spring.application.repositories.UserRepository;
import com.spring.application.request.SignUpForm;
import com.spring.application.response.ResponseMessage;
import com.spring.application.security.jwt.JwtProvider;

@RestController 
@CrossOrigin(origins = "*")

public class UserController {

		@Autowired
		private UserRepository service;
		  @Autowired
		  RoleRepository roleRepository;
		  
		  @Autowired
		  AuthenticationManager authenticationManager;
		 
		 
		  @Autowired
		  PasswordEncoder encoder;
		 
		  @Autowired
		  JwtProvider jwtProvider;
		 
		//AFFICHER
		@GetMapping("/user")
		public List<User> getUserList(){
			return service.findAll();
		}
		
		//Recherche
		@PreAuthorize("hasRole('ADMIN')")
		@GetMapping("/user/{id}")
	   public ResponseEntity<User> getUser(@PathVariable Long id) throws Exception{
				final User use = service.findById(id).orElseThrow(()->new Exception("L'utilisateur  n'existe pas"));
				return ResponseEntity.ok().body(use);
		}
		
		//ADD
		@PreAuthorize("hasRole('ADMIN')")
		@PostMapping("/user")
		
		  public ResponseEntity<?> addUser(@Valid @RequestBody SignUpForm signUpRequest) {
		    if (service.existsByUsername(signUpRequest.getUsername())) {
		      return new ResponseEntity<>(new ResponseMessage("Fail -> Username is already taken!"),
		          HttpStatus.BAD_REQUEST);
		    }
		 
		    if (service.existsByEmail(signUpRequest.getEmail())) {
		      return new ResponseEntity<>(new ResponseMessage("Fail -> Email is already in use!"),
		          HttpStatus.BAD_REQUEST);
		    }
		 
		    // Creating user's account
		    User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
		        encoder.encode(signUpRequest.getPassword()));
		 
		    Set<String> strRoles = signUpRequest.getRole();
		    Set<Role> roles = new HashSet<>();
		 
		    strRoles.forEach(role -> {
		      switch (role) {
		      case "admin":
		        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
		            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
		        roles.add(adminRole);
		 
		        break;
		      case "pm":
		        Role pmRole = roleRepository.findByName(RoleName.ROLE_PM)
		            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
		        roles.add(pmRole);
		 
		        break;
		      default:
		        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
		            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
		        roles.add(userRole);
		      }
		    });
		 
		    user.setRoles(roles);
		    service.save(user);
		 
		    return new ResponseEntity<>(new ResponseMessage("User registered successfully!"), HttpStatus.OK);
		  }
		
		//UPDATE
		@PreAuthorize("hasRole('ADMIN')")
		@PutMapping("/user/{id}")
	    public ResponseEntity<User> updateUser(@PathVariable Long id,@Valid @RequestBody User details) throws Exception{
		User user = service.findById(id).orElseThrow(()->new Exception("l'utilisateur  n'existe pas"));
			user.setId(details.getId());
		 	user.setUsername(details.getUsername());
		 	user.setName(details.getName());
		 	user.setEmail(details.getEmail());
		 	
		 	user.setPassword(encoder.encode(details.getPassword()));
		 	user.setRoles(details.getRoles());
		 	 User updateUs = service.save(user);
		     return ResponseEntity.ok(updateUs);
	    }
		
		

		//Delete
		@PreAuthorize("hasRole('ADMIN')")
		@DeleteMapping("/user/{id}")
		 public Map<String,Boolean> deleteUser(@PathVariable Long id) throws Exception {
		    User user = service.findById(id).orElseThrow(()->new Exception("L'utilisateur n'est pas trouvé"));
		    service.delete(user);
		    Map<String,Boolean> response = new HashMap<>();
		    response.put("L'utilisateur  est supprimé!",Boolean.TRUE);
		    return response;
		   }

			
		}
		
		
		

		
