package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.cbo.ValueOutOfRangeException;
import com.energyict.comserver.tools.Numbers;
import com.energyict.comserver.tools.Strings;
import com.energyict.cpo.ResultSetIterator;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.shadow.ports.IPBasedInboundComPortShadow;
import com.energyict.mdc.shadow.ports.InboundComPortShadow;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;

import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OnlineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:36)
 */
public class OnlineComServerImpl extends ComServerImpl<OnlineComServerShadow> implements ServerOnlineComServer {

    private String queryAPIPostUri;
    private boolean usesDefaultQueryAPIPostUri;
    private String eventRegistrationUri;
    private boolean usesDefaultEventRegistrationUri;
    private int storeTaskQueueSize;
    private int numberOfStoreTaskThreads;
    private int storeTaskThreadPriority;

    protected OnlineComServerImpl () {
        super();
    }

    protected OnlineComServerImpl (int id) {
        super(id);
    }

    protected OnlineComServerImpl (ResultSet resultSet, ResultSetIterator resultSetIterator) throws SQLException {
        super(resultSet, resultSetIterator);
    }

    @Override
    protected ComServerFactoryImpl.ComServerDiscriminator getDiscriminator () {
        return ComServerFactoryImpl.ComServerDiscriminator.ONLINE;
    }

    @Override
    public String getType () {
        return OnlineComServer.class.getName();
    }

    @Override
    public OnlineComServerShadow getShadow () {
        return new OnlineComServerShadow(this);
    }

    public void init (final OnlineComServerShadow shadow) throws SQLException, BusinessException {
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
        if (this.usesDefaultQueryAPIPostUri) {
            this.queryAPIPostUri = this.defaultQueryApiPostUri();
        }
    }

    private void validateNew (OnlineComServerShadow shadow) throws BusinessException {
        this.validate(shadow);
    }

    private void validateUpdate (OnlineComServerShadow shadow) throws BusinessException {
        this.validateUpdateAllowed();
        this.validate(shadow);
    }

    protected void validate (OnlineComServerShadow shadow) throws BusinessException {
        super.validate(shadow);
        this.validateQueryAPIPostUri(shadow);
        this.validateEventRegistrationUri(shadow);
        this.validateValueInRange(shadow.getStoreTaskQueueSize(), 1, OnlineComServerShadow.MAXIMUM_STORE_TASK_QUEUE_SIZE, "onlineComServer.storeTaskQueueSize");
        this.validateValueInRange(shadow.getNumberOfStoreTaskThreads(), 1, OnlineComServerShadow.MAXIMUM_NUMBER_OF_STORE_TASK_THREADS, "onlineComServer.numberOfStoreTaskThreads");
        this.validateValueInRange(shadow.getStoreTaskThreadPriority(), OnlineComServerShadow.MINIMUM_STORE_TASK_THREAD_PRIORITY, OnlineComServerShadow.MAXIMUM_STORE_TASK_THREAD_PRIORITY, "onlineComServer.storeTaskThreadPriority");
        this.validateInboundComPortDuplicateComPorts(shadow);
    }

    private void validateInboundComPortDuplicateComPorts(OnlineComServerShadow shadow) throws InvalidValueException {
        List<Integer> portNumbers = new ArrayList<>();
        for (InboundComPortShadow inboundComPortShadow : shadow.getInboundComPortShadows()) {
            if(IPBasedInboundComPortShadow.class.isAssignableFrom(inboundComPortShadow.getClass())){
                int portNumber = ((IPBasedInboundComPortShadow) inboundComPortShadow).getPortNumber();
                if(portNumbers.contains(portNumber)){
                    throw new InvalidValueException("duplicatecomportpercomserver", "'{0}' should be unique per comserver (duplicate: {1})", "comport.portnumber", portNumber);
                } else {
                    portNumbers.add(portNumber);
                }
            }
        }
    }

    private void validateQueryAPIPostUri (OnlineComServerShadow shadow) throws BusinessException {
        String uri = shadow.getQueryAPIPostUri();
        if (!Strings.isEmpty(uri)) {
            this.validateUri(uri, "queryAPIPostURI");
        }
    }

    private void validateEventRegistrationUri (OnlineComServerShadow shadow) throws BusinessException {
        String uri = shadow.getEventRegistrationUri();
        if (!Strings.isEmpty(uri)) {
            this.validateUri(uri, "eventRegistrationURI");
        }
    }

    private void validateValueInRange (int value, int lowerBoundary, int upperBoundary, String propertyName) throws ValueOutOfRangeException {
        if (!Numbers.between(value, lowerBoundary, upperBoundary)) {
            throw new ValueOutOfRangeException(
                    "valueXforPropertyshouldBeInRangeFromYandZ",
                    "The value {0} for property {1} should be in the range from {2} to {3}",
                    propertyName, value, lowerBoundary, upperBoundary);
        }
    }

    private void validateUri (String uri, String propertyName) throws BusinessException {
        try {
            new URI(uri);
        }
        catch (URISyntaxException e) {
            throw new BusinessException(
                    "XisNotAValidURI",
                    "\"{0}\" is not a valid URI for property {1} of {2}",
                    new Object[] {uri, "onlineComServer." + propertyName, "onlineComServer"},
                    e);
        }
    }

    private void copyNew (OnlineComServerShadow shadow) {
        this.copy(shadow);
    }

    private void copy (OnlineComServerShadow shadow) {
        super.copy(shadow);
        this.usesDefaultQueryAPIPostUri = Strings.isEmpty(shadow.getQueryAPIPostUri());
        if (!this.usesDefaultQueryApiPostUri()) {
            this.queryAPIPostUri = shadow.getQueryAPIPostUri();
        } else {
            this.queryAPIPostUri = null;
        }
        this.usesDefaultEventRegistrationUri = Strings.isEmpty(shadow.getEventRegistrationUri());
        if (!this.usesDefaultEventRegistrationUri) {
            this.eventRegistrationUri = shadow.getEventRegistrationUri();
        } else {
            this.eventRegistrationUri = null;
        }
        this.numberOfStoreTaskThreads = shadow.getNumberOfStoreTaskThreads();
        this.storeTaskThreadPriority = shadow.getStoreTaskThreadPriority();
        this.storeTaskQueueSize = shadow.getStoreTaskQueueSize();
    }

    @Override
    protected void validateDelete() throws SQLException, BusinessException {
        super.validateDelete();
        this.validateNotUsedByRemoteComServers();
    }

    @Override
    protected void validateMakeObsolete () throws BusinessException {
        super.validateMakeObsolete();
        this.validateNotUsedByRemoteComServers();
    }

    private void validateNotUsedByRemoteComServers () throws BusinessException {
        List<RemoteComServer> remoteComServersWithOnlineComServer = getComServerFactory().findRemoteComServersWithOnlineComServer(this);
        if (!remoteComServersWithOnlineComServer.isEmpty()) {
            throw new BusinessException("onlineComServerXStillReferenced", "Online Comserver {0} is still referenced by {1} remote comserver(s)", this.getName(), remoteComServersWithOnlineComServer.size());
        }
    }

    @Override
    protected int bindBody (PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
        preparedStatement.setString(parameterNumber++, this.eventRegistrationUri);
        preparedStatement.setInt(parameterNumber++, this.toBoolean(this.usesDefaultEventRegistrationUri));
        preparedStatement.setString(parameterNumber++, this.queryAPIPostUri);
        preparedStatement.setInt(parameterNumber++, this.toBoolean(this.usesDefaultQueryAPIPostUri));
        preparedStatement.setInt(parameterNumber++, this.storeTaskQueueSize);
        preparedStatement.setInt(parameterNumber++, this.storeTaskThreadPriority);
        preparedStatement.setInt(parameterNumber++, this.numberOfStoreTaskThreads);
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
        this.queryAPIPostUri = resultSet.nextString();
        this.usesDefaultQueryAPIPostUri = resultSet.nextBoolean();
        this.storeTaskQueueSize = resultSet.nextInt();
        this.storeTaskThreadPriority = resultSet.nextInt();
        this.numberOfStoreTaskThreads = resultSet.nextInt();
    }

    @Override
    public void update (final OnlineComServerShadow shadow) throws BusinessException, SQLException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doUpdate(shadow);
                return null;
            }
        });
    }

    protected void doUpdate(OnlineComServerShadow shadow) throws BusinessException, SQLException {
        String oldName = this.getName();
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        this.applyDefaultURIsIfEmpty();
        this.post();
        this.processInboundComPorts(shadow);
        this.processOutboundComPorts(shadow);
        this.updated();
    }

    private void copyUpdate (OnlineComServerShadow shadow) {
        this.copy(shadow);
    }

    @Override
    public boolean isOnline () {
        return true;
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
    public String getQueryApiPostUri () {
        return queryAPIPostUri;
    }

    @Override
    public boolean usesDefaultQueryApiPostUri () {
        return this.usesDefaultQueryAPIPostUri;
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

    @Override
    public String getQueryApiPostUriIfSupported () throws BusinessException {
        if (Strings.isEmpty(this.getQueryApiPostUri())) {
            return super.getQueryApiPostUriIfSupported();
        }
        else {
            return this.getQueryApiPostUri();
        }
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

}