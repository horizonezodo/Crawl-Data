package com.example.newapp.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.newapp.jwt.AuthEntryPoint;
import com.example.newapp.jwt.AuthTokenFilter;
import com.example.newapp.service.UserDetailsServiceImpl;

@Configuration
@EnableMethodSecurity
public class WebConfig {
	
	@Autowired
	UserDetailsServiceImpl details;
	
	@Autowired
	AuthEntryPoint unauthorized;
	
	@Bean
	public AuthTokenFilter authenticationTokenFillter() {
		return new AuthTokenFilter();
		
	}
	
	@Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider authProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(details);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorized))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.antMatchers("/auth/**").permitAll()
                                .anyRequest().authenticated()
                );
        http.authenticationProvider(authProvider());
        http.addFilterBefore(authenticationTokenFillter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
