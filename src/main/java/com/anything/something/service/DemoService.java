package com.anything.something.service;

import com.anything.something.domain.dao.ProductData;
import com.anything.something.domain.dto.Product;
import com.anything.something.domain.mapper.ProductMapper;
import com.anything.something.repository.DemoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class DemoService {

    private final DemoRepository repository;
    private final ProductMapper productMapper;
    public Flux<Product> getProducts() {
        Flux<ProductData> products = repository.findAll();
        System.out.println("_____________________________________________________");
        products.doOnEach(System.out::println);
        System.out.println("_____________________________________________________");
        return products.map(productMapper::map);
    }

    public void addProduct(Product product) {
        ProductData productData = productMapper.map(product);
        repository.save(productData);
    }
}
