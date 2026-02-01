package ru.practicum.mainservice.compilation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.Compilation;
import ru.practicum.compilation.dto.CompilationCreateDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationRequestParams;
import ru.practicum.event.Event;
import ru.practicum.exception.CompilationNotFoundException;
import ru.practicum.mainservice.feign.EventRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EntityManager entityManager;


    @Override
    @Transactional
    public CompilationDto create(CompilationCreateDto compilationDto) {
        log.info("Main-service. Create compilations {}", compilationDto);

        Compilation compilation = compilationMapper.toEntity(compilationDto);
        if (!compilationDto.getEvents().isEmpty()) {
            compilation.setEvents(getEventsByIds(compilationDto.getEvents()));
        }
        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Main-service. Create compilation success: id = {}", savedCompilation.getId());
        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    public CompilationDto getById(Long compilationId) throws CompilationNotFoundException {
        log.info("Main-service. Get compilation by id = {}", compilationId);

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new CompilationNotFoundException(String.format("Не найдена подборка с id=%d", compilationId)));

        log.info("Main-service. Get compilation by id success {}", compilation);
        return compilationMapper.toDto(compilation);
    }

    @Override
    @Transactional
    public void delete(Long compilationId) throws CompilationNotFoundException {
        log.info("Main-service. Delete compilation with id = {}", compilationId);

        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new CompilationNotFoundException(String.format("Не найдена подборка с id=%d", compilationId)));

        compilationRepository.delete(compilation);

        log.info("Main-service. Delete compilation success id = {}", compilationId);

    }

    @Override
    public Collection<CompilationDto> findAll(CompilationRequestParams params) {
        log.info("Main-service. findAll compilation: {}", params);

        Pageable pageable = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                Sort.by("id").ascending()
        );
        Page<Compilation> page;
        if (params.getPinned() != null) {
            page = compilationRepository.findAllByPinned(params.getPinned(), pageable);
        } else {
            page = compilationRepository.findAll(pageable);
        }
        log.info("Main-service. findAll success: found {} compilations", page.getNumberOfElements());
        return compilationMapper.toDtoList(page.getContent());
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, CompilationCreateDto compilationCreateDto) throws CompilationNotFoundException {
        log.info("Main-service. Update compilations id={} with {}", compilationId, compilationCreateDto);
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new CompilationNotFoundException(String.format("Не найдена подборка с id=%d", compilationId)));
        if (compilationCreateDto.getPinned() != null) {
            compilation.setPinned(compilationCreateDto.getPinned());
        }
        if (compilationCreateDto.getEvents() != null) {
            compilation.setEvents(getEventsByIds(compilationCreateDto.getEvents()));
        }
        if (compilationCreateDto.getTitle() != null) {
            compilation.setTitle(compilationCreateDto.getTitle());
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Main-service. Update compilations id={} success: {}", compilationId, compilation);
        return compilationMapper.toDto(savedCompilation);
    }

    private Set<Event> getEventsByIds(Set<Long> ids) {
        List<Event> eventList = eventRepository.findAllById(ids);
        if (eventList.size() != ids.size()) {
            throw new IllegalArgumentException("Переданы несуществующие события");
        }
        return ids.stream()
                .map(id -> entityManager.getReference(Event.class, id))
                .collect(Collectors.toSet());
    }
}