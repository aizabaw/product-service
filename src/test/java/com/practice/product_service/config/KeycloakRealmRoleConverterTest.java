package com.practice.product_service.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class KeycloakRealmRoleConverterTest {

    @Test
    void convert_prependsROLEToRoles() {
       Collection<GrantedAuthority> authorities =
               new KeycloakRealmRoleConverter().convert(Jwt.withTokenValue("token")
               .header("alg", "none")
               .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN")))
               .build());

       assertThat(authorities)
               .extracting(GrantedAuthority::getAuthority)
               .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @ParameterizedTest
    @MethodSource("testInvalidClaims")
    void convert_returnsNoAuthoritiesForMalformedClaims(String claimName, Object roles) {

        Collection<GrantedAuthority> authorities = new KeycloakRealmRoleConverter().convert(Jwt
                .withTokenValue("token")
                .header("alg", "none")
                .claim(claimName, roles)
                .build());
        assertThat(authorities).isEmpty();

    }

    private static Stream<Arguments> testInvalidClaims() {
        return Stream.of(
                Arguments.of("other_claims", Map.of()),
                Arguments.of("realm_access", Map.of()),
                Arguments.of("realm_access", Map.of("roles", List.of())),
                Arguments.of("realmaccess", Map.of("roles", "ADMIN"))
        );
    }

}
