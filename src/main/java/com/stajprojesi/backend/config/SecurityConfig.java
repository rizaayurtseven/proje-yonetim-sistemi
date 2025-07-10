package com.stajprojesi.backend.config;

import com.stajprojesi.backend.security.JwtAuthenticationFilter;
import com.stajprojesi.backend.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // Bu import'a artık doğrudan gerek yok
import org.springframework.security.authentication.AuthenticationProvider; // Yeni import
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Yeni import
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Metot seviyesi güvenlik için ekleyin (UserController için gerekli)


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Metot seviyesi güvenliği etkinleştirir (örn: @PreAuthorize)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF korumasını devre dışı bırakma
            .cors(withDefaults()) // CorsConfigurationSource bean'ini kullanır
            .authorizeHttpRequests(auth -> auth
                // OPTIONS isteklerine her zaman izin ver (CORS preflight için)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Kimlik doğrulama endpoint'lerine (login, register) izin ver
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll() // Sizin eklediğiniz bu satır, /api/auth altındaki yollara izin verir.
                .requestMatchers("/uploads/**").permitAll() // uploads klasörü herkese açık (Sizin eklediğiniz bu satır)
                // H2 konsoluna izin ver (eğer kullanılıyorsa)
                .requestMatchers("/h2-console/**").permitAll()
                // ADMIN rolü gerektiren endpoint'ler
                .requestMatchers("/admin/**").hasRole("ADMIN") // /admin/** yolları sadece ADMIN'e açık
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Yeni eklediğimiz UserController için
                // Diğer tüm istekler için kimlik doğrulaması gerektir
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Oturumsuz yönetim (JWT kullandığımız için)
            )
            // H2 konsolu gibi iframe kullanan sayfalar için X-Frame-Options'ı devre dışı bırak
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
            
        // JWT filtresini UsernamePasswordAuthenticationFilter'dan önce ekle
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // Frontend adresin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Tüm başlıklara izin ver
        configuration.setAllowCredentials(true); // Kimlik bilgilerinin (çerezler, yetkilendirme başlıkları) gönderilmesine izin ver
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Tüm path'lere uygulanır
        return source;
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider() { // AuthenticationProvider döndürmesi daha geneldir
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // AuthenticationConfiguration üzerinden AuthenticationManager'ı alırken,
        // Spring otomatik olarak DaoAuthenticationProvider bean'ini algılayıp kullanacaktır.
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}