package com.cafe.serviceImpl;

import com.cafe.repository.BillRepository;
import com.cafe.repository.CategoryRepository;
import com.cafe.repository.ProductRepository;
import com.cafe.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    CategoryRepository categoryRepository ;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BillRepository billRepository;


    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        System.out.println("inside getCount");

        Map<String , Object> map = new HashMap<>();
        map.put("category" , categoryRepository.count());
        map.put("product" , productRepository.count());
        map.put("bill" , billRepository.count());
        return new ResponseEntity<>(map , HttpStatus.OK);
    }
}

