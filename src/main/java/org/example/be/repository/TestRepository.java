package org.example.be.repository;

import org.example.be.model.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    TestEntity findByUsername(String username);
}
