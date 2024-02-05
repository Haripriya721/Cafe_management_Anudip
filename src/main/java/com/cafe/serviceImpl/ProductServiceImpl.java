package com.cafe.serviceImpl;

import com.cafe.JWT.CustomerUserDetailsService;
import com.cafe.JWT.JwtFilter;
import com.cafe.entities.Category;
import com.cafe.entities.Product;
import com.cafe.constants.CafeConstants;
import com.cafe.repository.ProductRepository;
import com.cafe.service.ProductService;
import com.cafe.utils.CafeUtil;
import com.cafe.utils.EmailUtil;
import com.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

	
	    @Autowired
	    ProductRepository productRepository;

	    @Autowired
	    AuthenticationManager authenticationManager;
	    @Autowired
	    com.cafe.JWT.JwtUtil jwtUtil;

	    @Autowired
	    JwtFilter jwtFilter;
	    
	    @Autowired
	    CustomerUserDetailsService customerUserDetailsService;

	    @Autowired
	    EmailUtil emailUtil;

	    @Override
	    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
	        log.info("Inside addNewProduct{}", requestMap);
	        try {
	            if (jwtFilter.isAdmin()) {
	                if (validateProductMap(requestMap, false)) {
	                    productRepository.save(getProductFromMap(requestMap, false));
	                    return CafeUtil.getResponeEntity("Product Added Successfully", HttpStatus.OK);
	                }
	            } else {
	                return CafeUtil.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

	    }

	    @Override
	    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
	        try {
	            return new ResponseEntity<>(productRepository.getAllProduct(), HttpStatus.OK);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }


	    @Override
	    public ResponseEntity<String> update(Map<String, String> requestMap) {
	        try {
	            if (jwtFilter.isAdmin()) {
	                if (validateProductMap(requestMap, true)) {
	                    Optional optional = productRepository.findById(Integer.parseInt(requestMap.get("id")));
	                    if (!optional.isEmpty()) {
	                    	productRepository.save(getProductFromMap(requestMap, true));
	                        return CafeUtil.getResponeEntity("Product is updated successfully", HttpStatus.OK);

	                    } else {
	                        return CafeUtil.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
	                    }

	                }
	                return CafeUtil.getResponeEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
	            } else {
	                return CafeUtil.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @Override
	    public ResponseEntity<String> delete(Integer id) {
	        try {
	            if (jwtFilter.isAdmin()) {
	                Optional optional = productRepository.findById(id);
	                if (!optional.isEmpty()) {
	                	productRepository.deleteById(id);
	                    //System.out.println("Product is deleted successfully");
	                    return CafeUtil.getResponeEntity("Product is deleted successfully", HttpStatus.OK);
	                }
	                //System.out.println("Product id doesn't exist");
	                return CafeUtil.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
	            } else {
	                return CafeUtil.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

	    }

	    @Override
	    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
	        try {
	            return new ResponseEntity<>(productRepository.getByCategory(id), HttpStatus.OK);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @Override
	    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
	        try {
	            return new ResponseEntity<>(productRepository.getProductById(id), HttpStatus.OK);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	    @Modifying
	    @Transactional
	    @Override
	    public ResponseEntity<String> updateProductStatus(Map<String, String> requestMap) {
	        try {
	            if (jwtFilter.isAdmin()) {
	                Optional optional = productRepository.findById(Integer.parseInt(requestMap.get("id")));
	                if (!optional.isEmpty()) {
	                	productRepository.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
	                    return CafeUtil.getResponeEntity("Product status is updated successfully", HttpStatus.OK);
	                }
	                return CafeUtil.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return CafeUtil.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	    }


	    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
	        if (requestMap.containsKey("name")) {
	            if (requestMap.containsKey("id") && validateId) {
	                return true;
	            } else if (!validateId) {
	                return true;
	            }
	        }
	        return false;
	    }

	    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
	        Product product = new Product();
	        Category category = new Category();
	        category.setId(Integer.parseInt(requestMap.get("categoryId")));

	        if (isAdd) {
	            product.setId(Integer.parseInt(requestMap.get("id")));
	        } else {
	            product.setstatus("true");
	        }
	        product.setCategory(category);
	        product.setName(requestMap.get("name"));
	        product.setDescription(requestMap.get("description"));
	        product.setPrice(Integer.parseInt(requestMap.get("price")));
	        product.setstatus(String.valueOf(isAdd));

	        return product;
	    }
	}

