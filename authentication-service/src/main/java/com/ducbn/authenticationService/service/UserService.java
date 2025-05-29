package com.ducbn.authenticationService.service;

import com.ducbn.authenticationService.component.JwtTokenUtils;
import com.ducbn.authenticationService.dto.UserRegisterDTO;
import com.ducbn.authenticationService.exception.DataNotFoundException;
import com.ducbn.authenticationService.exception.InvalidParamException;
import com.ducbn.authenticationService.exception.PermissionDenyException;
import com.ducbn.authenticationService.model.Role;
import com.ducbn.authenticationService.model.User;
import com.ducbn.authenticationService.repository.RoleRepository;
import com.ducbn.authenticationService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User registerUser(UserRegisterDTO userDTO) throws Exception{
        //register user
        String phoneNumber = userDTO.getPhoneNumber();
        String email = userDTO.getEmail();

        //check phone  number existing.
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        //check email existing.
        if(userRepository.existsByEmail(email)){
            throw new DataIntegrityViolationException("Email already exists");
        }

        //check role user
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        if(role.getName().equals(Role.ADMIN)){
            throw new PermissionDenyException("You cannot register a new admin account");
        }

        // check password match with retypePassword
        if (!Objects.equals(userDTO.getPassword(), userDTO.getRetypePassword())) {
            throw new InvalidParamException("Passwords do not match");
        }

        //convert from userDTO => user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(phoneNumber)
                .email(email)
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .build();

        newUser.setRole(role);

        String password = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password, Long roleId) throws Exception {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()){
            throw new DataNotFoundException("Invalid phone number/password");
        }

        //return optionalUser.get(); // want return JWT token
        User existingUser = optionalUser.get();

        if(!passwordEncoder.matches(password, existingUser.getPassword())){
            throw new BadCredentialsException("Wrong password or phone number");
        }

        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isEmpty() || !roleId.equals(existingUser.getRole().getId())) {
            throw new DataNotFoundException("Role not found/role id wrong");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber,
                password,
                existingUser.getAuthorities()
        );

        //auth with Java spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }
}
