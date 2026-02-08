package ru.practicum.compilation;

import ru.practicum.compilation.dto.CompilationCreateDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationRequestParams;
import ru.practicum.exception.CompilationNotFoundException;

import java.util.Collection;

public interface CompilationService {

    CompilationDto create(CompilationCreateDto compilationDto);

    CompilationDto getById(Long compilationId) throws CompilationNotFoundException;

    void delete(Long compilationId) throws CompilationNotFoundException;

    Collection<CompilationDto> findAll(CompilationRequestParams params);

    CompilationDto update(Long compilationId, CompilationCreateDto compilationCreateDto) throws CompilationNotFoundException;
}
