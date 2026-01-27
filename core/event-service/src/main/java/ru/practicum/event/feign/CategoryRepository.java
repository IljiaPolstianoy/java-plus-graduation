package ru.practicum.event.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.category.Category;
import ru.practicum.feign.CategoryFeignClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryRepository {

    private final CategoryFeignClient categoryFeignClient;

    public Optional<Category> findById(final Long categoryId) {
        return categoryFeignClient.findById(categoryId);
    }
}
