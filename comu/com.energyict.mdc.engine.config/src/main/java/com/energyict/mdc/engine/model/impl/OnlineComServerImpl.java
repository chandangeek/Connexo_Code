package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.IPBasedInboundComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OnlineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:36)
 */
public class OnlineComServerImpl extends ComServerImpl implements OnlineComServer {

    private final EngineModelService engineModelService;
    @URL(message = "{MDC.InvalidURL}", groups = {Save.Update.class, Save.Create.class})
    private String queryAPIPostUri;
    @URL(message = "{MDC.InvalidURL}", groups = {Save.Update.class, Save.Create.class})
    private String eventRegistrationUri;
    @Range(min=MINIMUM_STORE_TASK_QUEUE_SIZE, max=MAXIMUM_STORE_TASK_QUEUE_SIZE, message = "{MDC.ValueNotInRange}", groups = {Save.Update.class, Save.Create.class})
    private int storeTaskQueueSize;
    @Range(min=MINIMUM_NUMBER_OF_STORE_TASK_THREADS, max=MAXIMUM_NUMBER_OF_STORE_TASK_THREADS, message = "{MDC.ValueNotInRange}", groups = {Save.Update.class, Save.Create.class})
    private int numberOfStoreTaskThreads;
    @Range(min=MINIMUM_STORE_TASK_THREAD_PRIORITY, max=MAXIMUM_STORE_TASK_THREAD_PRIORITY, message = "{MDC.ValueNotInRange}", groups = {Save.Update.class, Save.Create.class})
    private int storeTaskThreadPriority;
    private boolean usesDefaultQueryAPIPostUri=true;
    private boolean usesDefaultEventRegistrationUri=true;

    @Inject
    public OnlineComServerImpl(DataModel dataModel, EngineModelService engineModelService, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider) {
        super(dataModel, engineModelService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider);
        this.engineModelService = engineModelService;
    }

    @Override
    public String getType () {
        return OnlineComServer.class.getName();
    }

    protected void validate() {
        super.validate();
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

    @Override
    protected void validateDelete() {
        super.validateDelete();
        this.validateNotUsedByRemoteComServers();
    }

    protected void validateMakeObsolete() {
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