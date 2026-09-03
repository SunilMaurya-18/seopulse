package com.seopulse.website.service;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

@Component
public class UrlValidator {

    public URI validate(String rawUrl) {

        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Website URL is required"
            );
        }

        String value = rawUrl.trim();

        URI uri;

        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid website URL"
            );
        }

        validateScheme(uri);
        validateHost(uri);
        validateUserInfo(uri);

        String host = uri.getHost();

        validateResolvedAddresses(host);

        return uri;
    }

    private void validateScheme(URI uri) {

        String scheme = uri.getScheme();

        if (scheme == null ||
                (!scheme.equalsIgnoreCase("http")
                        && !scheme.equalsIgnoreCase("https"))) {

            throw new IllegalArgumentException(
                    "Only HTTP and HTTPS URLs are allowed"
            );
        }
    }

    private void validateHost(URI uri) {

        String host = uri.getHost();

        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(
                    "Website URL must contain a valid host"
            );
        }

        if (host.length() > 253) {
            throw new IllegalArgumentException(
                    "Website hostname is too long"
            );
        }
    }

    private void validateUserInfo(URI uri) {

        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException(
                    "URLs containing user information are not allowed"
            );
        }
    }

    private void validateResolvedAddresses(String host) {

        InetAddress[] addresses;

        try {
            addresses = InetAddress.getAllByName(
                    host
            );
        } catch (UnknownHostException ex) {

            throw new IllegalArgumentException(
                    "Website hostname could not be resolved"
            );
        }

        if (addresses.length == 0) {
            throw new IllegalArgumentException(
                    "Website hostname could not be resolved"
            );
        }

        for (InetAddress address : addresses) {

            if (isUnsafeAddress(address)) {
                throw new IllegalArgumentException(
                        "Website URL resolves to a restricted network address"
                );
            }
        }
    }

    private boolean isUnsafeAddress(
            InetAddress address
    ) {

        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }
}