package com.anything.something.controller;

import com.anything.something.domain.dao.ProductData;
import com.anything.something.domain.dto.Product;
import com.anything.something.service.DemoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class DemoController {

    private final DemoService demoService;
    @GetMapping
    public Flux<Product> getProducts() {
        return demoService.getProducts();
    }

    @PostMapping("/add")
    public void addProduct(@RequestBody Product product){
        demoService.addProduct(product);
    }
}
