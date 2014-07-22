package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.google.inject.Provider;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import org.hibernate.validator.constraints.URL;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.RemoteComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:40)
 */
public class RemoteComServerImpl extends ComServerImpl implements RemoteComServer {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private final Reference<OnlineComServer> onlineComServer = ValueReference.absent();
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Update.class, Save.Create.class}, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    private String queryAPIUsername;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Update.class, Save.Create.class}, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    private String queryAPIPassword;
    private boolean usesDefaultEventRegistrationUri=true;
    @URL(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_INVALID_URL+"}")
    private String eventRegistrationUri;

    public static RemoteComServer from(DataModel dataModel) {
        return dataModel.getInstance(RemoteComServerImpl.class);
    }

    @Inject
    public RemoteComServerImpl(DataModel dataModel, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
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
    @XmlElement
    public String getQueryAPIUsername () {
        return queryAPIUsername;
    }

    @Override
    @XmlElement
    public String getQueryAPIPassword () {
        return queryAPIPassword;
    }

    @Override
    @XmlElement
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