package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.ComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (10:20)
 */
@XmlRootElement
@UniqueName(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_DUPLICATE_COM_SERVER+"}")
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

    private long id;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    @Pattern(regexp="[a-zA-Z0-9\\.\\-]+", groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_INVALID_CHARS+"}")
    private String name;
    private boolean active;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private LogLevel serverLogLevel;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private LogLevel communicationLogLevel;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    @MinTimeDuration(value = 60 ,groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_VALUE_TOO_SMALL+"}")
    private TimeDuration changesInterPollDelay;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    @MinTimeDuration(value = 60 ,groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_VALUE_TOO_SMALL+"}")
    private TimeDuration schedulingInterPollDelay;
    private Date modificationDate;
    private final List<ComPort>  comPorts = new ArrayList<>();
    @Null(groups = { Save.Update.class }, message = "{"+Constants.MDC_COMSERVER_NO_UPDATE_ALLOWED+"}")
    private Date obsoleteDate;

    @Inject
    protected ComServerImpl(DataModel dataModel, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider) {
        super();
        this.dataModel = dataModel;
        this.outboundComPortProvider = outboundComPortProvider;
        this.servletBasedInboundComPortProvider = servletBasedInboundComPortProvider;
        this.modemBasedInboundComPortProvider = modemBasedInboundComPortProvider;
        this.tcpBasedInboundComPortProvider = tcpBasedInboundComPortProvider;
        this.udpBasedInboundComPortProvider = udpBasedInboundComPortProvider;
    }

    protected void validate(){
    }

    public void makeObsolete () {
        this.validateMakeObsolete();
        this.makeComPortsObsolete();
        this.obsoleteDate = new Date();
        dataModel.update(this);
    }

    private void makeComPortsObsolete () {
        for (ComPort comPort : this.getComPorts()) {
            comPort.makeObsolete();
        }
    }

    protected void validateMakeObsolete () {
        if (this.isObsolete()) {
            throw new TranslatableApplicationException(Constants.MDC_IS_ALREADY_OBSOLETE, "Already obsolete");
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
    public OutboundComPort.OutboundComPortBuilder newOutboundComPort() {
        return new OutboundComPortBuilder();
    }

    /**
     * Builders are used to facilitate validating ComPorts??
     */
    private class OutboundComPortBuilder extends OutboundComPortImpl.OutboundComPortBuilderImpl {

        private OutboundComPortBuilder() {
            super(outboundComPortProvider);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
        }

        @Override
        public OutboundComPort add() {
            OutboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public ServletBasedInboundComPort.ServletBasedInboundComPortBuilder newServletBasedInboundComPort() {
        return new ServletBasedComPortBuilder();
    }

    public class ServletBasedComPortBuilder extends ServletBasedInboundComPortImpl.ServletBasedInboundComPortBuilderImpl
        implements ServletBasedInboundComPort.ServletBasedInboundComPortBuilder {

        protected ServletBasedComPortBuilder() {
            super(servletBasedInboundComPortProvider);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
        }

        @Override
        public ServletBasedInboundComPort add() {
            ServletBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder newModemBasedInboundComport() {
        return new ModemBasedComPortBuilder();
    }

    public class ModemBasedComPortBuilder extends ModemBasedInboundComPortImpl.ModemBasedInboundComPortBuilderImpl
            implements ModemBasedInboundComPort.ModemBasedInboundComPortBuilder {

        protected ModemBasedComPortBuilder() {
            super(modemBasedInboundComPortProvider);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
        }

        @Override
        public ModemBasedInboundComPort add() {
            ModemBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public TCPBasedInboundComPort.TCPBasedInboundComPortBuilder newTCPBasedInboundComPort() {
        return new TCPBasedComPortBuilder();
    }

    public class TCPBasedComPortBuilder extends TCPBasedInboundComPortImpl.TCPBasedInboundComPortBuilderImpl
            implements TCPBasedInboundComPort.TCPBasedInboundComPortBuilder {

        protected TCPBasedComPortBuilder() {
            super(tcpBasedInboundComPortProvider);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
            comPort.setComPortType(ComPortType.TCP);
        }

        @Override
        public TCPBasedInboundComPort add() {
            TCPBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    @Override
    public UDPBasedInboundComPort.UDPBasedInboundComPortBuilder newUDPBasedInboundComPort() {
        return new UDPBasedComPortBuilder();
    }

    public class UDPBasedComPortBuilder extends UDPBasedInboundComPortImpl.UDPBasedInboundComPortBuilderImpl
        implements UDPBasedInboundComPort.UDPBasedInboundComPortBuilder {

        protected UDPBasedComPortBuilder() {
            super(udpBasedInboundComPortProvider);
            ((ComPortImpl)comPort).setComServer(ComServerImpl.this);
        }

        @Override
        public UDPBasedInboundComPort add() {
            UDPBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    public Date getModificationDate() {
        return modificationDate;
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
        return this.obsoleteDate!=null;
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

    final public void save() {
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