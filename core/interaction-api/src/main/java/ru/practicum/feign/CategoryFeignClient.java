package ru.practicum.feign;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.category.Category;

import java.util.Optional;

@FeignClient(
        name = "main-service",
        contextId = "categoryFeign",
        path = "/internal/category"
)
public interface CategoryFeignClient {

    @GetMapping("/{categoryId}")
    Optional<Category> findById(@PathVariable @Positive final Long categoryId);
}
