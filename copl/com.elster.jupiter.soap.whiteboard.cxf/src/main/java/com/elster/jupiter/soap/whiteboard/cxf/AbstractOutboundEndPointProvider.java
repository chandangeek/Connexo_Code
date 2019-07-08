/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.impl.AbstractEndPointInitializer;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageSeeds;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageUtils;
import com.elster.jupiter.util.Pair;

import aQute.bnd.annotation.ConsumerType;
import org.apache.cxf.transport.http.HTTPException;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;

import javax.ws.rs.NotAuthorizedException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ConsumerType
public abstract class AbstractOutboundEndPointProvider<EP> implements OutboundEndPointProvider {
    public static final String URL_PROPERTY = "url";
    public static final String ENDPOINT_CONFIGURATION_ID_PROPERTY = "epcId";
    private static final Logger LOGGER = Logger.getLogger(AbstractOutboundEndPointProvider.class.getName());

    /**
     * Attention: These fields are injectable by hardcoded names via {@link AbstractEndPointInitializer}.
     */
    private volatile Thesaurus thesaurus;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile WebServicesService webServicesService;
    private volatile EventService eventService;

    private Map<Long, EP> endpoints = new ConcurrentHashMap<>();

    protected void doAddEndpoint(EP endpoint, Map<String, Object> properties) {
        endpoints.put(getEndpointConfigurationId(properties), endpoint);
    }

    protected void doRemoveEndpoint(EP endpoint) {
        endpoints.values().removeIf(ep -> endpoint == ep);
    }

    @Override
    public RequestSender using(String methodName) {
        return new RequestSenderImpl(methodName);
    }

    protected boolean hasEndpoints() {
        return !endpoints.isEmpty();
    }

    protected abstract Class<EP> getService();
    protected abstract String getName();

    private void publish(EndPointConfiguration endPointConfiguration) {
        if (!webServicesService.isPublished(endPointConfiguration)) {
            endPointConfiguration.log(LogLevel.CONFIG, thesaurus.getSimpleFormat(MessageSeeds.PUBLISHING_WEB_SERVICE_ENDPOINT).format(endPointConfiguration.getName()));
            webServicesService.publishEndPoint(endPointConfiguration);
        }
    }

    private static Long getEndpointConfigurationId(Map<String, Object> properties) {
        return properties == null ? null : (Long) properties.get(ENDPOINT_CONFIGURATION_ID_PROPERTY);
    }

    protected final class RequestSenderImpl implements RequestSender {
        private final String methodName;
        private Collection<EndPointConfiguration> endPointConfigurations;

        private RequestSenderImpl(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public RequestSenderImpl toEndpoints(Collection<EndPointConfiguration> endPointConfigurations) {
            this.endPointConfigurations = endPointConfigurations;
            return this;
        }

        @Override
        public RequestSenderImpl toEndpoints(EndPointConfiguration... endPointConfigurations) {
            return toEndpoints(Arrays.asList(endPointConfigurations));
        }

        @Override
        public RequestSenderImpl toAllEndpoints() {
            this.endPointConfigurations = null;
            return this;
        }

        private EP getEndpoint(EndPointConfiguration endPointConfiguration) {
            // TODO use here publish that throws exceptions to distinguish authorization error & unavailability
            if (endPointConfiguration.isActive()) {
                publish(endPointConfiguration);
                EP endpoint = endpoints.get(endPointConfiguration.getId());
                if (endpoint == null) {
                    long id = webServicesService.startOccurrence(endPointConfiguration, methodName, getApplicationName()).getId();
                    String message = thesaurus.getSimpleFormat(MessageSeeds.NO_WEB_SERVICE_ENDPOINT).format(endPointConfiguration.getName());
                    WebServiceCallOccurrence occurrence = webServicesService.failOccurrence(id, message);
                    eventService.postEvent(EventType.OUTBOUND_ENDPOINT_NOT_AVAILABLE.topic(), occurrence);
                }
                return endpoint;
            } else {
                long id = webServicesService.startOccurrence(endPointConfiguration, methodName, getApplicationName()).getId();
                String message = thesaurus.getSimpleFormat(MessageSeeds.INACTIVE_WEB_SERVICE_ENDPOINT).format(endPointConfiguration.getName());
                WebServiceCallOccurrence occurrence = webServicesService.failOccurrence(id, message);
                eventService.postEvent(EventType.OUTBOUND_ENDPOINT_NOT_AVAILABLE.topic(), occurrence);
                return null;
            }
        }

        private Map<EndPointConfiguration, EP> getEndpoints() {
            if (endPointConfigurations == null) {
                endPointConfigurations = endPointConfigurationService.getEndPointConfigurationsForWebService(getName()).stream()
                        .filter(EndPointConfiguration::isActive)
                        .collect(Collectors.toSet());
                if (endPointConfigurations.isEmpty()) {
                    LOGGER.severe(thesaurus.getSimpleFormat(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS).format(getName()));
                }
            }
            return endPointConfigurations.stream()
                    .map(epc -> Pair.of(epc, getEndpoint(epc)))
                    .filter(Pair::hasLast)
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        }

        @Override
        public Map<EndPointConfiguration, ?> sendRawXml(String message) {
            Method method = Arrays.stream(getService().getMethods())
                    .filter(meth -> meth.getName().equals(methodName))
                    .filter(meth -> meth.getParameterCount() == 1)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException(
                            new NoSuchMethodException("Couldn't find corresponding public method " + methodName + " in class " + getService().getName())));
            Class<?> type = method.getParameterTypes()[0];

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(type);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                StringReader sReader = new StringReader(message);
                StreamSource streamSource = new StreamSource(sReader);
                JAXBElement<?> root = jaxbUnmarshaller.unmarshal(streamSource, type);
                return doSend(method, root.getValue());
            } catch (JAXBException e) {
                getEndpoints().keySet().forEach(endPointConfiguration -> {
                    long id = webServicesService.startOccurrence(endPointConfiguration, methodName, getApplicationName(), message).getId();
                    webServicesService.failOccurrence(id,
                            "The provided xml payload can't be sent by means of web service " + getName() + " using request " + methodName + '.',
                            e);
                });
                return Collections.emptyMap();
            }
        }

        @Override
        public Map<EndPointConfiguration, ?> send(Object request) {
            Method method = Arrays.stream(getService().getMethods())
                    .filter(meth -> meth.getName().equals(methodName))
                    .filter(meth -> meth.getParameterCount() == 1)
                    .filter(meth -> meth.getParameterTypes()[0].isAssignableFrom(request.getClass()))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException(
                            new NoSuchMethodException("Couldn't find corresponding public method " + methodName + " in class " + getService().getName())));
            return doSend(method, request);
        }

        private Map<EndPointConfiguration, ?> doSend(Method method, Object request) {
            return getEndpoints().entrySet().stream()
                    .map(epcAndEP -> {
                        Object port = epcAndEP.getValue();
                        long id = webServicesService.startOccurrence(epcAndEP.getKey(), methodName, getApplicationName()).getId();
                        try {
                            MessageUtils.setOccurrenceId((BindingProvider) port, id);
                            Object response = method.invoke(port, request);
                            webServicesService.passOccurrence(id);
                            return Pair.of(epcAndEP.getKey(), response);
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getTargetException();
                            WebServiceCallOccurrence occurrence = webServicesService.failOccurrence(id, cause instanceof Exception ? (Exception) cause : new Exception(cause));
                            if (cause instanceof WebServiceException) { // SOAP endpoint
                                cause = cause.getCause();
                                if (cause instanceof HTTPException) {
                                    HTTPException httpe = (HTTPException) cause;
                                    if (httpe.getResponseCode() == 401) {
                                        eventService.postEvent(EventType.OUTBOUND_AUTH_FAILURE.topic(), occurrence);
                                        return null;
                                    }
                                } else if (cause instanceof SocketTimeoutException || cause instanceof ConnectException) {
                                    eventService.postEvent(EventType.OUTBOUND_NO_ACKNOWLEDGEMENT.topic(), occurrence);
                                    return null;
                                }
                            } else if (cause instanceof NotAuthorizedException) { // REST endpoint
                                eventService.postEvent(EventType.OUTBOUND_AUTH_FAILURE.topic(), occurrence);
                                return null;
                            } else if (cause instanceof MessageBodyProviderNotFoundException) { // REST endpoint
                                eventService.postEvent(EventType.OUTBOUND_NO_ACKNOWLEDGEMENT.topic(), occurrence);
                                return null;
                            }
                            eventService.postEvent(EventType.OUTBOUND_BAD_ACKNOWLEDGEMENT.topic(), occurrence);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        }

        private String getApplicationName() {
            return AbstractOutboundEndPointProvider.this instanceof ApplicationSpecific ?
                    ((ApplicationSpecific) AbstractOutboundEndPointProvider.this).getApplication() :
                    ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
        }
    }
}
