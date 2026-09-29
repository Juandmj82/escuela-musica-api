package com.juandidev.escuelademusica.security.config;

import com.juandidev.escuelademusica.security.jwt.AuthEntryPointJwt;
import com.juandidev.escuelademusica.security.jwt.JwtAuthenticationFilter;
import com.juandidev.escuelademusica.security.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.HttpMethod; // Importa HttpMethod para reglas más granulares si las necesitas


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilita CSRF para APIs REST
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Manejo de errores de autenticación
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin sesiones (JWT es stateless)
                .authorizeHttpRequests(auth -> auth
                        // 1. Permite acceso público a los endpoints de autenticación (login, registro si lo tienes)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. Permite acceso público a todos los archivos estáticos del frontend
                        // Esto incluye la raíz, la página de login, y todas tus páginas HTML principales,
                        // así como los directorios de CSS, JS, y cualquier otro recurso estático.
                        .requestMatchers(
                                "/",                            // La URL raíz (ej. http://localhost:8080/)
                                "/register.html",               //Formulario de registro
                                "/login.html",                  // Tu página de login
                                "/dashboard.html",              // Si usas una página dashboard para usuarios logueados
                                "/cursos.html",                 // La página de listado de cursos (principal para USER)
                                "/user-management.html",        // Páginas de gestión de administradores
                                "/profesores.html",
                                "/estudiantes.html",
                                "/css/**",                      // Todos los archivos CSS dentro de la carpeta 'css'
                                "/js/**",                       // Todos los archivos JavaScript dentro de la carpeta 'js'
                                "/images/**",                   // Si tienes una carpeta 'images' para recursos estáticos
                                "/favicon.ico"                  // El ícono de la pestaña del navegador
                        ).permitAll()

                        // 3. Reglas de autorización específicas para APIs basadas en roles
                        // Solo usuarios con rol ADMIN pueden acceder a la gestión de usuarios, estudiantes y profesores
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/estudiantes/**").hasRole("ADMIN")
                        .requestMatchers("/api/profesores/**").hasRole("ADMIN")

                        // NOTA sobre /api/cursos:
                        // Si un usuario normal (USER) puede *ver* los cursos (GET), pero solo ADMIN puede *modificarlos* (POST, PUT, DELETE),
                        // la configuración de seguridad para /api/cursos/ sería más granular aquí,
                        // antes de anyRequest().authenticated().
                        // Si no especificas HttpMethod.GET y solo pones .requestMatchers("/api/cursos/**").hasRole("ADMIN"),
                        // entonces solo los ADMIN podrán acceder a CUALQUIER operación de /api/cursos.
                        // EJEMPLO DE REGLAS GRANULARES (descomenta si las necesitas):
                        // .requestMatchers(HttpMethod.GET, "/api/cursos/**").authenticated() // Cualquier usuario autenticado puede hacer GET a cursos
                        // .requestMatchers(HttpMethod.POST, "/api/cursos/**").hasRole("ADMIN") // Solo ADMIN puede crear cursos
                        // .requestMatchers(HttpMethod.PUT, "/api/cursos/**").hasRole("ADMIN") // Solo ADMIN puede actualizar cursos
                        // .requestMatchers(HttpMethod.DELETE, "/api/cursos/**").hasRole("ADMIN") // Solo ADMIN puede eliminar cursos


                        // 4. Cualquier otra solicitud no cubierta por las reglas anteriores REQUIERE autenticación.
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configuración para redireccionar la URL raíz (ej. http://localhost:8080/) a login.html
    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            // Cuando alguien acceda a la URL raíz del servidor (http://localhost:8080/),
            // Spring redirigirá internamente (forward) al contenido de /login.html.
            // Esto significa que la URL en la barra de direcciones del navegador podría seguir siendo "/",
            // pero el contenido mostrado será el de login.html.
            registry.addViewController("/").setViewName("forward:/login.html");

            // Opcional: También puedes añadir una redirección explícita si alguien intenta acceder a /login
            // para asegurar que siempre se sirva login.html
            registry.addViewController("/login").setViewName("forward:/login.html");
        }
    }
}