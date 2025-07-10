package com.stajprojesi.backend.controller;

import com.stajprojesi.backend.model.User;
import com.stajprojesi.backend.model.Role;
import com.stajprojesi.backend.repository.UserRepository;
import com.stajprojesi.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.stajprojesi.backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Bu import hala gerekli, çünkü catch bloğu olmasa bile orElseThrow kullanılıyor.

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User newUser) {
        String username = newUser.getUsername() != null ? newUser.getUsername().trim() : null;
        String email = newUser.getEmail() != null ? newUser.getEmail().trim() : null;
        String password = newUser.getPassword() != null ? newUser.getPassword().trim() : null;

        if (username == null || username.isEmpty() || password == null || password.isEmpty() || email == null || email.isEmpty()) {
            return new ResponseEntity<>("Kullanıcı adı, şifre ve e-posta boş bırakılamaz.", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return new ResponseEntity<>("Kullanıcı adı zaten mevcut.", HttpStatus.CONFLICT); // 409 Conflict
        }
        // Assuming your UserRepository has findByEmail method (if not, remove this block)
        // if (userRepository.findByEmail(email).isPresent()) {
        //     return new ResponseEntity<>("E-posta zaten kayıtlı.", HttpStatus.CONFLICT); // 409 Conflict
        // }

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

        return new ResponseEntity<>("Kayıt başarılı.", HttpStatus.CREATED); // 201 Created
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        try {
            String username = user.getUsername() != null ? user.getUsername().trim() : null;
            String password = user.getPassword() != null ? user.getPassword().trim() : null;

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return new ResponseEntity<>(Collections.singletonMap("error", "Kullanıcı adı ve şifre boş bırakılamaz"), HttpStatus.BAD_REQUEST);
            }

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // Kullanıcı başarıyla doğrulandıktan sonra rollerini çekelim
            User authenticatedUser = userRepository.findByUsername(username)
                                                        .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
            
            Set<String> roles = authenticatedUser.getRoles().stream()
                                                    .map(Role::getName) // Rol adlarını string olarak al
                                                    .collect(Collectors.toSet());

            String token = jwtUtil.generateToken(username, roles); // ROLLERİ DE GÖNDER

            // Token ve rolleri içeren bir yanıt haritası oluştur
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("roles", roles); // Rolleri yanıta ekle

            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK ile yanıt dön
        } catch (AuthenticationException e) {
            System.err.println("Authentication failed for user " + user.getUsername() + ": " + e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", "Kullanıcı adı veya şifre hatalı"), HttpStatus.UNAUTHORIZED); // 401 Unauthorized dön
        }
        // UsernameNotFoundException artık burada özel olarak yakalanmıyor, genel Exception'a düşecek
        catch (Exception e) { // Diğer beklenmeyen hatalar için
            System.err.println("Login sırasında beklenmeyen hata: " + e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", "Sunucu hatası."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}