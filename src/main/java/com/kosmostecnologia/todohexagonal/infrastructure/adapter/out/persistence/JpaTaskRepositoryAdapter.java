package com.kosmostecnologia.todohexagonal.infrastructure.adapter.out.persistence;

import com.kosmostecnologia.todohexagonal.application.port.out.TaskRepositoryPort;
import com.kosmostecnologia.todohexagonal.domain.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaTaskRepositoryAdapter implements TaskRepositoryPort {

    private final SpingDataTaskRepository spingDataTaskRepository;
    private final TaskPersistenceMapper mapper;

    @Override
    public Task save(Task task) {
        task.initDefaults();
        TaskJpaEntity entity = mapper.toEntity(task);
        TaskJpaEntity savedEntity = spingDataTaskRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
