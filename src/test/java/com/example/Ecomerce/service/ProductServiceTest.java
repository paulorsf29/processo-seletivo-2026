package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.product.ProductRequest;
import com.example.Ecomerce.dto.product.ProductResponse;
import com.example.Ecomerce.exception.ResourceNotFoundException;
import com.example.Ecomerce.model.Product;
import com.example.Ecomerce.model.Size;
import com.example.Ecomerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product buildProduct() {
        return Product.builder()
                .id(1L)
                .name("Camisa Brasil")
                .description("Camisa oficial")
                .price(new BigDecimal("299.90"))
                .team("Brasil")
                .brand("Nike")
                .category("Seleções")
                .size(Size.M)
                .stockQuantity(10)
                .imageUrl("http://img")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ProductRequest buildRequest(String name, String team, String brand, Size size, int stock, String imageUrl, Boolean active) {
        return new ProductRequest(name, "desc", new BigDecimal("299.90"), team, brand, "Seleções", size, stock, imageUrl, active);
    }

    @Test
    void create_defaultsActiveToTrueWhenNotInformed() {
        ProductRequest request = buildRequest("Camisa Brasil", "Brasil", "Nike", Size.M, 10, "http://img", null);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
        assertThat(response.name()).isEqualTo("Camisa Brasil");
        assertThat(response.price()).isEqualByComparingTo("299.90");
    }

    @Test
    void create_respectsExplicitActiveFalse() {
        ProductRequest request = buildRequest("Camisa Brasil", "Brasil", "Nike", Size.M, 10, "http://img", false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void getById_returnsProduct_whenFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(buildProduct()));

        ProductResponse response = productService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Camisa Brasil");
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_overwritesFields() {
        Product existing = buildProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = buildRequest("Camisa Argentina", "Argentina", "Adidas", Size.G, 5, "http://img2", true);

        ProductResponse response = productService.update(1L, request);

        assertThat(response.name()).isEqualTo("Camisa Argentina");
        assertThat(response.team()).isEqualTo("Argentina");
        assertThat(response.price()).isEqualByComparingTo("299.90");
        assertThat(response.stockQuantity()).isEqualTo(5);
    }

    @Test
    void delete_softDeletesByDeactivating() {
        Product existing = buildProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.delete(1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
        verify(productRepository, never()).delete(any(Product.class));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void list_delegatesToSpecificationQuery() {
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = new PageImpl<>(List.of(buildProduct()));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        var result = productService.list("brasil", "Seleções", null, null, pageable);

        assertThat(result.content()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }
}
