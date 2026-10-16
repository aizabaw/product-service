package com.practice.product_service.web;

import com.practice.product_service.config.SecurityConfig;
import com.practice.product_service.model.Product;
import com.practice.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductCatalogController.class)
@Import(SecurityConfig.class)
class ProductCatalogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "USER")
    void getProductById_returnsProduct() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(Optional.of(new Product(1L, "mug", new BigDecimal(500))));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("mug"))
                .andExpect(jsonPath("$.price").value(new BigDecimal(500)));
    }

    @Test
    void getProductById_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProductById_returns404WhenMissing() throws Exception {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product 99 not found"));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_returns201WithLocation() throws Exception {
        when(productService.createProductAndSave("spoon", new BigDecimal(20)))
                .thenReturn(new Product(4L, "spoon", new BigDecimal(20)));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "spoon","price": 20}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/products/4"));
    }

    @Test
    void createProduct_returns201WhenAdmin() throws Exception {
        when(productService.createProductAndSave("table", new BigDecimal(1000)))
                .thenReturn(new Product(1L, "table", new BigDecimal(1000)));

        when(jwtDecoder.decode("admin-token")).thenReturn(jwtWithRoles("ADMIN"));

        mockMvc.perform(post("/products")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "table", "price": 1000}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/products/1"));
    }

    @Test
    void createProduct_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "spoon","price": 20}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_returns403WhenNotAdmin() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "spoon","price": 20}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Access denied"));

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_returns403WhenNotAdminJwt() throws Exception {

        when(jwtDecoder.decode("user-token")).thenReturn(jwtWithRoles("USER"));

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "chair", "price": 500}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Access denied"));

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_returns400WhenInvalid() throws Exception {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "","price": -10}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(productService);

    }

    @Test
    void createProduct_returns401WhenBadJwt() throws Exception {

        when(jwtDecoder.decode("admin-token")).thenThrow(new BadJwtException("Error encountered"));

        mockMvc.perform(post("/products")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "fan", "price": 80}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));

        verify(jwtDecoder).decode("admin-token");
        verifyNoInteractions(productService);

    }

    private Jwt jwtWithRoles(String ...roles) {
        return Jwt.withTokenValue("t").header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .build();
    }

}
