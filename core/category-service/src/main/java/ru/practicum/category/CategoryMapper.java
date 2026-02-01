package ru.practicum.category;

import org.mapstruct.Mapper;
import ru.practicum.config.CommonMapperConfiguration;

@Mapper(config = CommonMapperConfiguration.class)
public interface CategoryMapper {

    //@Mapping(target = "id", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    CategoryDto toDto(Category entity);
}
