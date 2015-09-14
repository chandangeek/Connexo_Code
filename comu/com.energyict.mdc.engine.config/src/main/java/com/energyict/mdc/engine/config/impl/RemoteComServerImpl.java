package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.RemoteComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:40)
 */
public class RemoteComServerImpl extends ComServerImpl implements RemoteComServer {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}")
    private final Reference<OnlineComServer> onlineComServer = ValueReference.absent();
    private boolean usesDefaultEventRegistrationUri = true;
    @URI(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_INVALID_URL + "}")
    @Size(max = 512)
    private String eventRegistrationUri;
    @URI(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_INVALID_URL + "}")
    @Size(max = 512)
    private String statusUri;
    private boolean usesDefaultStatusUri = true;

    public static RemoteComServer from(DataModel dataModel) {
        return dataModel.getInstance(RemoteComServerImpl.class);
    }

    @Inject
    public RemoteComServerImpl(DataModel dataModel, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public OnlineComServer getOnlineComServer() {
        return this.onlineComServer.get();
    }

    @Override
    public void setOnlineComServer(OnlineComServer onlineComServer) {
        this.onlineComServer.set(onlineComServer);
    }

    @Override
    public void setEventRegistrationUri(String eventRegistrationUri) {
        this.usesDefaultEventRegistrationUri = Checks.is(eventRegistrationUri).emptyOrOnlyWhiteSpace();
        this.eventRegistrationUri = eventRegistrationUri;
    }

    @Override
    @XmlElement
    public String getEventRegistrationUri() {
        if (this.usesDefaultEventRegistrationUri) {
            return this.defaultEventRegistrationUri();
        }
        return eventRegistrationUri;
    }

    @Override
    public boolean usesDefaultEventRegistrationUri() {
        return this.usesDefaultEventRegistrationUri;
    }

    @Override
    public void setUsesDefaultEventRegistrationUri(boolean usesDefaultEventRegistrationUri) {
        this.usesDefaultEventRegistrationUri = usesDefaultEventRegistrationUri;
    }

    @Override
    public String getEventRegistrationUriIfSupported() throws BusinessException {
        if (Checks.is(this.getEventRegistrationUri()).emptyOrOnlyWhiteSpace()) {
            return super.getEventRegistrationUriIfSupported();
        } else {
            return this.getEventRegistrationUri();
        }
    }

    @Override
    public void setStatusUri(String statusUri) {
        this.usesDefaultStatusUri = Checks.is(statusUri).emptyOrOnlyWhiteSpace();
        this.statusUri = statusUri;
    }

    @Override
    @XmlElement
    public String getStatusUri() {
        if (this.usesDefaultStatusUri) {
            return this.defaultStatusUri();
        }
        return statusUri;
    }

    @Override
    public boolean usesDefaultStatusUri() {
        return this.usesDefaultStatusUri;
    }

    @Override
    public void setUsesDefaultStatusUri(boolean usesDefaultStatusUri) {
        this.usesDefaultStatusUri = usesDefaultStatusUri;
    }

}