package com.kosmostecnologia.todohexagonal.infrastructure.adapter.out.persistence;

import com.kosmostecnologia.todohexagonal.application.port.out.TaskRepositoryPort;
import com.kosmostecnologia.todohexagonal.domain.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaTaskRepositoryAdapter implements TaskRepositoryPort {

    private final SpringDataTaskRepository springDataTaskRepository;
    private final TaskPersistenceMapper mapper;

    @Override
    public Task save(Task task) {
        task.initDefaults();
        TaskJpaEntity entity = mapper.toEntity(task);
        TaskJpaEntity savedEntity = springDataTaskRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Task> findAll() {
        return springDataTaskRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Task> findById(Long id) {
        return springDataTaskRepository.findById(id).map(mapper::toDomain);
    }
}
