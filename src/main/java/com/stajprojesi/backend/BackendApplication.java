package com.stajprojesi.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.stajprojesi.backend.model.Role;
import com.stajprojesi.backend.repository.RoleRepository;
import com.stajprojesi.backend.model.User;
import com.stajprojesi.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;
import java.util.Optional; // Bu import'un olduğundan emin olun
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Rolleri oluşturma kısmı
            if (roleRepository.findByName("USER").isEmpty()) {
                Role userRole = new Role();
                userRole.setName("USER");
                roleRepository.save(userRole);
            }
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                roleRepository.save(adminRole);
            }

            // Admin kullanıcı ekle
            // Daha önceki PG-Admin çıktınızda 'admin@admin.com' e-postalı kullanıcının username'i 'rıza' idi.
            // Bu kısım, eğer 'rıza' diye bir kullanıcı yoksa oluşturur.
            if (userRepository.findByUsername("rıza").isEmpty()) {
                User admin = new User();
                admin.setUsername("rıza");
                admin.setPassword(passwordEncoder.encode("1234")); // "1234" şifresiyle hashleniyor
                admin.setEmail("admin@admin.com");
                Optional<Role> adminRole = roleRepository.findByName("ADMIN");
                adminRole.ifPresent(role -> admin.setRoles(Set.of(role)));
                userRepository.save(admin);
                System.out.println("DEBUG: 'rıza' kullanıcısı oluşturuldu (initRoles).");
            }

            // Normal kullanıcı ekle
            if (userRepository.findByUsername("ahmet").isEmpty()) {
                User user = new User();
                user.setUsername("ahmet");
                user.setPassword(passwordEncoder.encode("2345")); // "2345" şifresiyle hashleniyor
                user.setEmail("ahmet@kullanici.com");
                Optional<Role> userRole = roleRepository.findByName("USER");
                userRole.ifPresent(role -> user.setRoles(Set.of(role)));
                userRepository.save(user);
                System.out.println("DEBUG: 'ahmet' kullanıcısı oluşturuldu (initRoles).");
            }

            // Yeni admin kullanıcı ekle
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin2 = new User();
                admin2.setUsername("admin");
                admin2.setPassword(passwordEncoder.encode("1"));
                admin2.setEmail("admin2@admin.com");
                Optional<Role> adminRole2 = roleRepository.findByName("ADMIN");
                adminRole2.ifPresent(role -> admin2.setRoles(Set.of(role)));
                userRepository.save(admin2);
                System.out.println("DEBUG: 'admin' kullanıcısı oluşturuldu (initRoles)." );
            }

            // 'ahmet' ve 'rıza' kullanıcılarını sil
            userRepository.findByUsername("ahmet").ifPresent(userRepository::delete);
            userRepository.findByUsername("rıza").ifPresent(userRepository::delete);

            // ****** ŞİFRE EŞLEŞTİRME TESTİ BAŞLANGICI ******
            System.out.println("--- ŞİFRE EŞLEŞTİRME TESTİ BAŞLANGICI ---");

            // 'rıza' kullanıcısının şifresini test et
            Optional<User> rizaUserOptional = userRepository.findByUsername("rıza");
            if (rizaUserOptional.isPresent()) {
                User rizaUser = rizaUserOptional.get();
                String plainPasswordRiza = "1234"; // Giriş yapmaya çalıştığınız şifre
                String hashedPasswordRiza = rizaUser.getPassword(); // Veritabanındaki hashlenmiş şifre

                boolean matchesRiza = passwordEncoder.matches(plainPasswordRiza, hashedPasswordRiza);
                System.out.println("Şifre Testi (rıza / 1234): " + (matchesRiza ? "EŞLEŞİYOR" : "EŞLEŞMİYOR"));
                if (!matchesRiza) {
                    System.err.println("HATA: 'rıza' kullanıcısının şifresi EŞLEŞMİYOR!");
                    System.err.println("Düz metin: " + plainPasswordRiza);
                    System.err.println("Hash (DB'den): " + hashedPasswordRiza);
                    System.err.println("Yeni Hash (1234 için): " + passwordEncoder.encode(plainPasswordRiza));
                }
            } else {
                System.out.println("'rıza' kullanıcısı bulunamadı veritabanında, şifre testi atlandı.");
            }

            // 'ahmet' kullanıcısının şifresini test et
            Optional<User> ahmetUserOptional = userRepository.findByUsername("ahmet");
            if (ahmetUserOptional.isPresent()) {
                User ahmetUser = ahmetUserOptional.get();
                String plainPasswordAhmet = "2345"; // Giriş yapmaya çalıştığınız şifre
                String hashedPasswordAhmet = ahmetUser.getPassword(); // Veritabanındaki hashlenmiş şifre

                boolean matchesAhmet = passwordEncoder.matches(plainPasswordAhmet, hashedPasswordAhmet);
                System.out.println("Şifre Testi (ahmet / 2345): " + (matchesAhmet ? "EŞLEŞİYOR" : "EŞLEŞMİYOR"));
                if (!matchesAhmet) {
                    System.err.println("HATA: 'ahmet' kullanıcısının şifresi EŞLEŞMİYOR!");
                    System.err.println("Düz metin: " + plainPasswordAhmet);
                    System.err.println("Hash (DB'den): " + hashedPasswordAhmet);
                    System.err.println("Yeni Hash (2345 için): " + passwordEncoder.encode(plainPasswordAhmet));
                }
            } else {
                System.out.println("'ahmet' kullanıcısı bulunamadı veritabanında, şifre testi atlandı.");
            }

            System.out.println("--- ŞİFRE EŞLEŞTİRME TESTİ BİTİŞİ ---");
        };
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:uploads/");
            }
        };
    }
}