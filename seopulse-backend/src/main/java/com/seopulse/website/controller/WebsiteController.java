package com.seopulse.website.controller;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.website.dto.CreateWebsiteRequest;
import com.seopulse.website.dto.WebsiteResponse;
import com.seopulse.website.service.WebsiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/website")
@RequiredArgsConstructor
public class WebsiteController {
    private final WebsiteService websiteService;

    @Operation(
            summary = "Create a Website",
            description = "Creates a new Website"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Website Created Successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid Request"
    )

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WebsiteResponse createWebsite(
            @Valid @RequestBody CreateWebsiteRequest request
    ) {
        return websiteService.createWebsite(request);
    }

    @Operation(
            summary = "Get Website By ID",
            description = "Return a Website using its url"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Website found"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Website not found"
    )

    @GetMapping("/{id}")
    public WebsiteResponse getWebsite(
            @PathVariable Long id
    ) {
        return websiteService.getWebsite(id);
    }

    @Operation(
            summary = "Get websites",
            description = "Returns a paginated list of website"
    )
    @GetMapping
    public PageResponse<WebsiteResponse> getWebsites(Pageable pageable) {
        return websiteService.getWebsites(pageable);

    }
}
