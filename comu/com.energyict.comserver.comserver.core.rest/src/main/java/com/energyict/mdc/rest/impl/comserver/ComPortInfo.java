/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.ports.ComPortType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TcpInboundComPortInfo.class, name = "inbound_TCP"),
        @JsonSubTypes.Type(value = UdpInboundComPortInfo.class, name = "inbound_UDP"),
        @JsonSubTypes.Type(value = ModemInboundComPortInfo.class, name = "inbound_SERIAL"),
        @JsonSubTypes.Type(value = CoapInboundComPortInfo.class, name = "inbound_COAP"),
        @JsonSubTypes.Type(value = ServletInboundComPortInfo.class, name = "inbound_SERVLET"),
        @JsonSubTypes.Type(value = TcpOutboundComPortInfo.class, name = "outbound_TCP"),
        @JsonSubTypes.Type(value = UdpOutboundComPortInfo.class, name = "outbound_UDP"),
        @JsonSubTypes.Type(value = ModemOutboundComPortInfo.class, name = "outbound_SERIAL")})
@JsonIgnoreProperties(ignoreUnknown = true) // support for FE delete request (which uses common port model with all fields)
public abstract class ComPortInfo<T extends ComPort, B extends ComPort.Builder<B,T>> {

    public long id;
    public String direction;
    public String name;
    public String description;
    public Boolean active = Boolean.FALSE;
    public Boolean bound;
    public ComPortTypeInfo comPortType;
    public Long comServer_id;
    public String comServerName;
    public Integer numberOfSimultaneousConnections = Integer.valueOf(0);
    public Integer ringCount;
    public Integer maximumNumberOfDialErrors;
    public TimeDurationInfo connectTimeout;
    public TimeDurationInfo delayAfterConnect;
    public TimeDurationInfo delayBeforeSend;
    public TimeDurationInfo atCommandTimeout;
    public BigDecimal atCommandTry;
    public List<Map<String, String>> modemInitStrings;
    public List<Map<String, String>> globalModemInitStrings;
    public String addressSelector;
    public String postDialCommands;
    @XmlJavaTypeAdapter(BaudrateAdapter.class)
    public BaudrateValue baudrate;
    @XmlJavaTypeAdapter(NrOfDataBitsAdapter.class)
    public NrOfDataBits nrOfDataBits;
    @XmlJavaTypeAdapter(NrOfStopBitsAdapter.class)
    public NrOfStopBits nrOfStopBits;
    public FlowControlInfo flowControl;
    public ParitiesInfo parity;
    public Integer portNumber;
    public Integer bufferSize;
    public Boolean useDtls;
    public Boolean useSharedKeys;
    public Boolean useHttps;
    public String keyStoreFilePath;
    public String trustStoreFilePath;
    public String keyStorePassword;
    public String trustStorePassword;
    public String contextPath;
    public long version;
    public VersionInfo<Long> parent;

    public ComPortInfo() {
    }

    public ComPortInfo(ComPort comPort) {
        this.id = comPort.getId();
        this.name = comPort.getName();
        this.description = comPort.getDescription();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comServer_id = comPort.getComServer()!=null ? comPort.getComServer().getId() : 0L;
        this.comServerName = comPort.getComServer() != null ? comPort.getComServer().getName() : null;
        this.comPortType = new ComPortTypeInfo(comPort.getComPortType());
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.version = comPort.getVersion();
        this.parent = new VersionInfo<>(this.comServer_id, comPort.getComServer() != null ? comPort.getComServer().getVersion() : 0L);
    }

    protected void writeTo(T source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        Optional<String> name = Optional.ofNullable(this.name);
        if(name.isPresent()) {
            source.setName(name.get());
        }
        Optional<String> description = Optional.ofNullable(this.description);
        if(description.isPresent()) {
            source.setDescription(description.get());
        }
        Optional<Boolean> active = Optional.ofNullable(this.active);
        if(active.isPresent()) {
            source.setActive(active.get());
        }
        Optional<ComPortType> comPortType = Optional.ofNullable(this.comPortType.id);
        if(comPortType.isPresent()) {
            source.setComPortType(comPortType.get());
        }
        Optional<Integer> numberOfSimultaneousConnections = Optional.ofNullable(this.numberOfSimultaneousConnections);
        if(numberOfSimultaneousConnections.isPresent()) {
            source.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections.get());
        }
    }

    protected B build(B builder, EngineConfigurationService engineConfigurationService) {
        return builder.description(this.description).active(this.active);
    }

    protected abstract ComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService);

}
