package ru.practicum.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.practicum.validation.ValidationGroups;

@AllArgsConstructor
@Builder
@Getter
public class UserDto {

    @Null(groups = ValidationGroups.Create.class)
    private Long id;

    @Email
    @NotBlank
    @Size(min = 6, max = 254)
    private String email;

    @NotNull(groups = ValidationGroups.Create.class)
    @NotBlank
    @Size(min = 2, max = 250)
    private String name;

}
