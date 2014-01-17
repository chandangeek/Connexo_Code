package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.google.inject.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.RemoteComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:40)
 */
public class RemoteComServerImpl extends ComServerImpl implements ServerRemoteComServer {

    private final Reference<OnlineComServer> onlineComServer = ValueReference.absent();
    private String queryAPIUsername;
    private String queryAPIPassword;
    private String eventRegistrationUri;
    private boolean usesDefaultEventRegistrationUri=true;

    public static RemoteComServer from(DataModel dataModel) {
        return dataModel.getInstance(RemoteComServerImpl.class);
    }

    @Inject
    public RemoteComServerImpl(DataModel dataModel, EngineModelService engineModelService, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServerServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ServerModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<ServerTCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<ServerUDPBasedInboundComPort> udpBasedInboundComPortProvider) {
        super(dataModel, engineModelService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider);
    }

    protected void validate()  {
        super.validate();
        this.validateNotNull(this.onlineComServer.orNull(), "remoteComServer.onlineComServer");
        this.validateEventRegistrationUri();
    }

    private void validateEventRegistrationUri() {
        if (!Checks.is(this.eventRegistrationUri).emptyOrOnlyWhiteSpace()) {
            try {
                new URI(this.eventRegistrationUri);
            }
            catch (URISyntaxException e) {
                throw new TranslatableApplicationException(
                        "XisNotAValidURI",
                        "\"{0}\" is not a valid URI for property {1} of {2}",
                        new Object[] {this.eventRegistrationUri, "remoteComServer." + "eventRegistrationURI", "remoteComServer"},
                        e);
            }
        }
    }

    @Override
    public boolean isRemote () {
        return true;
    }

    @Override
    public OnlineComServer getOnlineComServer () {
        return this.onlineComServer.get();
    }

    @Override
    public void setOnlineComServer(OnlineComServer onlineComServer) {
        this.onlineComServer.set(onlineComServer);
    }

    @Override
    public void setQueryAPIUsername(String queryAPIUsername) {
        this.queryAPIUsername = queryAPIUsername;
    }

    @Override
    public void setQueryAPIPassword(String queryAPIPassword) {
        this.queryAPIPassword = queryAPIPassword;
    }

    @Override
    public void setEventRegistrationUri(String eventRegistrationUri) {
        this.usesDefaultEventRegistrationUri=Checks.is(eventRegistrationUri).emptyOrOnlyWhiteSpace();
        this.eventRegistrationUri = eventRegistrationUri;
    }

    @Override
    public String getQueryAPIUsername () {
        return queryAPIUsername;
    }

    @Override
    public String getQueryAPIPassword () {
        return queryAPIPassword;
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
    public String getType () {
        return RemoteComServer.class.getName();
    }

    @Override
    public String getEventRegistrationUriIfSupported() throws BusinessException {
        if (Checks.is(this.getEventRegistrationUri()).emptyOrOnlyWhiteSpace()) {
            return super.getEventRegistrationUriIfSupported();
        }
        else {
            return this.getEventRegistrationUri();
        }
    }

}