package com.elster.partners.connexo.filters.generic;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by dragos on 11/19/2015.
 */
public abstract class ConnexoAbstractSSOFilter implements Filter {
    private FilterConfig filterConfig;
    private List<String> excludedUrls = new ArrayList<>();
    private List<String> unauthorizedUrls = new ArrayList<>();

    protected final String CONNEXO_CONFIG = System.getProperty("connexo.configuration");

    protected Properties properties = new Properties();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        String excludePatterns = this.filterConfig.getInitParameter("excludePatterns");
        if(excludePatterns != null) {
            String[] exclude = excludePatterns.split(";");
            for (String url : exclude) {
                excludedUrls.add(url.replace("*", ".*?").trim());
            }
        }

        String unauthorizedPatterns = this.filterConfig.getInitParameter("unauthorizedPatterns");
        if(unauthorizedPatterns != null) {
            String[] unauthorize = unauthorizedPatterns.split(";");
            for (String url : unauthorize) {
                unauthorizedUrls.add(url.replace("*", ".*?").trim());
            }
        }

        loadProperties();
    }

    @Override
    public void destroy() {

    }

    protected void redirectToLogin(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.sendRedirect(getConnexoExternalUrl() + "/apps/login/index.html?page=" + request.getRequestURL());
    }

    protected void redirectToLogout(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.sendRedirect(getConnexoExternalUrl() + "/apps/login/index.html?logout");
    }

    protected String getConnexoInternalUrl() {
        String url = properties.getProperty("com.elster.jupiter.url");
        return (url != null) ? url : "http://localhost:8080";
    }

    protected String getConnexoExternalUrl() {
        String url = properties.getProperty("com.elster.jupiter.externalurl");
        return (url != null) ? url : getConnexoInternalUrl();
    }

    protected boolean shouldExcludUrl(final HttpServletRequest request) {
        String requestUrl = request.getRequestURI().toString();
        for(String url : excludedUrls) {
            if(requestUrl.matches(request.getContextPath() + url)) {
                return true;
            }
        }

        return false;
    }

    protected boolean shouldUnauthorize(final HttpServletRequest request) {
        String requestUrl = request.getRequestURI().toString();
        for(String url : unauthorizedUrls) {
            if(requestUrl.matches(request.getContextPath() + url)) {
                return true;
            }
        }

        return false;
    }

    private void loadProperties() {
        if(CONNEXO_CONFIG != null){
            try {
                FileInputStream inputStream = new FileInputStream(CONNEXO_CONFIG);
                properties.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
