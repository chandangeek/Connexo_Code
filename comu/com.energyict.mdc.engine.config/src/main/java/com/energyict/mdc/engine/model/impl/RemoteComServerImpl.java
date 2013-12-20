package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.RemoteComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:40)
 */
public class RemoteComServerImpl extends ComServerImpl implements ServerRemoteComServer {

    private Reference<OnlineComServer> onlineComServer;
    private String queryAPIUsername;
    private String queryAPIPassword;
    private String eventRegistrationUri;
    private boolean usesDefaultEventRegistrationUri;

    protected RemoteComServerImpl () {
        super();
    }

    protected void validate()  {
        super.validate();
        this.validateNotNull(this.onlineComServer, "remoteComServer.onlineComServer");
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
        this.onlineComServer = ValueReference.of(onlineComServer);
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
        return eventRegistrationUri;
    }

    @Override
    public boolean usesDefaultEventRegistrationUri () {
        return this.usesDefaultEventRegistrationUri;
    }

    @Override
    public void setUsesDefaultEventRegistrationUri(boolean usesDefaultEventRegistrationUri) {
        this.usesDefaultEventRegistrationUri = usesDefaultEventRegistrationUri;
        if (this.usesDefaultEventRegistrationUri) {
            this.eventRegistrationUri = this.defaultEventRegistrationUri();
        }
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