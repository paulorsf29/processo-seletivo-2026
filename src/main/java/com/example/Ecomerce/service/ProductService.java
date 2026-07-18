package com.example.Ecomerce.service;

import com.example.Ecomerce.dto.common.PageResponse;
import com.example.Ecomerce.dto.product.ProductRequest;
import com.example.Ecomerce.dto.product.ProductResponse;
import com.example.Ecomerce.exception.ResourceNotFoundException;
import com.example.Ecomerce.model.Product;
import com.example.Ecomerce.repository.ProductRepository;
import com.example.Ecomerce.repository.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "T(String).join('|', #search ?: '', #category ?: '', #minPrice?.toString() ?: '', #maxPrice?.toString() ?: '', #pageable.pageNumber.toString(), #pageable.pageSize.toString(), #pageable.sort.toString())")
    public PageResponse<ProductResponse> list(String search, String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        var spec = ProductSpecifications.filter(search, category, minPrice, maxPrice, true);
        return PageResponse.fromPage(productRepository.findAll(spec, pageable).map(ProductResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'byId:' + #id")
    public ProductResponse getById(Long id) {
        return ProductResponse.fromEntity(findEntity(id));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .team(request.team())
                .brand(request.brand())
                .category(request.category())
                .size(request.size())
                .stockQuantity(request.stockQuantity())
                .imageUrl(request.imageUrl())
                .active(request.active() == null || request.active())
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setTeam(request.team());
        product.setBrand(request.brand());
        product.setCategory(request.category());
        product.setSize(request.size());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());
        if (request.active() != null) {
            product.setActive(request.active());
        }

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        Product product = findEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }
}
