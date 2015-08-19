package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.MinTimeDuration;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.ComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (10:20)
 */
@XmlRootElement
@UniqueName(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER+"}")
public abstract class ComServerImpl implements ComServer {

    protected static final String ONLINE_COMSERVER_DISCRIMINATOR = "0";
    protected static final String OFFLINE_COMSERVER_DISCRIMINATOR = "1";
    protected static final String REMOTE_COMSERVER_DISCRIMINATOR = "2";

    static final Map<String, Class<? extends ComServer>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComServer>>of(
                    ONLINE_COMSERVER_DISCRIMINATOR, OnlineComServerImpl.class,
                    OFFLINE_COMSERVER_DISCRIMINATOR, OfflineComServerImpl.class,
                    REMOTE_COMSERVER_DISCRIMINATOR, RemoteComServerImpl.class);

    private final DataModel dataModel;
    private final Provider<OutboundComPortImpl> outboundComPortProvider;

    private final Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider;
    private final Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider;
    private final Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider;
    private final Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider;
    protected final Thesaurus thesaurus;

    /**
     * Notifies this ComServer that the specified {@link ComPortImpl} was saved.
     *
     * @param comPort The ComPort that was saved
     */
    void saved(ComPortImpl comPort) {
        this.dataModel.touch(this);
    }

    enum FieldNames {
        NAME("name");
        private final String name;

        FieldNames(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    @Pattern(regexp="[a-zA-Z0-9\\.\\-]*", groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.COMSERVER_NAME_INVALID_CHARS +"}")
    private String name;
    private boolean active;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private LogLevel serverLogLevel;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private LogLevel communicationLogLevel;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    @MinTimeDuration(value = 60 ,groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}")
    private TimeDuration changesInterPollDelay;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    @MinTimeDuration(value = 60 ,groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}")
    private TimeDuration schedulingInterPollDelay;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    private final List<ComPort>  comPorts = new ArrayList<>();
    @Null(groups = { Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_COMSERVER_NO_UPDATE_ALLOWED+"}")
    private Instant obsoleteDate;

    @Inject
    protected ComServerImpl(DataModel dataModel, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.outboundComPortProvider = outboundComPortProvider;
        this.servletBasedInboundComPortProvider = servletBasedInboundComPortProvider;
        this.modemBasedInboundComPortProvider = modemBasedInboundComPortProvider;
        this.tcpBasedInboundComPortProvider = tcpBasedInboundComPortProvider;
        this.udpBasedInboundComPortProvider = udpBasedInboundComPortProvider;
        this.thesaurus = thesaurus;
    }

    protected void validate(){
    }

    @Override
    public void makeObsolete () {
        this.validateMakeObsolete();
        this.makeComPortsObsolete();
        this.obsoleteDate = Instant.now();
        dataModel.update(this);
    }

    private void makeComPortsObsolete () {
        for (ComPort comPort : this.getComPorts()) {
            comPort.makeObsolete();
        }
    }

    protected void validateMakeObsolete () {
        if (this.isObsolete()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.IS_ALREADY_OBSOLETE);
        }
    }

    @Override
    public void delete() {
        validateDelete();
        this.comPorts.clear();
        dataModel.remove(this);
    }

    protected void validateDelete() {

    }

    @Override
    public List<ComPort> getComPorts() {
        List<ComPort> nonObsoleteComPorts = new ArrayList<>();
        for (ComPort comPort : this.comPorts) {
            if (!comPort.isObsolete()) {
                nonObsoleteComPorts.add(comPort);
            }
        }

        return nonObsoleteComPorts;
    }

    public final  List<InboundComPort> getInboundComPorts () {
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (ComPort comPort : this.comPorts) {
            if (comPort.isInbound() && !comPort.isObsolete()) {
                InboundComPort inboundComPort = (InboundComPort) comPort;
                inboundComPorts.add(inboundComPort);
            }
        }
        return ImmutableList.copyOf(inboundComPorts);
    }

    public final List<OutboundComPort> getOutboundComPorts() {
        List<OutboundComPort> outboundComPorts = new ArrayList<>();
        for (ComPort comPort : this.comPorts) {
            if (!comPort.isInbound() && !comPort.isObsolete()) {
                OutboundComPort outboundComPort = (OutboundComPort) comPort;
                outboundComPorts.add(outboundComPort);
            }
        }
        return ImmutableList.copyOf(outboundComPorts);
    }

    @Override
    public OutboundComPort.OutboundComPortBuilder newOutboundComPort(String name, int numberOfSimultaneousConnections) {
        return new OutboundComPortBuilder(name, numberOfSimultaneousConnections);
    }

    private class OutboundComPortBuilder extends OutboundComPortImpl.OutboundComPortBuilderImpl {

        private OutboundComPortBuilder(String name, int numberOfSimultaneousConnections) {
            super(outboundComPortProvider.get(), name);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
            comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        }

        @Override
        public OutboundComPort add() {
            OutboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public ServletBasedInboundComPort.ServletBasedInboundComPortBuilder newServletBasedInboundComPort(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber) {
        return new ServletBasedComPortBuilder(name, contextPath, numberOfSimultaneousConnections, portNumber);
    }

    public class ServletBasedComPortBuilder extends ServletBasedInboundComPortImpl.ServletBasedInboundComPortBuilderImpl
        implements ServletBasedInboundComPort.ServletBasedInboundComPortBuilder {

        protected ServletBasedComPortBuilder(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber) {
            super(servletBasedInboundComPortProvider.get(), name, numberOfSimultaneousConnections, portNumber);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
            comPort.setContextPath(contextPath);
        }

        @Override
        public ServletBasedInboundComPort add() {
            ServletBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder newModemBasedInboundComport(String name, int ringCount, int maximumDialErrors,
                                               TimeDuration connectTimeout, TimeDuration atCommandTimeout,
                                               SerialPortConfiguration serialPortConfiguration) {
        return new ModemBasedComPortBuilder(name, ringCount, maximumDialErrors, connectTimeout, atCommandTimeout, serialPortConfiguration);
    }

    public class ModemBasedComPortBuilder extends ModemBasedInboundComPortImpl.ModemBasedInboundComPortBuilderImpl
            implements ModemBasedInboundComPort.ModemBasedInboundComPortBuilder {

        protected ModemBasedComPortBuilder(String name, int ringCount, int maximumDialErrors,
                                           TimeDuration connectTimeout, TimeDuration atCommandTimeout,
                                           SerialPortConfiguration serialPortConfiguration) {
            super(modemBasedInboundComPortProvider.get(),name);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
            comPort.setRingCount(ringCount);
            comPort.setMaximumDialErrors(maximumDialErrors);
            comPort.setConnectTimeout(connectTimeout);
            comPort.setAtCommandTimeout(atCommandTimeout);
            comPort.setSerialPortConfiguration(serialPortConfiguration);
        }

        @Override
        public ModemBasedInboundComPort add() {
            ModemBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public TCPBasedInboundComPort.TCPBasedInboundComPortBuilder newTCPBasedInboundComPort(String name, int numberOfSimultaneousConnections, int portNumber) {
        return new TCPBasedComPortBuilder(name, numberOfSimultaneousConnections, portNumber);
    }

    public class TCPBasedComPortBuilder extends TCPBasedInboundComPortImpl.TCPBasedInboundComPortBuilderImpl
            implements TCPBasedInboundComPort.TCPBasedInboundComPortBuilder {

        protected TCPBasedComPortBuilder(String name, int numberOfSimultaneousConnections, int portNumber) {
            super(tcpBasedInboundComPortProvider.get(), name, numberOfSimultaneousConnections, portNumber);
            ((ComPortImpl)this.comPort).setComServer(ComServerImpl.this);
            this.comPort.setComPortType(ComPortType.TCP);
        }

        @Override
        public TCPBasedInboundComPort add() {
            TCPBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public UDPBasedInboundComPort.UDPBasedInboundComPortBuilder newUDPBasedInboundComPort(String name, int numberOfSimultaneousConnections, int portNumber) {
        return new UDPBasedComPortBuilder(name, numberOfSimultaneousConnections, portNumber);
    }

    public class UDPBasedComPortBuilder extends UDPBasedInboundComPortImpl.UDPBasedInboundComPortBuilderImpl
        implements UDPBasedInboundComPort.UDPBasedInboundComPortBuilder {

        protected UDPBasedComPortBuilder(String name, int numberOfSimultaneousConnections, int portNumber) {
            super(udpBasedInboundComPortProvider.get(), name, numberOfSimultaneousConnections, portNumber);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
        }

        @Override
        public UDPBasedInboundComPort add() {
            UDPBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    public Instant getModificationDate() {
        return this.modTime;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public void removeComPort(long id) {
        for (ComPort next : comPorts) {
            if (next.getId() == id) {
                if (!next.isObsolete()) {
                    next.makeObsolete();
                }
            }
        }
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
        return "ws://" + this.getName() + ":" + DEFAULT_QUERY_API_PORT_NUMBER + "/remote/queries";
    }

    /**
     * Returns the default event registration URI for this ComServer.
     *
     * @return The default event registration URI
     */
    protected String defaultStatusUri () {
        return "http://" + this.getName() + ":8080/api/dsr/comserverstatus";
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

    @XmlElement(name = "type")
    public String getXmlType () {
        return this.getClass().getSimpleName();
    }

    public void setXmlType (String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    @XmlAttribute
    public String getName () {
        return this.name;
    }

    @Override
    @XmlAttribute
    public boolean isActive () {
        return active;
    }

    @Override
    @XmlAttribute
    public LogLevel getServerLogLevel () {
        return serverLogLevel;
    }

    @Override
    @XmlAttribute
    public LogLevel getCommunicationLogLevel () {
        return communicationLogLevel;
    }

    @Override
    @XmlElement
    public TimeDuration getChangesInterPollDelay () {
        if (this.changesInterPollDelay!=null) {
            return new TimeDuration(this.changesInterPollDelay.getCount(), this.changesInterPollDelay.getTimeUnitCode());
        }
        return null;
    }

    @Override
    @XmlElement
    public TimeDuration getSchedulingInterPollDelay () {
        if (this.schedulingInterPollDelay!=null) {
            return new TimeDuration(this.schedulingInterPollDelay.getCount(), this.schedulingInterPollDelay.getTimeUnitCode());
        }
        return null;
    }

    @Override
    @XmlElement
    public boolean isObsolete () {
        return this.obsoleteDate!=null;
    }

    @Override
    @XmlElement
    public Instant getObsoleteDate () {
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

    public final void save() {
        Save.action(this.getId()).save(dataModel, this);
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