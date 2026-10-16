package com.practice.product_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

}
