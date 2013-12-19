package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.comserver.tools.Strings;
import com.energyict.cpo.ResultSetIterator;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.shadow.servers.RemoteComServerShadow;

import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.RemoteComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:40)
 */
public class RemoteComServerImpl extends ComServerImpl<RemoteComServerShadow> implements ServerRemoteComServer {

    private int onlineComServerId;
    private OnlineComServer onlineComServer;
    private String queryAPIUsername;
    private String queryAPIPassword;
    private String eventRegistrationUri;
    private boolean usesDefaultEventRegistrationUri;

    protected RemoteComServerImpl () {
        super();
    }

    protected RemoteComServerImpl (int id) {
        super(id);
    }

    protected RemoteComServerImpl (ResultSet resultSet, ResultSetIterator resultSetIterator) throws SQLException {
        super(resultSet, resultSetIterator);
    }

    @Override
    protected ComServerFactoryImpl.ComServerDiscriminator getDiscriminator () {
        return ComServerFactoryImpl.ComServerDiscriminator.REMOTE;
    }

    @Override
    public String getType () {
        return RemoteComServer.class.getName();
    }

    @Override
    public RemoteComServerShadow getShadow () {
        return new RemoteComServerShadow(this);
    }

    public void init (final RemoteComServerShadow shadow) throws SQLException, BusinessException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doInit(shadow);
                return null;
            }
        }
        );
    }

    protected void doInit (RemoteComServerShadow shadow) throws SQLException, BusinessException {
        this.validateNew(shadow);
        this.copyNew(shadow);
        this.applyDefaultURIsIfEmpty();
        this.postNew();
        this.processInboundComPorts(shadow);
        this.processOutboundComPorts(shadow);
        this.created();
    }

    private void applyDefaultURIsIfEmpty () {
        if (this.usesDefaultEventRegistrationUri) {
            this.eventRegistrationUri = this.defaultEventRegistrationUri();
        }
    }

    private void validateNew (RemoteComServerShadow shadow) throws BusinessException {
        this.validate(shadow);
    }

    private void validateUpdate (RemoteComServerShadow shadow) throws BusinessException {
        this.validateUpdateAllowed();
        this.validate(shadow);
    }

    protected void validate (RemoteComServerShadow shadow) throws BusinessException {
        super.validate(shadow);
        int onlineComServerId = shadow.getOnlineComServerId();
        ComServer comServer = this.newFactoryInstance().find(onlineComServerId);
        this.validateNotNull(comServer, "remoteComServer.onlineComServer");
        this.validateEventRegistrationUri(shadow);
    }

    private void validateEventRegistrationUri (RemoteComServerShadow shadow) throws BusinessException {
        String uri = shadow.getEventRegistrationUri();
        if (!Strings.isEmpty(uri)) {
            this.validateUri(uri, "eventRegistrationURI");
        }
    }

    private void validateUri (String url, String propertyName) throws BusinessException {
        try {
            new URI(url);
        }
        catch (URISyntaxException e) {
            throw new BusinessException(
                    "XisNotAValidURI",
                    "\"{0}\" is not a valid URI for property {1} of {2}",
                    new Object[] {url, "remoteComServer." + propertyName, "remoteComServer"},
                    e);
        }
    }

    private void copyNew (RemoteComServerShadow shadow) {
        this.copy(shadow);
    }

    private void copy (RemoteComServerShadow shadow) {
        super.copy(shadow);
        this.onlineComServerId = shadow.getOnlineComServerId();
        this.onlineComServer = null;
        this.queryAPIUsername = shadow.getQueryAPIUsername();
        this.queryAPIPassword = shadow.getQueryAPIPassword();
        this.eventRegistrationUri = shadow.getEventRegistrationUri();
        this.usesDefaultEventRegistrationUri = Strings.isEmpty(shadow.getEventRegistrationUri());
    }

    @Override
    protected int bindBody (PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
        preparedStatement.setString(parameterNumber++, this.eventRegistrationUri);
        preparedStatement.setInt(parameterNumber++, this.toBoolean(this.usesDefaultEventRegistrationUri));
        preparedStatement.setString(parameterNumber++, this.queryAPIUsername);
        preparedStatement.setString(parameterNumber++, this.queryAPIPassword);
        preparedStatement.setInt(parameterNumber++, this.onlineComServerId);
        return parameterNumber;
    }

    private int toBoolean (boolean flag) {
        if (flag) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @Override
    protected void doLoad (ResultSetIterator resultSet) throws SQLException {
        super.doLoad(resultSet);
        this.eventRegistrationUri = resultSet.nextString();
        this.usesDefaultEventRegistrationUri = resultSet.nextBoolean();
        this.queryAPIUsername = resultSet.nextString();
        this.queryAPIPassword = resultSet.nextString();
        this.onlineComServerId = resultSet.nextInt();
    }

    @Override
    public void update (final RemoteComServerShadow shadow) throws BusinessException, SQLException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doUpdate(shadow);
                return null;
            }
        });
    }

    protected void doUpdate(RemoteComServerShadow shadow) throws BusinessException, SQLException {
        String oldName = this.getName();
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        if (!oldName.equals(this.getName())) {
            this.applyDefaultURIsIfEmpty();
        }
        this.post();
        this.processInboundComPorts(shadow);
        this.processOutboundComPorts(shadow);
        this.updated();
    }

    private void copyUpdate (RemoteComServerShadow shadow) {
        this.copy(shadow);
    }

    @Override
    public boolean isRemote () {
        return true;
    }

    @Override
    public OnlineComServer getOnlineComServer () {
        if (this.onlineComServer == null) {
            this.onlineComServer = (OnlineComServer) this.newFactoryInstance().find(this.onlineComServerId);
        }
        return this.onlineComServer;
    }

    @Override
    public List<InboundComPort> getInboundComPorts () {
        return super.getInboundComPorts();
    }

    @Override
    public List<OutboundComPort> getOutboundComPorts () {
        return super.getOutboundComPorts();
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
        return eventRegistrationUri;
    }

    @Override
    public boolean usesDefaultEventRegistrationUri () {
        return this.usesDefaultEventRegistrationUri;
    }

    @Override
    public String getEventRegistrationUriIfSupported () throws BusinessException {
        if (Strings.isEmpty(this.getEventRegistrationUri())) {
            return super.getEventRegistrationUriIfSupported();
        }
        else {
            return this.getEventRegistrationUri();
        }
    }

}