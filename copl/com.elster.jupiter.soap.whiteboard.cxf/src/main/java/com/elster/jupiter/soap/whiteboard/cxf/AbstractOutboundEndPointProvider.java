/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.impl.AbstractEndPointInitializer;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageSeeds;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageUtils;
import com.elster.jupiter.transaction.TransactionService;
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
    private volatile TransactionService transactionService;

    private Map<Long, EP> endpoints = new ConcurrentHashMap<>();

    protected void doAddEndpoint(EP endpoint, Map<String, Object> properties) {
        endpoints.put(getEndpointConfigurationId(properties), endpoint);
    }

    protected void doRemoveEndpoint(EP endpoint) {
        endpoints.values().removeIf(ep -> endpoint == ep);
    }

    @Override
    public RequestSenderImpl  using(String methodName) {
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
                    webServicesService.failOccurrence(id, thesaurus.getSimpleFormat(MessageSeeds.NO_WEB_SERVICE_ENDPOINT).format(endPointConfiguration.getName()));
                    // TODO send event for issue here, in a different transaction
                }
                return endpoint;
            } else {
                long id = webServicesService.startOccurrence(endPointConfiguration, methodName, getApplicationName()).getId();
                webServicesService.failOccurrence(id, thesaurus.getSimpleFormat(MessageSeeds.INACTIVE_WEB_SERVICE_ENDPOINT).format(endPointConfiguration.getName()));
                // TODO send event for issue here, in a different transaction
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
        public Map<EndPointConfiguration, ?> send(Object request) {
            Method method = Arrays.stream(getService().getMethods())
                    .filter(meth -> meth.getName().equals(methodName))
                    .filter(meth -> meth.getParameterCount() == 1)
                    .filter(meth -> meth.getParameterTypes()[0].isAssignableFrom(request.getClass()))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException(
                            new NoSuchMethodException("Couldn't find corresponding public method " + methodName + " in class " + getService().getName())));
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
                            webServicesService.failOccurrence(id, cause instanceof Exception ? (Exception) cause : new Exception(cause));
                            if (cause instanceof WebServiceException) { // SOAP endpoint
                                WebServiceException wse = (WebServiceException) cause;
                                String message = wse.getLocalizedMessage();
                                cause = wse.getCause();
                                if (cause != null) {
                                    message = cause.getLocalizedMessage();
                                    if (cause instanceof HTTPException) {
                                        HTTPException httpe = (HTTPException) cause;
                                        httpe.getResponseCode();
                                        // TODO send event for issue here, in a different transaction
                                    } else if (cause instanceof SocketTimeoutException || cause instanceof ConnectException) {
                                        // TODO send event for issue here, in a different transaction
                                    }
                                }
                                if (message == null)
                                {
                                    message = "null";
                                }
                                epcAndEP.getKey().log(message, wse);
                            } else if (cause instanceof NotAuthorizedException) { // REST endpoint
                                // TODO send event for issue here, in a different transaction
                            } else if (cause instanceof MessageBodyProviderNotFoundException) { // REST endpoint
                                // TODO send event for issue here, in a different transaction
                            } else {
                                epcAndEP.getKey().log(e.getLocalizedMessage(), e);
                            }
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        }

        //@Override
        public void send(String message, EndPointConfiguration endPointConfiguration){
            Class<?> type;
            Method method = Arrays.stream(getService().getMethods())
                    .filter(meth -> meth.getName().equals(methodName))
                    .filter(meth -> meth.getParameterCount() == 1)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException(
                            new NoSuchMethodException("Couldn't find corresponding public method " + methodName + " in class " + getService().getName())));
            Class[] types = method.getParameterTypes();
            type = types[0];

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(type);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                StringReader sReader = new StringReader( message);
                StreamSource streamSource = new StreamSource(sReader);
                JAXBElement<?>  root = jaxbUnmarshaller.unmarshal(streamSource, type);
                Object msg = root.getValue();
                send(msg);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        private String getApplicationName() {
            return AbstractOutboundEndPointProvider.this instanceof ApplicationSpecific ?
                    ((ApplicationSpecific) AbstractOutboundEndPointProvider.this).getApplication() :
                    ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
        }
    }
}