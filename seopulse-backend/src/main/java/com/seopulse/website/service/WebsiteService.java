package com.seopulse.website.service;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.common.exception.ResourceNotFoundException;
import com.seopulse.website.dto.CreateWebsiteRequest;
import com.seopulse.website.dto.WebsiteResponse;
import com.seopulse.website.entity.Website;
import com.seopulse.website.repository.WebsiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.core.annotation.MergedAnnotations.from;

@Service
@RequiredArgsConstructor
public class WebsiteService {

    private final WebsiteRepository websiteRepository;

    @Transactional
    public WebsiteResponse createWebsite(CreateWebsiteRequest request) {

        if (websiteRepository.existsByUrl(request.url())) {
            throw new IllegalArgumentException("URL already exists");
        }

        Website website = Website.builder()
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

        Website website = websiteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Website not found with id: " + id
                        )
                );

        return mapToResponse(website);
    }

    @Transactional(readOnly = true)
    public PageResponse<WebsiteResponse> getWebsites(Pageable pageable) {

        Page<WebsiteResponse> page = websiteRepository
                .findAll(pageable)
                .map(this::mapToResponse);

        return PageResponse.<WebsiteResponse>from(page);
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