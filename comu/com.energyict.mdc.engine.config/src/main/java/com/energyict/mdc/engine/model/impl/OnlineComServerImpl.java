package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.IPBasedInboundComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.google.common.collect.Range;

import com.google.inject.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OnlineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:36)
 */
public class OnlineComServerImpl extends ComServerImpl implements ServerOnlineComServer {

    private final EngineModelService engineModelService;
    private String queryAPIPostUri;
    private String eventRegistrationUri;
    private boolean usesDefaultQueryAPIPostUri=true;
    private boolean usesDefaultEventRegistrationUri=true;
    private int storeTaskQueueSize;
    private int numberOfStoreTaskThreads;
    private int storeTaskThreadPriority;

    @Inject
    public OnlineComServerImpl(DataModel dataModel, EngineModelService engineModelService, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServerServletBasedInboundComPort> servletBasedInboundComPortProvider) {
        super(dataModel, engineModelService, outboundComPortProvider, servletBasedInboundComPortProvider);
        this.engineModelService = engineModelService;
    }

    @Override
    public String getType () {
        return OnlineComServer.class.getName();
    }

    protected void validate() {
        super.validate();
        this.validateQueryAPIPostUri();
        this.validateEventRegistrationUri();
        this.validateValueInRange(this.getStoreTaskQueueSize(), MINIMUM_STORE_TASK_QUEUE_SIZE, MAXIMUM_STORE_TASK_QUEUE_SIZE, "onlineComServer.storeTaskQueueSize");
        this.validateValueInRange(this.getNumberOfStoreTaskThreads(), MINIMUM_NUMBER_OF_STORE_TASK_THREADS, MAXIMUM_NUMBER_OF_STORE_TASK_THREADS, "onlineComServer.numberOfStoreTaskThreads");
        this.validateValueInRange(this.getStoreTaskThreadPriority(), MINIMUM_STORE_TASK_THREAD_PRIORITY, MAXIMUM_STORE_TASK_THREAD_PRIORITY, "onlineComServer.storeTaskThreadPriority");
        this.validateInboundComPortDuplicateComPorts();
    }

    private void validateInboundComPortDuplicateComPorts() {
        List<Integer> portNumbers = new ArrayList<>();
        for (InboundComPort inboundComPort : this.getInboundComPorts()) {
            if(IPBasedInboundComPort.class.isAssignableFrom(inboundComPort.getClass())){
                int portNumber = ((IPBasedInboundComPort) inboundComPort).getPortNumber();
                if(portNumbers.contains(portNumber)){
                    throw new TranslatableApplicationException("duplicatecomportpercomserver", "'{0}' should be unique per comserver (duplicate: {1})", "comport.portnumber", portNumber);
                } else {
                    portNumbers.add(portNumber);
                }
            }
        }
    }

    private void validateQueryAPIPostUri() {
        if (!Checks.is(this.queryAPIPostUri).emptyOrOnlyWhiteSpace()) {
            this.validateUri(this.queryAPIPostUri, "queryAPIPostURI");
        }
    }

    private void validateEventRegistrationUri() {
        String uri = this.eventRegistrationUri;
        if (!Checks.is(uri).emptyOrOnlyWhiteSpace()) {
            this.validateUri(uri, "eventRegistrationURI");
        }
    }

    private void validateValueInRange(int value, int lowerBoundary, int upperBoundary, String propertyName) {
        if (!Range.closed(lowerBoundary, upperBoundary).contains(value)) {
            throw new TranslatableApplicationException(
                    "valueXforPropertyshouldBeInRangeFromYandZ",
                    "The value {0} for property {1} should be in the range from {2} to {3}",
                    propertyName, value, lowerBoundary, upperBoundary);
        }
    }

    private void validateUri(String uri, String propertyName)  {
        try {
            new URI(uri);
        }
        catch (URISyntaxException e) {
            throw new TranslatableApplicationException(
                    "XisNotAValidURI",
                    "\"{0}\" is not a valid URI for property {1} of {2}",
                    new Object[] {uri, "onlineComServer." + propertyName, "onlineComServer"},
                    e);
        }
    }

    @Override
    protected void validateDelete() {
        super.validateDelete();
        this.validateNotUsedByRemoteComServers();
    }

    @Override
    protected void validateMakeObsolete() {
        super.validateMakeObsolete();
        this.validateNotUsedByRemoteComServers();
    }

    private void validateNotUsedByRemoteComServers() {
        List<RemoteComServer> remoteComServersWithOnlineComServer = engineModelService.findRemoteComServersWithOnlineComServer(this);
        if (!remoteComServersWithOnlineComServer.isEmpty()) {
            throw new TranslatableApplicationException("onlineComServerXStillReferenced", "Online Comserver {0} is still referenced by {1} remote comserver(s)", this.getName(), remoteComServersWithOnlineComServer.size());
        }
    }

    @Override
    public boolean isOnline () {
        return true;
    }

    @Override
    public String getQueryApiPostUri () {
        if (this.usesDefaultQueryAPIPostUri) {
            return this.defaultQueryApiPostUri();
        }
        return queryAPIPostUri;
    }

    @Override
    public boolean usesDefaultQueryApiPostUri () {
        return this.usesDefaultQueryAPIPostUri;
    }

    @Override
    public void setUsesDefaultQueryAPIPostUri(boolean usesDefaultQueryAPIPostUri) {
        this.usesDefaultQueryAPIPostUri = usesDefaultQueryAPIPostUri;
    }

    @Override
    public String getEventRegistrationUri () {
        if (this.usesDefaultEventRegistrationUri) {
            return this.defaultEventRegistrationUri();
        }
        return eventRegistrationUri;
    }

    @Override
    public boolean usesDefaultEventRegistrationUri () {
        return this.usesDefaultEventRegistrationUri;
    }

    @Override
    public void setUsesDefaultEventRegistrationUri(boolean usesDefaultEventRegistrationUri) {
        this.usesDefaultEventRegistrationUri = usesDefaultEventRegistrationUri;
    }

    @Override
    public String getEventRegistrationUriIfSupported () throws BusinessException {
        if (Checks.is(this.getEventRegistrationUri()).emptyOrOnlyWhiteSpace()) {
            return super.getEventRegistrationUriIfSupported();
        }
        else {
            return this.getEventRegistrationUri();
        }
    }

    @Override
    public String getQueryApiPostUriIfSupported () throws BusinessException {
        if (Checks.is(this.getQueryApiPostUri()).emptyOrOnlyWhiteSpace()) {
            return super.getQueryApiPostUriIfSupported();
        }
        else {
            return this.getQueryApiPostUri();
        }
    }

    @Override
    public int getStoreTaskQueueSize () {
        return storeTaskQueueSize;
    }

    @Override
    public int getNumberOfStoreTaskThreads () {
        return numberOfStoreTaskThreads;
    }

    @Override
    public int getStoreTaskThreadPriority () {
        return storeTaskThreadPriority;
    }

    @Override
    public void setQueryAPIPostUri(String queryAPIPostUri) {
        this.usesDefaultQueryAPIPostUri=Checks.is(queryAPIPostUri).emptyOrOnlyWhiteSpace();
        this.queryAPIPostUri = queryAPIPostUri;
    }

    @Override
    public void setEventRegistrationUri(String eventRegistrationUri) {
        this.usesDefaultEventRegistrationUri=Checks.is(eventRegistrationUri).emptyOrOnlyWhiteSpace();
        this.eventRegistrationUri = eventRegistrationUri;
    }

    @Override
    public void setStoreTaskQueueSize(int storeTaskQueueSize) {
        this.storeTaskQueueSize = storeTaskQueueSize;
    }

    @Override
    public void setNumberOfStoreTaskThreads(int numberOfStoreTaskThreads) {
        this.numberOfStoreTaskThreads = numberOfStoreTaskThreads;
    }

    @Override
    public void setStoreTaskThreadPriority(int storeTaskThreadPriority) {
        this.storeTaskThreadPriority = storeTaskThreadPriority;
    }
}