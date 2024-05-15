package com.nighthawk.spring_portfolio.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
/*
* To enable HTTP Security in Spring
*/
@Configuration
public class SecurityConfig {

    @Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

    // Provide security configuration
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// disable csrf
			.csrf(csrf -> csrf
				.disable()
			)
			// list the requests/endpoints need to be authenticated
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.GET,"/login").permitAll()
				.requestMatchers(HttpMethod.POST,"/authenticate").permitAll()
				.requestMatchers(HttpMethod.POST,"/authenticateForm").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/person/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/person/**").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/person/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/person/**").hasAuthority("ROLE_ADMIN")
				.requestMatchers("/mvc/person/search/**").authenticated()
				.requestMatchers("/mvc/person/create/**").authenticated()
				.requestMatchers("/mvc/person/read/**").authenticated()
				.requestMatchers("/mvc/person/update/**").authenticated()
				.requestMatchers( "/mvc/person/delete/**").hasAuthority("ROLE_ADMIN")
				.requestMatchers("/**").permitAll()
			)
			// support cors
			.cors(Customizer.withDefaults())
			.headers(headers -> headers
				.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Credentials", "true"))
				.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-ExposedHeaders", "*", "Authorization"))
				.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Headers", "Content-Type", "Authorization", "x-csrf-token"))
				.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-MaxAge", "600"))
				.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Methods", "POST", "GET", "OPTIONS", "HEAD"))
				//.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Origin", "https://nighthawkcoders.github.io", "http://localhost:4100", "http://127.0.0.1:4100"))
			)
			// configure form login for server side authentication
			.formLogin(form -> form 
				.loginPage("/login")
				.defaultSuccessUrl("/mvc/person/read")
			)
			// configure logout for server side authentication
			.logout(logout -> logout
				.deleteCookies("sess_java_spring")
				.logoutSuccessUrl("/")
			)
			// make sure we use stateless session; 
			// session won't be used to store user's state.
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			)
			// Add a filter to validate the tokens with every request
			.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
