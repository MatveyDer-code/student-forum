package security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = io.student.GatewayApplication.class,
        properties = {
                "jwt.secret=supersecretkeysupersecretkeysupersecretkey",
                "jwt.access-expiration=900000",
                "jwt.refresh-expiration=604800000"
        })
@AutoConfigureMockMvc
@Import(security.test.TestController.class)
class GatewaySecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String secret;

    private String generateValidToken() {
        return Jwts.builder()
                .setSubject("user123")
                .claim("authUserId", 42)
                .claim("roles", List.of("ROLE_USER"))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    private String generateExpiredToken() {
        return Jwts.builder()
                .setSubject("user123")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    @Test
    void requestWithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithValidToken_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateValidToken()))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithExpiredToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateExpiredToken()))
                .andExpect(status().isUnauthorized());
    }
}
