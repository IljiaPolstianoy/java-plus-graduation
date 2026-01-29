package ru.practicum.category.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryService;

import java.util.Optional;

@Controller
@RequestMapping("/internal/category")
public class InternalCategoryController {

    private final CategoryService categoryService;

    public InternalCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{categoryId}")
    public Optional<Category> findById(@PathVariable @Positive final Long categoryId) {
        return categoryService.findById(categoryId);
    }
}
