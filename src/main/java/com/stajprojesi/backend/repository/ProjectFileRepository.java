package com.stajprojesi.backend.repository;

import com.stajprojesi.backend.model.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
} 