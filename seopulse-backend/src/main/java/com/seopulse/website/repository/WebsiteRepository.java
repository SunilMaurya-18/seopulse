package com.seopulse.website.repository;

import com.seopulse.website.entity.Website;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebsiteRepository extends JpaRepository<Website, Long> {

    boolean existsByUrl(String url);
}