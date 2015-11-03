package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * This class wraps the default UriInfo. It will by default overwrite the standard values for scheme, host and port obtained from the URL
 * and replace them with values provided through Connexo parameters.
 *
 * This functionality is intended to counter confusion due to URL rewrites by Apache webserver.
 * Created by bvn on 10/28/15.
 */
public class UriInfo implements javax.ws.rs.core.UriInfo {

    private final javax.ws.rs.core.UriInfo uriInfo;
    private final Optional<String> host;
    private final Optional<Integer> port;
    private final Optional<String> scheme;

    @Inject
    public UriInfo(@Context javax.ws.rs.core.UriInfo uriInfo, @Named("host") Optional host, @Named("port") Optional port, @Named("scheme") Optional scheme) {
        this.uriInfo = uriInfo;
        this.host = host;
        this.port = port;
        this.scheme = scheme;
    }

    @Override
    public String getPath() {
        return uriInfo.getPath();
    }

    @Override
    public String getPath(boolean b) {
        return uriInfo.getPath(b);
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return uriInfo.getPathSegments();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean b) {
        return uriInfo.getPathSegments(b);
    }

    @Override
    public URI getRequestUri() {
        return uriInfo.getRequestUri();
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return uriInfo.getRequestUriBuilder();
    }

    @Override
    public URI getAbsolutePath() {
        return uriInfo.getAbsolutePath();
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return uriInfo.getAbsolutePathBuilder();
    }

    @Override
    public URI getBaseUri() {
        return uriInfo.getBaseUri();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        host.ifPresent(baseUriBuilder::host);
        scheme.ifPresent(baseUriBuilder::scheme);
        port.ifPresent(baseUriBuilder::port);
        return baseUriBuilder;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return uriInfo.getPathParameters();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean b) {
        return uriInfo.getPathParameters(b);
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return uriInfo.getQueryParameters();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean b) {
        return uriInfo.getQueryParameters(b);
    }

    @Override
    public List<String> getMatchedURIs() {
        return uriInfo.getMatchedURIs();
    }

    @Override
    public List<String> getMatchedURIs(boolean b) {
        return uriInfo.getMatchedURIs(b);
    }

    @Override
    public List<Object> getMatchedResources() {
        return uriInfo.getMatchedResources();
    }

    @Override
    public URI resolve(URI uri) {
        return uriInfo.resolve(uri);
    }

    @Override
    public URI relativize(URI uri) {
        return uriInfo.relativize(uri);
    }
}
