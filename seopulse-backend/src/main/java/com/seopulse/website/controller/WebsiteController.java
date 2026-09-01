package com.seopulse.website.controller;

import com.seopulse.website.dto.CreateWebsiteRequest;
import com.seopulse.website.dto.WebsiteResponse;
import com.seopulse.website.service.WebsiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/website")
@RequiredArgsConstructor
public class WebsiteController {
    private final WebsiteService websiteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WebsiteResponse createWebsite(
            @Valid @RequestBody CreateWebsiteRequest request
    ) {
        return websiteService.createWebsite(request);
    }

    @GetMapping("/{id}")
    public WebsiteResponse getWebsite(
            @PathVariable Long id
    ) {
        return websiteService.getWebsite(id);
    }
}
