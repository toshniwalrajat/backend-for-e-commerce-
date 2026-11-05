package com.example.ecommerce.service;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found with ID: " + id
                ));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A category named '" + category.getName() + "' already exists"
            );
        }

        category.setId(null);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = getCategoryById(id);

        if (categoryRepository.existsByNameAndIdNot(
                updatedCategory.getName(),
                id
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A category named '" + updatedCategory.getName()
                            + "' already exists"
            );
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());

        if (updatedCategory.getActive() != null) {
            existingCategory.setActive(updatedCategory.getActive());
        }

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        if (productRepository.existsByCategory_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a category that is assigned to products"
            );
        }

        categoryRepository.delete(category);
    }
}