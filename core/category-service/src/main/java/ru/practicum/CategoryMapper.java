package ru.practicum;

import org.mapstruct.Mapper;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryDto;
import ru.practicum.config.CommonMapperConfiguration;

@Mapper(config = CommonMapperConfiguration.class)
public interface CategoryMapper {

    //@Mapping(target = "id", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    CategoryDto toDto(Category entity);
}
