package com.personalfinance.manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.manager.dto.LoginRequest;
import com.personalfinance.manager.dto.RegisterRequest;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "new.user@example.com",
                "strongPassword123",
                "New User",
                "+123456789"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(notNullValue()));
    }

    @Test
    void testRegisterDuplicateUsername() throws Exception {
        User existingUser = User.builder()
                .username("duplicate@example.com")
                .password(passwordEncoder.encode("password"))
                .fullName("Existing")
                .phoneNumber("123")
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(
                "duplicate@example.com",
                "password123",
                "New",
                "456"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Username is already taken"));
    }

    @Test
    void testLoginSuccessAndCookieGeneration() throws Exception {
        User user = User.builder()
                .username("login@example.com")
                .password(passwordEncoder.encode("correctPassword"))
                .fullName("Login User")
                .phoneNumber("123456")
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest("login@example.com", "correctPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(cookie().exists("SESSION_TOKEN"))
                .andExpect(cookie().httpOnly("SESSION_TOKEN", true))
                .andExpect(cookie().secure("SESSION_TOKEN", true));
    }

    @Test
    void testLoginBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void testAccessProtectedEndpointWithoutCookie() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessProtectedEndpointWithCookie() throws Exception {
        User user = User.builder()
                .username("auth@example.com")
                .password(passwordEncoder.encode("password"))
                .fullName("Auth User")
                .phoneNumber("123")
                .build();
        userRepository.save(user);

        // Perform login to get a cookie
        LoginRequest loginRequest = new LoginRequest("auth@example.com", "password");
        String cookieHeader = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andReturn().getResponse().getHeader("Set-Cookie");

        if (cookieHeader == null) {
            throw new AssertionError("Set-Cookie header was null");
        }

        String[] cookieParts = cookieHeader.split(";");
        if (cookieParts.length == 0) {
            throw new AssertionError("Set-Cookie header was empty");
        }

        String[] tokenParts = cookieParts[0].split("=");
        if (tokenParts.length < 2) {
            throw new AssertionError("SESSION_TOKEN value was missing");
        }

        String token = tokenParts[1];

        // Access protected endpoint with cookie. Should get 404 (not found) instead of 401 (unauthorized)
        mockMvc.perform(get("/api/transactions")
                        .cookie(new Cookie("SESSION_TOKEN", token)))
                .andExpect(status().isNotFound()); // Bypassed auth, hits dispatcher and finds no handler (404)
    }

    @Test
    void testLogoutClearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("SESSION_TOKEN"))
                .andExpect(cookie().maxAge("SESSION_TOKEN", 0));
    }

    @NonNull
    private String toJson(Object obj) throws Exception {
        String json = objectMapper.writeValueAsString(obj);
        return json != null ? json : "";
    }
}
