/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.common.rest.MinTimeDuration;
import com.energyict.mdc.ports.ComPortType;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link ComServer} interface.
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

    private static final Logger LOGGER = Logger.getLogger(ComServerImpl.class.getName());

    static final Map<String, Class<? extends ComServer>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComServer>>of(
                    ONLINE_COMSERVER_DISCRIMINATOR, OnlineComServerImpl.class,
                    OFFLINE_COMSERVER_DISCRIMINATOR, OfflineComServerImpl.class,
                    REMOTE_COMSERVER_DISCRIMINATOR, RemoteComServerImpl.class);

    private DataModel dataModel;
    private Provider<OutboundComPort> outboundComPortProvider;

    private Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider;
    private Provider<CoapBasedInboundComPort> coapBasedInboundComPortProvider;
    private Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider;
    private Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider;
    private Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider;
    protected Thesaurus thesaurus;

    /**
     * Notifies this ComServer that the specified {@link ComPortImpl} was saved.
     *
     * @param comPort The ComPort that was saved
     */
    public void saved(ComPort comPort) {
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
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String serverMonitorUrl;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    private List<ComPort>  comPorts = new ArrayList<>();
    @Null(groups = { Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_COMSERVER_NO_UPDATE_ALLOWED+"}")
    private Instant obsoleteDate;
    protected boolean obsolete;
    protected boolean remote;
    protected boolean online;
    protected boolean offline;

    protected ComServerImpl() {
        super();
    }

    @Inject
    protected ComServerImpl(DataModel dataModel, Provider<OutboundComPort> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<CoapBasedInboundComPort> coapBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.outboundComPortProvider = outboundComPortProvider;
        this.servletBasedInboundComPortProvider = servletBasedInboundComPortProvider;
        this.coapBasedInboundComPortProvider = coapBasedInboundComPortProvider;
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

    @Override
    public void update() {
        save();
    }

    protected void validateDelete() {

    }

    @Override
    @XmlElements({
            @XmlElement(name = "OutboundComPortImpl", type = OutboundComPortImpl.class),
            @XmlElement(name = "InboundComPortImpl", type = InboundComPortImpl.class)
    })
    public List<ComPort> getComPorts() {
        List<ComPort> nonObsoleteComPorts = new ArrayList<>();
        for (ComPort comPort : this.comPorts) {
            if (!comPort.isObsolete()) {
                nonObsoleteComPorts.add(comPort);
            }
        }

        return nonObsoleteComPorts;
    }

    public List<InboundComPort> getInboundComPorts () {
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (ComPort comPort : this.comPorts) {
            if (comPort.isInbound() && !comPort.isObsolete()) {
                InboundComPort inboundComPort = (InboundComPort) comPort;
                inboundComPorts.add(inboundComPort);
            }
        }
        return Collections.unmodifiableList(inboundComPorts);
    }

    @XmlElement(type = OutboundComPortImpl.class)
    public List<OutboundComPort> getOutboundComPorts() {
        List<OutboundComPort> outboundComPorts = new ArrayList<>();
        for (ComPort comPort : this.comPorts) {
            if (!comPort.isInbound() && !comPort.isObsolete()) {
                OutboundComPortImpl outboundComPort = (OutboundComPortImpl) comPort;
                outboundComPorts.add(outboundComPort);
            }
        }
        return outboundComPorts;
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
    public CoapBasedInboundComPort.CoapBasedInboundComPortBuilder newCoapBasedInboundComPort(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber) {
        return new CoapBasedComPortBuilder(name, contextPath, numberOfSimultaneousConnections, portNumber);
    }

    @Override
    public ServletBasedInboundComPort.ServletBasedInboundComPortBuilder newServletBasedInboundComPort(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber) {
        return new ServletBasedComPortBuilder(name, contextPath, numberOfSimultaneousConnections, portNumber);
    }

    public class CoapBasedComPortBuilder extends CoapBasedInboundComPortImpl.CoapBasedInboundComPortBuilderImpl
            implements CoapBasedInboundComPort.CoapBasedInboundComPortBuilder {

        protected CoapBasedComPortBuilder(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber) {
            super(coapBasedInboundComPortProvider.get(), name, numberOfSimultaneousConnections, portNumber);
            ((ComPortImpl) comPort).setComServer(ComServerImpl.this);
            comPort.setContextPath(contextPath);
        }

        @Override
        public CoapBasedInboundComPort add() {
            CoapBasedInboundComPort comPort = super.add();
            ComServerImpl.this.comPorts.add(comPort);
            return comPort;
        }
    }

    public class ServletBasedComPortBuilder extends ServletBasedInboundComPortImpl.ServletBasedInboundComPortBuilderImpl
            implements ServletBasedInboundComPort.ServletBasedInboundComPortBuilder {

        protected ServletBasedComPortBuilder(String name, String contextPath, int numberOfSimultaneousConnections, int portNumber) {
            super(servletBasedInboundComPortProvider.get(), name, numberOfSimultaneousConnections, portNumber);
            ((ComPortImpl) comPort).setComServer(ComServerImpl.this);
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

    @XmlElement
    public Instant getModTime() {
        return this.modTime;
    }

    @Override
    @XmlElement
    public long getVersion() {
        return this.version;
    }

    @Override
    public void removeComPort(long id) {
        for (ComPort next : comPorts) {
            if (next.getId() == id) {
                if (!next.isObsolete()) {
                    next.makeObsolete();
                    this.dataModel.touch(this);
                }
            }
        }
    }

    /**
     * Builds the event registration URI for this ComServer.
     *
     * @return The event registration URI
     */
    protected String buildEventRegistrationUri(String serverName, int eventRegistrationPort) {
        return MessageFormat.format(EVENT_REGISTRATION_URI_PATTERN, serverName, Integer.toString(eventRegistrationPort));
    }

    /**
     * Build the query api post uri for this ComServer.
     *
     * @return The query api URI
     */
    protected String buildQueryApiPostUri(String serverName, int queryApiPort) {
        return MessageFormat.format(QUERY_API_URI_PATTERN, serverName, Integer.toString(queryApiPort));
    }

    /**
     * Builds the event registration URI for this ComServer.
     *
     * @return The event registration URI
     */
    protected String buildStatusUri(String serverName, int statusPort) {
        return MessageFormat.format(STATUS_URI_ATTERN, serverName, Integer.toString(statusPort));
    }

    @Override
    @XmlElement
    public boolean isOnline () {
        online = false;
        return online;
    }

    @Override
    @XmlElement
    public boolean isRemote () {
        remote = false;
        return remote;
    }

    @Override
    @XmlElement
    public boolean isOffline () {
        offline = false;
        return offline;
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
        obsolete = (this.obsoleteDate != null);
        return obsolete;
    }

    @Override
    @XmlElement
    public Instant getObsoleteDate () {
        return this.obsoleteDate;
    }

    public long getId() {
        return id;
    }

    @Override
    @XmlElement
    public String getServerMonitorUrl() { return serverMonitorUrl; }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
        LOGGER.info("ComServer " + name + " status set to " + active);
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
    public void setServerMonitorUrl (String serverMonitorUrl) {
        this.serverMonitorUrl = serverMonitorUrl;
    }

    @Override
    @XmlElement
    public String getEventRegistrationUriIfSupported () {
        throw new UnsupportedOperationException("The comserver " + this.getName() + " does not support event registration");
    }

    @Override
    public String getQueryApiPostUriIfSupported () {
        throw new UnsupportedOperationException("The comserver " + this.getName() + " does not support remote queries");
    }

    final void save() {
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComServerImpl comServer = (ComServerImpl) o;

        return id == comServer.id;

    }

    @Override
    public final int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public static abstract class AbstractComServerBuilder<CS extends ComServerImpl, CSB extends ComServerBuilder> implements ComServerBuilder<CS, CSB> {

        final CS comServerInstance;
        private CSB me;

        protected AbstractComServerBuilder(CS comServerInstance, Class<CSB> clazz) {
            this.comServerInstance = comServerInstance;
            this.me = clazz.cast(this); // typesafe cast ...
        }

        public void me(CSB me) {
            this.me = me;
        }

        protected CS getComServerInstance() {
            return comServerInstance;
        }

        @Override
        public CSB name(String comServerName) {
            comServerInstance.setName(comServerName);
            return me;
        }

        @Override
        public CSB changesInterPollDelay(TimeDuration changesInterPollDelay) {
            comServerInstance.setChangesInterPollDelay(changesInterPollDelay);
            return me;
        }

        @Override
        public CSB schedulingInterPollDelay(TimeDuration schedulingInterPollDelay) {
            comServerInstance.setSchedulingInterPollDelay(schedulingInterPollDelay);
            return me;
        }

        @Override
        public CSB communicationLogLevel(LogLevel logLevel) {
            comServerInstance.setCommunicationLogLevel(logLevel);
            return me;
        }

        @Override
        public CSB serverLogLevel(LogLevel logLevel) {
            comServerInstance.setServerLogLevel(logLevel);
            return me;
        }

        @Override
        public CSB active(boolean active) {
            comServerInstance.setActive(active);
            LOGGER.info("ComServer " + comServerInstance.getName() + " status set to " + active);
            Thread.dumpStack();
            return me;
        }

        @Override
        public CS create() {
            comServerInstance.save();
            return comServerInstance;
        }
    }

}