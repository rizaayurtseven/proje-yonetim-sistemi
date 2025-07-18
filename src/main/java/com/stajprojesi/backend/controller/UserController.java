package com.stajprojesi.backend.controller;

import com.stajprojesi.backend.model.User;
import com.stajprojesi.backend.model.Role;
import com.stajprojesi.backend.repository.UserRepository;
import com.stajprojesi.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Bu import'u ekleyin
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users") // Admin'e özel endpoint
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Sadece ADMIN rolüne sahip kullanıcılar bu endpoint'e erişebilir
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Bu anotasyon yetkilendirmeyi sağlar
    public ResponseEntity<?> createUser(@RequestBody User newUser) {
        String username = newUser.getUsername() != null ? newUser.getUsername().trim() : null;
        String email = newUser.getEmail() != null ? newUser.getEmail().trim() : null;
        String password = newUser.getPassword() != null ? newUser.getPassword().trim() : null;

        if (username == null || username.isEmpty() || password == null || password.isEmpty() || email == null || email.isEmpty()) {
            return new ResponseEntity<>("Kullanıcı adı, şifre ve e-posta boş bırakılamaz.", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return new ResponseEntity<>("Kullanıcı adı zaten mevcut.", HttpStatus.CONFLICT); // 409 Conflict
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return new ResponseEntity<>("E-posta zaten kayıtlı.", HttpStatus.CONFLICT); // 409 Conflict
        }

        // Yeni kullanıcının şifresini hash'le
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setUsername(username);
        newUser.setEmail(email);

        // Varsayılan olarak "USER" rolünü ata (veya istekten gelen rolü işle)
        Optional<Role> userRoleOptional = roleRepository.findByName("USER");
        if (userRoleOptional.isEmpty()) {
            return new ResponseEntity<>("Varsayılan 'USER' rolü bulunamadı, lütfen rol tablosunu kontrol edin.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        newUser.setRoles(Collections.singleton(userRoleOptional.get())); // Sadece USER rolü ata

        // Kullanıcıyı kaydet
        userRepository.save(newUser);

        return new ResponseEntity<>("Kullanıcı başarıyla eklendi.", HttpStatus.CREATED); // 201 Created
    }

    // İsterseniz tüm kullanıcıları listelemek için (sadece admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}