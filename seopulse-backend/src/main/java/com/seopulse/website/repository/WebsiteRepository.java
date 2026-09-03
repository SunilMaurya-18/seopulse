package com.seopulse.website.repository;

import com.seopulse.website.entity.Website;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebsiteRepository extends JpaRepository<Website, Long> {


    Page<Website> findByProjectId(Long projectId, Pageable pageable);

    boolean existsByProjectId(Long projectId, String url);
}