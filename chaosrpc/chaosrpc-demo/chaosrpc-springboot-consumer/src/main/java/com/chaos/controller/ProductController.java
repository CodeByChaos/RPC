package com.chaos.controller;

import com.chaos.annotation.ChaosService;
import com.chaos.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WongYut
 */
@RestController
public class ProductController {

    @ChaosService
    private ProductService productService;

    @GetMapping("/product")
    public String findProduct(){
        return productService.findAllProducts();
    }

}
