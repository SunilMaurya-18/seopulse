package com.seopulse.website.service;

import com.seopulse.common.exception.ResourceNotFoundException;
import com.seopulse.website.dto.CreateWebsiteRequest;
import com.seopulse.website.dto.WebsiteResponse;
import com.seopulse.website.entity.Website;
import com.seopulse.website.repository.WebsiteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebsiteService {
    private final WebsiteRepository websiteRepository;

    @Transactional
    public WebsiteResponse createWebsite(CreateWebsiteRequest request) {
        if (websiteRepository.existsByUrl(request.url())) {
            throw new IllegalArgumentException("url already exists");
        }
        Website website = new website.builder()
                .name(request.name())
                .url(request.url())
                .build();
        Website savedWebsite = websiteRepository.save(website);
        return new WebsiteResponse(
                savedWebsite.getId(),
                savedWebsite.getName(),
                savedWebsite.getUrl(),
                savedWebsite.getCreatedAt(),
                savedWebsite.getUpdatedAt()
        );

    }

    @Transactional(readOnly = true)
    public WebsiteResponse getWebsite(Long id) {
        Website website =websiteRepository.findById(id)
                .orElseThrow(() ->
                new ResourceNotFoundException(
                        "Website not found With id: " + id
                )
        );
        return new mapToResponse(website);
    }

    private WebsiteResponse mapToResponse(Website website) {
        return new WebsiteResponse(
                website.getId(),
                website.getName(),
                website.getUrl(),
                website.getCreatedAt(),
                website.getUpdatedAt()
        );

    }
}
