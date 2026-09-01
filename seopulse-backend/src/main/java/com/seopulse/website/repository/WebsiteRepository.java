package com.seopulse.website.repository;

import com.seopulse.website.entity.Website;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebsiteRepository extends JpaRepository<WebsiteRepository, Long> {
    Optional<Website> findByUrl(String url);

    boolean existsByUrl(String url);
}
