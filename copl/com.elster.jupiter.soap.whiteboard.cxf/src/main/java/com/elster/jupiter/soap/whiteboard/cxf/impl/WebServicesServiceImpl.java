/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProp;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.PayloadSaveStrategy;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.rest.InboundRestEndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.rest.OutboundRestEndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.soap.InboundSoapEndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.soap.OutboundSoapEndPointFactoryImpl;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.soap.whiteboard.cxf.impl.MessageSeeds.WEB_SERVICE_CALL_OCCURRENCE_IS_ALREADY_IN_STATE;
import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/29/16.
 */
public class WebServicesServiceImpl implements WebServicesService {
    private static final Logger LOGGER = Logger.getLogger("WebServicesServiceImpl");
    private static final long OCCURRENCES_EXPIRE_AFTER_MINUTES = 30;

    private final DataModel dataModel;
    private final EventService eventService;
    private final TransactionService transactionService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServiceCallOccurrenceService webServiceCallOccurrenceService;

    private Map<String, EndPointFactory> webServices = new ConcurrentHashMap<>();
    private final Map<EndPointConfiguration, ManagedEndpoint> endpoints = new ConcurrentHashMap<>();
    private Cache<Long, WebServiceCallOccurrence> occurrences;

    @Inject
    WebServicesServiceImpl(DataModel dataModel,
                           EventService eventService,
                           TransactionService transactionService,
                           Clock clock,
                           Thesaurus thesaurus,
                           EndPointConfigurationService endPointConfigurationService,
                           WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
        this.occurrences = CacheBuilder.newBuilder().expireAfterWrite(OCCURRENCES_EXPIRE_AFTER_MINUTES, TimeUnit.MINUTES).build();
    }

    @Override
    public Optional<WebService> getWebService(String webServiceName) {
        final EndPointFactory endPointFactory = webServices.get(webServiceName);
        if (endPointFactory != null) {
            return Optional.of(new WebService() {
                @Override
                public String getName() {
                    return webServiceName;
                }

                @Override
                public boolean isInbound() {
                    return endPointFactory.isInbound();
                }

                @Override
                public WebServiceProtocol getProtocol() {
                    return endPointFactory.getProtocol();
                }

                @Override
                public String getApplicationName() {
                    EndPointProvider provider = endPointFactory.getEndPointProvider();
                    if (provider instanceof InboundSoapEndPointProvider) {
                        if (((InboundSoapEndPointProvider) provider).get() instanceof ApplicationSpecific) {
                            ApplicationSpecific tmpProvider = (ApplicationSpecific) ((InboundSoapEndPointProvider) provider).get();
                            return tmpProvider.getApplication();
                        }
                    }

                    if (provider instanceof InboundRestEndPointProvider) {
                        if (((InboundRestEndPointProvider) provider).get() instanceof ApplicationSpecific) {
                            ApplicationSpecific tmpProvider = (ApplicationSpecific) ((InboundRestEndPointProvider) provider).get();
                            return tmpProvider.getApplication();
                        }
                    }

                    if (provider instanceof OutboundSoapEndPointProvider || provider instanceof OutboundRestEndPointProvider) {
                        if (provider instanceof ApplicationSpecific) {
                            return ((ApplicationSpecific) provider).getApplication();
                        }
                    }

                    return ApplicationSpecific.WebServiceApplicationName.UNDEFINED.getName();
                }
            });
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void removeAllEndPoints() {
        endpoints.forEach((endPointConfiguration, managedEndpoint) -> {
            String msg = "Stopping WebService " + endPointConfiguration.getWebServiceName() + " with config " + endPointConfiguration
                    .getName();
            LOGGER.info(msg);
            endPointConfiguration.log(LogLevel.FINE, msg);
            stopEndpoint(managedEndpoint);
            msg = "Endpoint was stopped";
            LOGGER.info(msg);
            endPointConfiguration.log(LogLevel.FINE, msg);
        });
        endpoints.clear();
    }

    @Override
    public List<WebService> getWebServices() {
        return webServices.entrySet().stream().map(e -> new WebService() {

            @Override
            public String getName() {
                return e.getKey();
            }

            @Override
            public boolean isInbound() {
                return e.getValue().isInbound();
            }

            @Override
            public WebServiceProtocol getProtocol() {
                return e.getValue().getProtocol();
            }

            @Override
            public String getApplicationName() {
                EndPointProvider provider = e.getValue().getEndPointProvider();
                if (provider instanceof InboundSoapEndPointProvider) {
                    if (((InboundSoapEndPointProvider) provider).get() instanceof ApplicationSpecific) {
                        ApplicationSpecific tmpProvider = (ApplicationSpecific) ((InboundSoapEndPointProvider) provider).get();
                        return tmpProvider.getApplication();
                    }
                }

                if (provider instanceof InboundRestEndPointProvider) {
                    if (((InboundRestEndPointProvider) provider).get() instanceof ApplicationSpecific) {
                        ApplicationSpecific tmpProvider = (ApplicationSpecific) ((InboundRestEndPointProvider) provider).get();
                        return tmpProvider.getApplication();
                    }
                }

                if (provider instanceof OutboundSoapEndPointProvider || provider instanceof OutboundRestEndPointProvider) {
                    if ((provider) instanceof ApplicationSpecific) {
                        return ((ApplicationSpecific) provider).getApplication();
                    }
                }

                return ApplicationSpecific.WebServiceApplicationName.UNDEFINED.getName();
            }
        }).collect(toList());
    }

    @Override
    public void publishEndPoint(EndPointConfiguration endPointConfiguration) {
        EndPointFactory endPointFactory = webServices.get(endPointConfiguration.getWebServiceName());
        if (endPointFactory != null) {
            try {
                ManagedEndpoint managedEndpoint = endPointFactory.createEndpoint(endPointConfiguration);
                managedEndpoint.publish();
                endpoints.put(endPointConfiguration, managedEndpoint);
                if (managedEndpoint.isPublished()) {
                    String msg = "Endpoint was published";
                    LOGGER.info(msg);
                    endPointConfiguration.log(LogLevel.FINE, msg);
                }
            } catch (Exception e) {
                String msg = "Failed to publish endpoint " + endPointConfiguration.getName();
                LOGGER.log(Level.SEVERE, msg, e);
                endPointConfiguration.log(msg, e);
            }
        } else {
            LOGGER.warning("Could not publish " + endPointConfiguration.getName() + ": the required web service '" + endPointConfiguration
                    .getWebServiceName() + "' is not registered");
        }
    }

    @Override
    public void removeEndPoint(EndPointConfiguration endPointConfiguration) {
        ManagedEndpoint endpoint = endpoints.remove(endPointConfiguration);
        if (endpoint != null) {
            endpoint.stop();
            String msg = "Endpoint was stopped";
            LOGGER.info(msg);
            if (endPointConfigurationService.getEndPointConfiguration(endPointConfiguration.getId()).isPresent()) {
                endPointConfiguration.log(LogLevel.FINE, msg);
            }
        }
    }

    @Override
    public List<EndPointConfiguration> getPublishedEndPoints() {
        return endpoints.entrySet()
                .stream()
                .filter(set -> set.getValue().isPublished())
                .map(Map.Entry::getKey)
                .collect(toList());
    }

    @Override
    public boolean isInbound(String webServiceName) {
        EndPointFactory endPointFactory = webServices.get(webServiceName);
        if (endPointFactory != null) {
            return endPointFactory.isInbound();
        } else {
            throw new IllegalArgumentException("No such web service");
        }
    }

    @Override
    public boolean isPublished(EndPointConfiguration endPointConfiguration) {
        ManagedEndpoint managedEndpoint = endpoints.get(endPointConfiguration);
        return managedEndpoint != null && managedEndpoint.isPublished();
    }

    // called by whiteboard
    public void register(String name, InboundSoapEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(InboundSoapEndPointFactoryImpl.class).init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void register(String name, InboundRestEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(InboundRestEndPointFactoryImpl.class).init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void register(String name, OutboundSoapEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(OutboundSoapEndPointFactoryImpl.class)
                .init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void register(String name, OutboundRestEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(OutboundRestEndPointFactoryImpl.class)
                .init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void unregister(String webServiceName) {
        if (webServices.remove(webServiceName) != null) {
            List<EndPointConfiguration> endPointConfigurations = endpoints.keySet()
                    .stream()
                    .filter(e -> e.getWebServiceName().equals(webServiceName))
                    .collect(toList());
            for (EndPointConfiguration endPointConfiguration : endPointConfigurations) {
                endpoints.remove(endPointConfiguration).stop();
            }
        }
    }

    @Override
    public List<PropertySpec> getWebServicePropertySpecs(String webServiceName) {
        final EndPointFactory endPointFactory = webServices.get(webServiceName);
        if (endPointFactory != null) {
            EndPointProvider provider = endPointFactory.getEndPointProvider();
            if (provider instanceof EndPointProp) {
                return ((EndPointProp) provider).getPropertySpecs();
            }
            Object endPoint = null;
            if (provider instanceof InboundSoapEndPointProvider) {
                endPoint = ((InboundSoapEndPointProvider) provider).get();
            } else if (provider instanceof OutboundSoapEndPointProvider) {
                endPoint = ((OutboundSoapEndPointProvider) provider).get();
            }
            if (endPoint instanceof EndPointProp) {
                return ((EndPointProp) endPoint).getPropertySpecs();
            }
        }
        return new ArrayList<>();
    }

    private void stopEndpoint(ManagedEndpoint endpoint) {
        try {
            endpoint.stop();
        } catch (Exception ex) {
            // exception suppressed
        }
    }

    // TODO: move to another service maybe

    @Override
    public WebServiceCallOccurrence startOccurrence(EndPointConfiguration endPointConfiguration, String requestName, String application) {
        WebServiceCallOccurrence tmp = transactionService.executeInIndependentTransaction(
                () -> endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), requestName, application));
        occurrences.put(tmp.getId(), tmp);
        return tmp;
    }

    @Override
    public WebServiceCallOccurrence startOccurrence(EndPointConfiguration endPointConfiguration, String requestName, String application, String payload) {
        WebServiceCallOccurrence tmp = transactionService.executeInIndependentTransaction(
                () -> endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), requestName, application, payload));
        occurrences.put(tmp.getId(), tmp);
        return tmp;
    }

    @Override
    public WebServiceCallOccurrence passOccurrence(long id) {
        WebServiceCallOccurrence tmp = popOngoingOccurrence(id);
        return transactionService.executeInIndependentTransaction(() -> {
            tmp.log(LogLevel.INFO, "Request completed successfully.");
            tmp.setEndTime(clock.instant());
            validateOngoingStatus(tmp);
            if (PayloadSaveStrategy.ALWAYS != tmp.getEndPointConfiguration().getPayloadSaveStrategy()) {
                tmp.setPayload(null);
            }
            tmp.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
            tmp.save();
            return tmp;
        });
    }

    @Override
    public WebServiceCallOccurrence failOccurrence(long id, String message) {
        return failOccurrence(id, message, null);
    }

    @Override
    public WebServiceCallOccurrence failOccurrence(long id, Exception exception) {
        return failOccurrence(id, exception.getLocalizedMessage(), exception);
    }

    @Override
    public WebServiceCallOccurrence failOccurrence(long id, String message, Exception exception) {
        WebServiceCallOccurrence tmp = popOngoingOccurrence(id);
        return transactionService.executeInIndependentTransaction(() -> {
            if (exception == null) {
                tmp.log(LogLevel.SEVERE, message);
            } else {
                tmp.log(message, exception);
            }
            tmp.setEndTime(clock.instant());
            validateOngoingStatus(tmp);
            tmp.setStatus(WebServiceCallOccurrenceStatus.FAILED);
            tmp.save();
            return tmp;
        });
    }

    @Override
    public WebServiceCallOccurrence cancelOccurrence(long id) {
        WebServiceCallOccurrence tmp = popOngoingOccurrence(id);
        tmp.log(LogLevel.INFO, "Request has been cancelled.");
        tmp.setEndTime(clock.instant());
        validateOngoingStatus(tmp);
        tmp.setStatus(WebServiceCallOccurrenceStatus.CANCELLED);
        tmp.save();
        return tmp;
    }

    private void validateOngoingStatus(WebServiceCallOccurrence occurrence) {
        WebServiceCallOccurrenceStatus status = occurrence.getStatus();
        if (status != WebServiceCallOccurrenceStatus.ONGOING) {
            throw new WebServiceCallOccurrenceException(thesaurus, WEB_SERVICE_CALL_OCCURRENCE_IS_ALREADY_IN_STATE, status.translate(thesaurus));
        }
    }

    private WebServiceCallOccurrence popOngoingOccurrence(long id) {
        occurrences.invalidate(id);
        return webServiceCallOccurrenceService.findAndLockWebServiceCallOccurrence(id)
                .orElseThrow(() -> new IllegalStateException("Web service call occurrence isn't present."));
    }

    @Override
    public WebServiceCallOccurrence getOngoingOccurrence(long id) {
        WebServiceCallOccurrence tmp = occurrences.getIfPresent(id);
        if (tmp == null) {
            tmp = webServiceCallOccurrenceService.getWebServiceCallOccurrence(id)
                    .orElseThrow(() -> new IllegalStateException("Web service call occurrence isn't present."));
        }
        return tmp;
    }

    @Override
    public Optional<EndPointProvider> getProvider(String webServiceName) {
        return Optional.ofNullable(webServices.get(webServiceName)).map(EndPointFactory::getEndPointProvider);
    }
}
