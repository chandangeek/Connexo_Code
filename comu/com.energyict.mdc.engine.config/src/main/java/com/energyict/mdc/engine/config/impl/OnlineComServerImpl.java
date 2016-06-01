package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.IPBasedInboundComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.OnlineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:36)
 */
@UniqueUri(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER_URI + "}")
@ValidComServerUri(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}")
public final class OnlineComServerImpl extends ComServerImpl implements OnlineComServer {

    private final EngineConfigurationService engineConfigurationService;
    @URI(message = "{"+ MessageSeeds.Keys.MDC_INVALID_URL+"}", groups = {Save.Update.class, Save.Create.class})
    @Size(max = 512)
    private String queryAPIPostUri;
    @URI(message = "{"+ MessageSeeds.Keys.MDC_INVALID_URL+"}", groups = {Save.Update.class, Save.Create.class})
    @Size(max = 512)
    private String eventRegistrationUri;
    @URI(message = "{" + MessageSeeds.Keys.MDC_INVALID_URL + "}", groups = {Save.Update.class, Save.Create.class})
    @Size(max = 512)
    private String statusUri;
    @Range(min=MINIMUM_STORE_TASK_QUEUE_SIZE, max=MAXIMUM_STORE_TASK_QUEUE_SIZE, message = "{"+ MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE+"}", groups = {Save.Update.class, Save.Create.class})
    private int storeTaskQueueSize;
    @Range(min=MINIMUM_NUMBER_OF_STORE_TASK_THREADS, max=MAXIMUM_NUMBER_OF_STORE_TASK_THREADS, message = "{"+ MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE+"}", groups = {Save.Update.class, Save.Create.class})
    private int numberOfStoreTaskThreads;
    @Range(min=MINIMUM_STORE_TASK_THREAD_PRIORITY, max=MAXIMUM_STORE_TASK_THREAD_PRIORITY, message = "{"+ MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE+"}", groups = {Save.Update.class, Save.Create.class})
    private int storeTaskThreadPriority;

    @Inject
    public OnlineComServerImpl(DataModel dataModel, EngineConfigurationService engineConfigurationService, Provider<OutboundComPort> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        this.engineConfigurationService = engineConfigurationService;
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
                    throw new TranslatableApplicationException(thesaurus, MessageSeeds.DUPLICATE_COM_PORT_NUMBER);
                } else {
                    portNumbers.add(portNumber);
                }
            }
        }
    }

    @Override
    protected void validateMakeObsolete() {
        validateOnlineComServerNotReferenced();
        super.validateMakeObsolete();
    }

    private void validateOnlineComServerNotReferenced() {
        if (!engineConfigurationService.findRemoteComServersForOnlineComServer(this).isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.ONLINE_COMSERVER_STILL_REFERENCED);
        }
    }

    @Override
    protected void validateDelete() {
        validateOnlineComServerNotReferenced();
        super.validateDelete();
    }

    @Override
    public boolean isOnline () {
        return true;
    }

    @Override
    @XmlElement
    public String getQueryApiPostUri () {
        return queryAPIPostUri;
    }

    @Override
    public String getQueryApiPostUriIfSupported () {
        if (Checks.is(this.getQueryApiPostUri()).emptyOrOnlyWhiteSpace()) {
            return super.getQueryApiPostUriIfSupported();
        }
        else {
            return this.getQueryApiPostUri();
        }
    }

    @Override
    @XmlElement
    public String getEventRegistrationUri () {
        return eventRegistrationUri;
    }

    @Override
    public String getEventRegistrationUriIfSupported () {
        if (Checks.is(this.getEventRegistrationUri()).emptyOrOnlyWhiteSpace()) {
            return super.getEventRegistrationUriIfSupported();
        }
        else {
            return this.getEventRegistrationUri();
        }
    }

    @Override
    public void setStatusUri(String statusUri) {
        this.statusUri = statusUri;
    }

    @Override
    @XmlElement
    public String getStatusUri () {
        return statusUri;
    }

    @Override
    @XmlElement
    public int getStoreTaskQueueSize () {
        return storeTaskQueueSize;
    }

    @Override
    @XmlElement
    public int getNumberOfStoreTaskThreads () {
        return numberOfStoreTaskThreads;
    }

    @Override
    @XmlElement
    public int getStoreTaskThreadPriority () {
        return storeTaskThreadPriority;
    }

    @Override
    public void setQueryAPIPostUri(String queryAPIPostUri) {
        this.queryAPIPostUri = queryAPIPostUri;
    }

    @Override
    public void setEventRegistrationUri(String eventRegistrationUri) {
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

    public static class OnlineComServerBuilderImpl extends AbstractComServerBuilder<OnlineComServerImpl, OnlineComServerBuilder> implements OnlineComServerBuilder<OnlineComServerImpl> {

        @Inject
        public OnlineComServerBuilderImpl(DataModel dataModel) {
            super(dataModel.getInstance(OnlineComServerImpl.class), OnlineComServerBuilder.class);
        }

        @Override
        public OnlineComServerBuilderImpl numberOfStoreTaskThreads(int numberOfStoreTaskThreads) {
            getComServerInstance().setNumberOfStoreTaskThreads(numberOfStoreTaskThreads);
            return this;
        }

        @Override
        public OnlineComServerBuilderImpl storeTaskQueueSize(int storeTaskQueueSize) {
            getComServerInstance().setStoreTaskQueueSize(storeTaskQueueSize);
            return this;
        }

        @Override
        public OnlineComServerBuilderImpl storeTaskThreadPriority(int storeTaskThreadPriority) {
            getComServerInstance().setStoreTaskThreadPriority(storeTaskThreadPriority);
            return this;
        }

        @Override
        public OnlineComServerBuilderImpl eventRegistrationUri(String eventRegistrationUri) {
            getComServerInstance().setEventRegistrationUri(eventRegistrationUri);
            return this;
        }

        @Override
        public OnlineComServerBuilderImpl queryApiPostUri(String queryApiPostUri) {
            getComServerInstance().setQueryAPIPostUri(queryApiPostUri);
            return this;
        }

        @Override
        public OnlineComServerBuilderImpl statusUri(String statusUri) {
            getComServerInstance().setStatusUri(statusUri);
            return this;
        }
    }
}