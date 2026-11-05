package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + id
                ));
    }

    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product with SKU '" + product.getSku() + "' already exists"
            );
        }

        // The database generates the ID; the entity sets its timestamps.
        product.setId(null);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);

        if (productRepository.existsBySkuAndIdNot(updatedProduct.getSku(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product with SKU '" + updatedProduct.getSku() + "' already exists"
            );
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setActive(updatedProduct.getActive());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}