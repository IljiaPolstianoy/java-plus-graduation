package ru.practicum.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.category.Category;

@Component
@RequiredArgsConstructor
public class CategoryRepository {

    private final CategoryFeignClient categoryFeignClient;

    public Category findById(final Long categoryId) {
        return categoryFeignClient.findById(categoryId);
    }
}
