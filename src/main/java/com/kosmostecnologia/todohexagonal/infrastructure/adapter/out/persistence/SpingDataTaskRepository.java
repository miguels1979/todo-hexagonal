package com.kosmostecnologia.todohexagonal.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpingDataTaskRepository extends JpaRepository<TaskJpaEntity, Long> {
}
