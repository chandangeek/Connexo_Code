/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.impl.AbstractEndPointInitializer;
import com.elster.jupiter.soap.whiteboard.cxf.impl.EndPointException;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageSeeds;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageUtils;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.SetMultimap;
import org.apache.cxf.transport.http.HTTPException;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.NotAuthorizedException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Basic abstract class for implementation of {@link OutboundSoapEndPointProvider}s and (in the future) {@link OutboundRestEndPointProvider}s.
 * Acts as a container of injected web service endpoints and provides unified interface {@link OutboundEndPointProvider} for simple sending of outbound requests.
 * Creation of related web service call occurrences and (if needed) web service issues is implemented inside.
 * <b>NB:</b> During the implementation please don't forget to introduce explicit dependency on {@link WebServicesService} in the subclass,
 * otherwise the provider may not register on whiteboard and thus may work incorrectly (e.g. some fields below won't be injected).
 *
 * @param <EP> The type of web service endpoint (port).
 */
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
    private volatile WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    private volatile EventService eventService;

    private final Map<Long, EP> endpoints = new ConcurrentHashMap<>();

    /**
     * Must be overridden or re-implemented as a reference addition method in any subclass to inject endpoints; addition should be delegated to this method.
     *
     * @param endpoint An endpoint injected with the help of multiple/dynamic {@link Reference}.
     * @param properties Properties of the injected endpoint.
     */
    protected void doAddEndpoint(EP endpoint, Map<String, Object> properties) {
        endpoints.put(getEndpointConfigurationId(properties), endpoint);
    }

    /**
     * Must be overridden or re-implemented as a reference removal method in any subclass to remove injected endpoints; removal should be delegated to this method.
     *
     * @param endpoint An endpoint to remove; previously injected with the help of multiple/dynamic {@link Reference}.
     */
    protected void doRemoveEndpoint(EP endpoint) {
        endpoints.values().removeIf(ep -> endpoint == ep);
    }

    @Override
    public RequestSender using(String methodName) {
        return new RequestSenderImpl(methodName);
    }

    /**
     * Checks if there're any endpoints registered in this provider (i.e. published and ready for communication).
     */
    protected boolean hasEndpoints() {
        return !endpoints.isEmpty();
    }

    protected List<EndPointConfiguration> getEndPointConfigurationsForWebService() {
        return endPointConfigurationService.getEndPointConfigurationsForWebService(getName());
    }

    /**
     * Returns the {@link Class} representing considered endpoints (ports).
     *
     * @return The {@link Class} representing considered endpoints (ports).
     */
    protected abstract Class<EP> getService();

    /**
     * Returns the web service name.
     *
     * @return The web service name.
     */
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

    private final class RequestSenderImpl implements RequestSender {
        private final String methodName;
        private String payload;
        private Collection<EndPointConfiguration> endPointConfigurations;
        private SetMultimap<String, String> values;


        private RequestSenderImpl(String methodName) {
            this.methodName = methodName;
        }

        public RequestSenderImpl withRelatedAttributes(SetMultimap<String, String> values) {
            this.values = values;
            return this;
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
            if (!getName().equals(endPointConfiguration.getWebServiceName())) {
                processUnavailableEndpoint(endPointConfiguration, MessageSeeds.WRONG_WEB_SERVICE_ENDPOINT_CONFIGURATION, endPointConfiguration.getName(), getName());
                return null;
            }
            if (!endPointConfiguration.isActive()) {
                processUnavailableEndpoint(endPointConfiguration, MessageSeeds.INACTIVE_WEB_SERVICE_ENDPOINT, endPointConfiguration.getName());
                return null;
            }
            // TODO use here publish that throws exceptions to distinguish authorization error & unavailability
            publish(endPointConfiguration);
            EP endpoint = endpoints.get(endPointConfiguration.getId());
            if (endpoint == null) {
                processUnavailableEndpoint(endPointConfiguration, MessageSeeds.NO_WEB_SERVICE_ENDPOINT, endPointConfiguration.getName());
            }
            return endpoint;
        }

        private void processUnavailableEndpoint(EndPointConfiguration endPointConfiguration, MessageSeed messageSeed, Object... args) {
            long id = webServiceCallOccurrenceService.startOccurrence(endPointConfiguration, methodName, getApplicationName(), payload).getId();
            String message = thesaurus.getSimpleFormat(messageSeed).format(args);
            WebServiceCallOccurrence occurrence = webServiceCallOccurrenceService.failOccurrence(id, message);
            eventService.postEvent(EventType.OUTBOUND_ENDPOINT_NOT_AVAILABLE.topic(), occurrence);
        }

        private Map<EndPointConfiguration, EP> getEndpoints() {
            if (endPointConfigurations == null) {
                endPointConfigurations = getEndPointConfigurationsForWebService().stream()
                        .filter(EndPointConfiguration::isActive)
                        .collect(Collectors.toSet());
                if (endPointConfigurations.isEmpty()) {
                    throw new EndPointException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS, getName());
                }
            }
            return endPointConfigurations.stream()
                    .map(epc -> Pair.of(epc, getEndpoint(epc)))
                    .filter(Pair::hasLast)
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        }

        @Override
        public Map<WebServiceCallOccurrence, ?> sendRawXml(String message) {
            payload = message;
            Method method = Arrays.stream(getService().getMethods())
                    .filter(meth -> meth.getName().equals(methodName))
                    .filter(meth -> meth.getParameterCount() == 1)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException(
                            new NoSuchMethodException("Couldn't find corresponding public method " + methodName + " in class " + getService().getName())));
            Class<?> type = method.getParameterTypes()[0];

            try {
                InputStream inputStream = new ByteArrayInputStream(message.getBytes());
                SOAPMessage msg = MessageFactory.newInstance().createMessage(null, inputStream);
                JAXBContext jaxbContext = JAXBContext.newInstance(type);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<?> root = jaxbUnmarshaller.unmarshal(msg.getSOAPBody().extractContentAsDocument(), type);
                return doSend(method, root.getValue());
            } catch (JAXBException | SOAPException | IOException e) {
                getEndpoints().keySet().forEach(endPointConfiguration -> {
                    long id = webServiceCallOccurrenceService.startOccurrence(endPointConfiguration, methodName, getApplicationName(), message).getId();
                    webServiceCallOccurrenceService.failOccurrence(id,
                            "The provided xml payload can't be sent by means of web service " + getName() + " using request " + methodName + '.',
                            e);
                });
                return Collections.emptyMap();
            }
        }

        @Override
        public Map<WebServiceCallOccurrence, ?> send(Object request) {
            Method method = Arrays.stream(getService().getMethods())
                    .filter(meth -> meth.getName().equals(methodName))
                    .filter(meth -> meth.getParameterCount() == 1)
                    .filter(meth -> meth.getParameterTypes()[0].isAssignableFrom(request.getClass()))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException(
                            new NoSuchMethodException("Couldn't find corresponding public method " + methodName + " in class " + getService().getName())));
            return doSend(method, request);
        }

        private Map<WebServiceCallOccurrence, ?> doSend(Method method, Object request) {
            return getEndpoints().entrySet().stream()
                    .map(epcAndEP -> {
                        Object port = epcAndEP.getValue();
                        long id = webServiceCallOccurrenceService.startOccurrence(epcAndEP.getKey(), methodName, getApplicationName()).getId();
                        try {
                            MessageUtils.setOccurrenceId((BindingProvider) port, id);
                            if (values != null) {
                                webServiceCallOccurrenceService.getOngoingOccurrence(id).saveRelatedAttributes(values);
                            }
                            Object response = method.invoke(port, request);
                            WebServiceCallOccurrence occurrence = webServiceCallOccurrenceService.passOccurrence(id);
                            return Pair.of(occurrence, response);
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getTargetException();
                            WebServiceCallOccurrence occurrence = webServiceCallOccurrenceService.failOccurrence(id, cause instanceof Exception ? (Exception) cause : new Exception(cause));
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
                    .collect(HashMap::new, (map, pair) -> map.put(pair.getFirst(), pair.getLast()), Map::putAll); // to avoid NPE in case response is null
        }

        private String getApplicationName() {
            return AbstractOutboundEndPointProvider.this instanceof ApplicationSpecific ?
                    ((ApplicationSpecific) AbstractOutboundEndPointProvider.this).getApplication() :
                    ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
        }
    }
}
