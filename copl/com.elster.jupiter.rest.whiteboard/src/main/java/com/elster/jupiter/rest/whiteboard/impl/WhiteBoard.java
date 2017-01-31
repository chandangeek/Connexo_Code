/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionMapper;
import com.elster.jupiter.rest.util.ConcurrentModificationInfo;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.FormValidationExceptionMapper;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.rest.util.OptimisticLockExceptionMapper;
import com.elster.jupiter.rest.util.RestExceptionMapper;
import com.elster.jupiter.rest.util.TransactionWrapper;
import com.elster.jupiter.rest.whiteboard.RestCallExecutedEvent;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import com.google.common.base.Strings;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component (name = "com.elster.jupiter.rest.whiteboard.implementation" , immediate = true , service = {}  )
public class WhiteBoard {

    private static final Logger LOGGER = Logger.getLogger(WhiteBoard.class.getName());

    private volatile HttpContextImpl httpContext;
    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile LicenseService licenseService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Publisher publisher;
    private volatile TransactionService transactionService;
    private AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();
    private volatile WhiteBoardConfiguration configuration;
    private volatile HttpAuthenticationService authenticationService;
    private volatile JsonService jsonService;

    private final UrlRewriteFilter urlRewriteFilter = new UrlRewriteFilter();

    UserService getUserService() {
        return userService;
    }


    ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setAuthenticationService(HttpAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    Publisher getPublisher() {
        return publisher;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setEventAdmin(EventAdmin eventAdmin) {
        eventAdminHolder.set(eventAdmin);
    }

    public void unsetEventAdmin(EventAdmin eventAdmin) {
        eventAdminHolder.compareAndSet(eventAdmin, null);
    }

    @Reference(name = "YServiceLocator")
    public void setConfiguration(WhiteBoardConfigurationProvider provider) {
        this.configuration = provider.getConfiguration();
        this.httpContext = new HttpContextImpl(authenticationService);
    }

	void fire(RestCallExecutedEvent event) {
		publisher.publish(event);
		if (configuration.log()) {
			Logger.getLogger("com.elster.jupiter.rest.whiteboard").info(event.toString());
		}
		if (configuration.throwEvents()) {
			EventAdmin eventAdmin = eventAdminHolder.get();
			if (eventAdmin != null) {
				eventAdmin.postEvent(event.toOsgiEvent());
			}
		}
	}

    @Reference(name="ZApplication",cardinality=ReferenceCardinality.MULTIPLE,policy=ReferencePolicy.DYNAMIC)
    public void addResource(Application application, Map<String,Object> properties) {
    	Optional<String> alias = getAlias(properties);
    	if (!alias.isPresent()) {
    		return;
    	}
        ResourceConfig secureConfig = ResourceConfig.forApplication(Objects.requireNonNull(application));
        secureConfig.register(ObjectMapperProvider.class);
        secureConfig.register(JacksonFeature.class);
        secureConfig.register(TextPlainMessageBodyWriter.class);
        secureConfig.register(RoleFilter.class);
        secureConfig.register(RolesAllowedDynamicFeature.class);
        secureConfig.register(LocalizedFieldValidationExceptionMapper.class);
        secureConfig.register(RestExceptionMapper.class);
        secureConfig.register(LocalizedExceptionMapper.class);
        secureConfig.register(ConstraintViolationExceptionMapper.class);
        secureConfig.register(FormValidationExceptionMapper.class);
        secureConfig.register(JsonMappingExceptionMapper.class);
        secureConfig.register(OptimisticLockExceptionMapper.class);
        secureConfig.register(TransactionWrapper.class);
        secureConfig.register(ConcurrentModificationExceptionMapper.class);
        secureConfig.register(urlRewriteFilter);
        secureConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ConcurrentModificationInfo.class).to(ConcurrentModificationInfo.class);
                bind(ConcurrentModificationExceptionFactory.class).to(ConcurrentModificationExceptionFactory.class);
                bind(jsonService).to(JsonService.class);
                bind(transactionService).to(TransactionService.class);
                bind(threadPrincipalService).to(ThreadPrincipalService.class);
            }
        });
        if (application instanceof BinderProvider) {
            secureConfig.register(((BinderProvider) application).getBinder());
        }
        if (configuration.debug()) {
            secureConfig.register(LoggingFilter.class);
        }
        EncodingFilter.enableFor(secureConfig, GZipEncoder.class);
        try (ContextClassLoaderResource ctx = ContextClassLoaderResource.of(application.getClass())) {
            ServletContainer container = new ServletContainer(secureConfig);
            HttpServlet wrapper = new EventServletWrapper(new ServletWrapper(container, threadPrincipalService), this);
            httpService.registerServlet(alias.get(), wrapper, null, httpContext);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while registering " + alias.get() + ": " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public void removeResource(Application application, Map<String, Object> properties) {
        Optional<String> alias = getAlias(properties);
        if (alias.isPresent()) {
            httpService.unregister(alias.get());
        }
    }

    private Optional<String> getAlias(Map<String, Object> properties) {
        String version = Optional.ofNullable(properties.get("version")).map(v -> "/" + v).orElse("");
        String published = Optional.ofNullable(properties.get("version")).map(p -> "/public").orElse("");
        return Optional.ofNullable(properties.get("alias")).map(alias -> published + "/api" + alias + version);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        if (bundleContext != null) {

            urlRewriteFilter.setHost(bundleContext.getProperty("com.elster.jupiter.url.rewrite.host"));
            String portString = bundleContext.getProperty("com.elster.jupiter.url.rewrite.port");
            if (!Strings.isNullOrEmpty(portString)) {
                try {
                    urlRewriteFilter.setPort(Integer.valueOf(portString));
                } catch (NumberFormatException e) {
                    LOGGER.warning("Failed to read property com.elster.jupiter.url.rewrite.port:" + e);
                }
            }
            urlRewriteFilter.setScheme(bundleContext.getProperty("com.elster.jupiter.url.rewrite.scheme"));
        }

    }

}
