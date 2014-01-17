package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.inject.Provider;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.ComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (10:20)
 */
@XmlRootElement
public abstract class ComServerImpl implements ServerComServer {

    protected static final String ONLINE_COMSERVER_DISCRIMINATOR = "0";
    protected static final String OFFLINE_COMSERVER_DISCRIMINATOR = "1";
    protected static final String REMOTE_COMSERVER_DISCRIMINATOR = "2";

    static final Map<String, Class<? extends ComServer>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComServer>>of(
                    ONLINE_COMSERVER_DISCRIMINATOR, OnlineComServerImpl.class,
                    OFFLINE_COMSERVER_DISCRIMINATOR, OfflineComServerImpl.class,
                    REMOTE_COMSERVER_DISCRIMINATOR, RemoteComServerImpl.class);

    public static final int DEFAULT_EVENT_REGISTRATION_PORT_NUMBER = 8888;
    public static final int DEFAULT_QUERY_API_PORT_NUMBER = 8889;
    private final DataModel dataModel;
    private final EngineModelService engineModelService;

    private final Provider<OutboundComPortImpl> outboundComPortProvider;
    private final Provider<ServerServletBasedInboundComPort> servletBasedInboundComPortProvider;
    private final Provider<ServerModemBasedInboundComPort> modemBasedInboundComPortProvider;
    private final Provider<ServerTCPBasedInboundComPort> tcpBasedInboundComPortProvider;

    private long id;
    private String name;
    private boolean active;
    private LogLevel serverLogLevel;
    private LogLevel communicationLogLevel;
    private TimeDuration changesInterPollDelay;
    private TimeDuration schedulingInterPollDelay;
    private UtcInstant modificationDate;
    private final List<ServerComPort> comPorts = new ArrayList<>();
    private boolean obsoleteFlag;
    private Date obsoleteDate;

    @Inject
    protected ComServerImpl(DataModel dataModel, EngineModelService engineModelService, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServerServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ServerModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<ServerTCPBasedInboundComPort> tcpBasedInboundComPortProvider) {
        super();
        this.dataModel = dataModel;
        this.engineModelService = engineModelService;
        this.outboundComPortProvider = outboundComPortProvider;
        this.servletBasedInboundComPortProvider = servletBasedInboundComPortProvider;
        this.modemBasedInboundComPortProvider = modemBasedInboundComPortProvider;
        this.tcpBasedInboundComPortProvider = tcpBasedInboundComPortProvider;
    }

    private List<ServerComPort> getServerComPorts () {
        return ImmutableList.copyOf(this.comPorts);
    }

    protected void validate(){
        this.validate(name);
        this.validateConstraint(name);
        this.validateNotNull(this.getServerLogLevel(), "comserver.serverLogLevel");
        this.validateNotNull(this.getCommunicationLogLevel(), "comserver.comLogLevel");
        this.validateChangesInterPollDelay();
        this.validateSchedulingInterPollDelay();
    }

    private void validateChangesInterPollDelay() {
        this.validateNotNull(this.getChangesInterPollDelay(), CHANGES_INTER_POLL_DELAY_RESOURCE_KEY);
        this.validateBigger(this.getChangesInterPollDelay(), MINIMUM_INTERPOLL_DELAY, CHANGES_INTER_POLL_DELAY_RESOURCE_KEY);
    }

    private void validateSchedulingInterPollDelay()  {
        this.validateNotNull(this.getSchedulingInterPollDelay(), SCHEDULING_INTER_POLL_DELAY_RESOURCE_KEY);
        this.validateBigger(this.getSchedulingInterPollDelay(), MINIMUM_INTERPOLL_DELAY, SCHEDULING_INTER_POLL_DELAY_RESOURCE_KEY);
    }

    protected void validateUpdateAllowed() {
        if (this.obsoleteFlag) {
            throw new TranslatableApplicationException("comserver.noUpdateAllowed", "Obsolete ComServers can no longer be updated");
        }
    }

    protected void validateConstraint (String name) {
        ComServer comServerWithTheSameName = engineModelService.findComServer(name);
        if (comServerWithTheSameName!=null && this.getId() != comServerWithTheSameName.getId() && !comServerWithTheSameName.isObsolete()) {
            throw new TranslatableApplicationException("duplicateComServerX", "A ComServer by the name of \"{0}\" already exists (id={1})", name, comServerWithTheSameName.getId());
        }
    }

    protected void validateNotNull (Object propertyValue, String propertyName) {
        if (propertyValue == null) {
            throw new TranslatableApplicationException("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
        }
    }

    protected void validateBigger (TimeDuration propertyValue, TimeDuration minimum, String propertyName) {
        if (minimum.compareTo(propertyValue) > 0) {
            throw new TranslatableApplicationException("XshouldBeAtLeast", "Minimal acceptable value for \"{0}\" is \"{1}\"", propertyName, minimum);
        }
    }

    public void makeObsolete () {
        this.validateMakeObsolete();
        this.makeComPortsObsolete();
        this.obsoleteFlag = true;
        this.obsoleteDate = new Date();
        dataModel.update(this);
    }

    private void makeComPortsObsolete () {
        for (ServerComPort comPort : this.getServerComPorts()) {
            comPort.makeObsolete();
        }
    }

    protected void validateMakeObsolete () {
        if (this.isObsolete()) {
            throw new TranslatableApplicationException("comServerIsAlreadyObsolete",
                    "The ComServer with id {0} is already obsolete since {1,date,yyyy-MM-dd HH:mm:ss}",
                    this.getId(),
                    this.getObsoleteDate());
        }
    }

    protected void validateDelete() {

    }

    @Override
    public List<ComPort> getComPorts() {
        List<ComPort> comPorts = new ArrayList<>();
        for (ComPort comPort : this.getServerComPorts()) {
            comPorts.add(comPort);
        }
        return ImmutableList.copyOf(comPorts);
    }

    public void setComPorts(List<ComPort> comPorts) {
        //todo merge lists in stead of delting re-adding
        this.comPorts.clear();
        for (ComPort comPort : comPorts) {
            this.comPorts.add((ServerComPort) comPort);
        }
    }

    public final  List<InboundComPort> getInboundComPorts () {
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (ComPort comPort : this.getServerComPorts()) {
            if (comPort.isInbound()) {
                InboundComPort inboundComPort = (InboundComPort) comPort;
                inboundComPorts.add(inboundComPort);
            }
        }
        return ImmutableList.copyOf(inboundComPorts);
    }

    public final List<OutboundComPort> getOutboundComPorts() {
        List<OutboundComPort> outboundComPorts = new ArrayList<>();
        for (ComPort comPort : this.getServerComPorts()) {
            if (!comPort.isInbound()) {
                OutboundComPort outboundComPort = (OutboundComPort) comPort;
                outboundComPorts.add(outboundComPort);
            }
        }
        return ImmutableList.copyOf(outboundComPorts);
    }

    @Override
    public OutboundComPort.OutboundComPortBuilder newOutboundComPort() {
        return new OutboundComPortBuilder();
    }

    /**
     * Builders are used to facilitate validating ComPorts??
     */
    private class OutboundComPortBuilder extends OutboundComPortImpl.OutboundComPortBuilderImpl {

        private OutboundComPortBuilder() {
            super(outboundComPortProvider);
            comPort.init(ComServerImpl.this);
        }

        @Override
        public ServerOutboundComPort add() {
            ServerOutboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    public ServletBasedComPortBuilder newServletBasedInboundComPort() {
        return new ServletBasedComPortBuilder();
    }

    private class ServletBasedComPortBuilder extends ServletBasedInboundComPortImpl.ServletBasedInboundComPortBuilderImpl {

        protected ServletBasedComPortBuilder() {
            super(servletBasedInboundComPortProvider);
            comPort.init(ComServerImpl.this);
        }

        @Override
        public ServerServletBasedInboundComPort add() {
            ServerServletBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    public ModemBasedComPortBuilder newModemBasedInboundComport() {
        return new ModemBasedComPortBuilder();
    }

    private class ModemBasedComPortBuilder extends ModemBasedInboundComPortImpl.ModemBasedInboundComPortBuilderImpl {

        protected ModemBasedComPortBuilder() {
            super(modemBasedInboundComPortProvider);
            comPort.init(ComServerImpl.this);
        }

        @Override
        public ServerModemBasedInboundComPort add() {
            ServerModemBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    public TCPBasedComPortBuilder newTCPBasedInboundComPort() {
        return new TCPBasedComPortBuilder();
    }

    private class TCPBasedComPortBuilder extends TCPBasedInboundComPortImpl.TCPBasedInboundComPortBuilderImpl {

        protected TCPBasedComPortBuilder() {
            super(tcpBasedInboundComPortProvider);
            comPort.init(ComServerImpl.this);
        }

        @Override
        public ServerTCPBasedInboundComPort add() {
            ServerTCPBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

//            @Override
//            public UDPBasedInboundComPort createUDPBasedInbound (final UDPBasedInboundComPortShadow shadow) throws BusinessException, SQLException {
//                shadow.setComServerId(this.getId());
//                ServerUDPBasedInboundComPort comPort = this.getComPortFactory().createUDPBasedInbound(this, shadow);
//                this.post();
//                this.addToComPortCache(comPort);
//                return comPort;
//            }
//
    protected void validate (String newName) {
        validateNotNull(newName, "name");
        /* Validation provided by superclass work with a set of invalid
         * characters (see getInvalidCharacters()) but actually,
         * a set of valid chars works a lot better in this case.
         * Therefore, getInvalidCharacters will return "" and
         * superclass will only validate null and empty String.
         */
        this.checkContainsOnlyRFC1035Chars(newName);
    }

    /**
     * Checks that the specified name contains only characters
     * that are allowed by the RCC 1035 specification as the name
     * of a ComServer will be used as a hostname in URLs later on.
     *
     * @param name The name to be verified
     */
    private void checkContainsOnlyRFC1035Chars (String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!this.isRFC1035Char(name.charAt(i))) {
                throw new TranslatableApplicationException("nameXcontainsInvalidChars", "The name \"{0}\" contains invalid characters", name);
            }
        }
    }

    private boolean isRFC1035Char (char c) {
        return Character.isDigit(c) || Character.isAlphabetic(c) || c == '.' || c == '-';
    }

    public UtcInstant getModificationDate() {
        return modificationDate;
    }

    /**
     * Returns the default event registration URI for this ComServer.
     *
     * @return The default event registration URI
     */
    protected String defaultEventRegistrationUri () {
        return "ws://" + this.getName() + ":" + DEFAULT_EVENT_REGISTRATION_PORT_NUMBER + "/events/registration";
    }

    /**
     * Returns the default query api URI for this ComServer.
     *
     * @return The default query api URI
     */
    protected String defaultQueryApiPostUri () {
        return "http://" + this.getName() + ":" + DEFAULT_QUERY_API_PORT_NUMBER + "/remote/queries";
    }

    @Override
    public boolean isOnline () {
        return false;
    }

    @Override
    public boolean isRemote () {
        return false;
    }

    @Override
    public boolean isOffline () {
        return false;
    }

    @Override
    public String getName () {
        return this.name;
    }

    @Override
    public boolean isActive () {
        return active;
    }

    @Override
    public LogLevel getServerLogLevel () {
        return serverLogLevel;
    }

    @Override
    public LogLevel getCommunicationLogLevel () {
        return communicationLogLevel;
    }

    @Override
    public TimeDuration getChangesInterPollDelay () {
        if (this.changesInterPollDelay!=null) {
            return new TimeDuration(this.changesInterPollDelay.getCount(), this.changesInterPollDelay.getTimeUnitCode());
        }
        return null;
    }

    @Override
    public TimeDuration getSchedulingInterPollDelay () {
        if (this.schedulingInterPollDelay!=null) {
            return new TimeDuration(this.schedulingInterPollDelay.getCount(), this.schedulingInterPollDelay.getTimeUnitCode());
        }
        return null;
    }

    @Override
    public boolean isObsolete () {
        return this.obsoleteFlag;
    }

    @Override
    public Date getObsoleteDate () {
        return this.obsoleteDate;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setServerLogLevel(LogLevel serverLogLevel) {
        this.serverLogLevel = serverLogLevel;
    }

    public void setCommunicationLogLevel(LogLevel communicationLogLevel) {
        this.communicationLogLevel = communicationLogLevel;
    }

    public void setChangesInterPollDelay(TimeDuration changesInterPollDelay) {
        if (changesInterPollDelay!=null) {
            this.changesInterPollDelay = new TimeDuration(changesInterPollDelay.getCount(), changesInterPollDelay.getTimeUnitCode());
        }
    }

    public void setSchedulingInterPollDelay(TimeDuration schedulingInterPollDelay) {
        if (schedulingInterPollDelay!=null) {
            this.schedulingInterPollDelay = new TimeDuration(schedulingInterPollDelay.getCount(), schedulingInterPollDelay.getTimeUnitCode());
        }
    }

    @Override
    public String getEventRegistrationUriIfSupported () throws BusinessException {
        throw new BusinessException("ComServerXDoesNotSupportEventRegistration", "The comserver {0} does not support event registration", this.getName());
    }

    @Override
    public String getQueryApiPostUriIfSupported () throws BusinessException {
        throw new BusinessException("ComServerXDoesNotSupportRemoteQueries", "The comserver {0} does not support remote queries", this.getName());
    }

    public void save() {
        validate();
        if (this.getId()==0) {
            dataModel.persist(this);
        } else {
            validateUpdateAllowed();
            dataModel.update(this);
        }
    }

    @Override
    public void delete() {
        validateDelete();
        this.comPorts.clear();
        dataModel.remove(this);
    }

    @Override
    public String toString() {
        if (this.isObsolete()) {
            return getName() + " (deleted on "+getObsoleteDate()+")";
        }
        else {
            return this.getName();
        }

    }
}