package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}