package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.CategoryDto;
import ru.practicum.CategoryService;
import ru.practicum.exception.CategoryIsRelatedToEventException;
import ru.practicum.exception.CategoryNameUniqueException;
import ru.practicum.exception.CategoryNotFoundException;
import ru.practicum.exception.InvalidCategoryException;
import ru.practicum.validation.ValidationGroups;

@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Validated
public class CategoryControllerAdmin {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Validated({ValidationGroups.Create.class, Default.class}) @RequestBody CategoryDto categoryDto) throws CategoryNameUniqueException, InvalidCategoryException {
        return categoryService.createCategory(categoryDto);
    }

    @PatchMapping(path = "/{catId}")
    public CategoryDto updateCategory(@PathVariable @Positive Long catId,
                                      @Validated(Default.class) @RequestBody CategoryDto categoryDto) throws CategoryNotFoundException, CategoryNameUniqueException, InvalidCategoryException {
        categoryDto.setId(catId);
        return categoryService.updateCategory(categoryDto);
    }

    @DeleteMapping(path = "/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive Long catId) throws CategoryIsRelatedToEventException {
        categoryService.deleteCategory(catId);
    }
}
