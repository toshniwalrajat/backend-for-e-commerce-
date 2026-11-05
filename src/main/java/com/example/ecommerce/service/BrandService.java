package com.example.ecommerce.service;

import com.example.ecommerce.entity.Brand;
import com.example.ecommerce.repository.BrandRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public BrandService(
            BrandRepository brandRepository,
            ProductRepository productRepository
    ) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Brand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Brand not found with ID: " + id
                ));
    }

    public Brand createBrand(Brand brand) {
        if (brandRepository.existsByName(brand.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A brand named '" + brand.getName() + "' already exists"
            );
        }

        // Ensure this is treated as a new entity.
        brand.setId(null);
        return brandRepository.save(brand);
    }

    public Brand updateBrand(Long id, Brand updatedBrand) {
        Brand existingBrand = getBrandById(id);

        if (brandRepository.existsByNameAndIdNot(updatedBrand.getName(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A brand named '" + updatedBrand.getName() + "' already exists"
            );
        }

        existingBrand.setName(updatedBrand.getName());
        existingBrand.setDescription(updatedBrand.getDescription());

        if (updatedBrand.getActive() != null) {
            existingBrand.setActive(updatedBrand.getActive());
        }

        return brandRepository.save(existingBrand);
    }

    public void deleteBrand(Long id) {
        Brand brand = getBrandById(id);

        if (productRepository.existsByBrand_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a brand that is assigned to products"
            );
        }

        brandRepository.delete(brand);
    }
}