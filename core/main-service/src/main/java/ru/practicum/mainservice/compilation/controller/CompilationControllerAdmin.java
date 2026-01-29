package ru.practicum.mainservice.compilation.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationCreateDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.exception.CompilationNotFoundException;
import ru.practicum.mainservice.compilation.CompilationService;
import ru.practicum.validation.ValidationGroups;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Validated
public class CompilationControllerAdmin {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Validated({ValidationGroups.Create.class}) CompilationCreateDto compilationDto) {
        return compilationService.create(compilationDto);
    }

    @DeleteMapping(path = "/{compilationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long compilationId) throws CompilationNotFoundException {
        compilationService.delete(compilationId);
    }

    @PatchMapping(path = "/{compilationId}")
    public CompilationDto update(@RequestBody @Validated({ValidationGroups.Update.class}) CompilationCreateDto compilationCreateDto,
                                 @PathVariable @Positive Long compilationId) throws CompilationNotFoundException {
        return compilationService.update(compilationId, compilationCreateDto);
    }
}
