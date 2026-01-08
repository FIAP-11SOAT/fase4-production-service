package com.fiap.soat11.production.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class GrantedAuthoritiesExtractorTest {

    private GrantedAuthoritiesExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new GrantedAuthoritiesExtractor();
    }

    @Test
    void testExtractorInitialization() {
        // Assert
        assertNotNull(extractor);
        assertTrue(extractor instanceof GrantedAuthoritiesExtractor);
    }

    @Test
    void testConvertJwtWithValidUserType() {
        // Arrange
        Jwt jwt = createMockJwt("ADMIN");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        assertNotNull(token);
        assertTrue(token instanceof JwtAuthenticationToken);
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testConvertJwtWithMultipleUserTypes() {
        // Arrange
        Jwt jwt = createMockJwt("ADMIN USER");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        assertNotNull(token);
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testConvertJwtWithNullUserType() {
        // Arrange
        Jwt jwt = createMockJwtWithNullClaim();

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        assertNotNull(token);
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(0, authorities.size());
    }

    @Test
    void testConvertJwtWithEmptyUserType() {
        // Arrange
        Jwt jwt = createMockJwt("");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        assertNotNull(token);
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(0, authorities.size());
    }

    @Test
    void testConvertJwtWithBlankUserType() {
        // Arrange
        Jwt jwt = createMockJwt("   ");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        assertNotNull(token);
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(0, authorities.size());
    }

    @Test
    void testConvertJwtRoleNormalization() {
        // Arrange
        Jwt jwt = createMockJwt("admin user");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testConvertJwtWithExtraWhitespace() {
        // Arrange
        Jwt jwt = createMockJwt("  ADMIN   USER  ");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testConvertJwtWithThreeRoles() {
        // Arrange
        Jwt jwt = createMockJwt("ADMIN USER MANAGER");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(3, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }

    @Test
    void testConvertJwtReturnsJwtAuthenticationToken() {
        // Arrange
        Jwt jwt = createMockJwt("USER");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        assertInstanceOf(JwtAuthenticationToken.class, token);
        assertEquals(jwt, token.getPrincipal());
    }

    @Test
    void testConvertJwtWithLowercaseRoles() {
        // Arrange
        Jwt jwt = createMockJwt("admin user");

        // Act
        AbstractAuthenticationToken token = extractor.convert(jwt);

        // Assert
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    // Helper methods
    private Jwt createMockJwt(String userType) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("user_type")).thenReturn(userType);
        return jwt;
    }

    private Jwt createMockJwtWithNullClaim() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("user_type")).thenReturn(null);
        return jwt;
    }
}
