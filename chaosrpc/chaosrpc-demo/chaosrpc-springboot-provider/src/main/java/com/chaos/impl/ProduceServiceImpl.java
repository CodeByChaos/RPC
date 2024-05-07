package com.chaos.impl;

import com.chaos.annotation.ChaosApi;
import com.chaos.annotation.TryTimes;
import com.chaos.service.ProductService;

/**
 * @author WongYut
 */
@ChaosApi(group = "primary")
public class ProduceServiceImpl implements ProductService {
    @Override
    @TryTimes(tryTimes = 4, intervalTimes = 3000)
    public String findAllProducts() {
        return "查到了所有的商品";
    }
}
