package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonTypeIdResolver;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TcpInboundComPortInfo.class, name = "inbound_TCP"),
        @JsonSubTypes.Type(value = UdpInboundComPortInfo.class, name = "inbound_UDP"),
        @JsonSubTypes.Type(value = ModemInboundComPortInfo.class, name = "inbound_SERIAL"),
        @JsonSubTypes.Type(value = ServletInboundComPortInfo.class, name = "inbound_SERVLET"),
        @JsonSubTypes.Type(value = TcpOutboundComPortInfo.class, name = "outbound_TCP"),
        @JsonSubTypes.Type(value = UdpOutboundComPortInfo.class, name = "outbound_UDP"),
        @JsonSubTypes.Type(value = ModemOutboundComPortInfo.class, name = "outbound_SERIAL") })
public abstract class ComPortInfo<T extends ComPort, B extends ComPort.Builder<B,T>> {

    public long id;
    public String direction;
    public String name;
    public String description;
    public Boolean active = Boolean.FALSE;
    public Boolean bound;
    @XmlJavaTypeAdapter(ComPortTypeAdapter.class)
    public ComPortType comPortType;
    public Long comServer_id;
    public String comServerName;
    public Integer numberOfSimultaneousConnections = Integer.valueOf(0);
    public Date modificationDate;
    public Integer ringCount;
    public Integer maximumNumberOfDialErrors;
    public TimeDurationInfo connectTimeout;
    public TimeDurationInfo delayAfterConnect;
    public TimeDurationInfo delayBeforeSend;
    public TimeDurationInfo atCommandTimeout;
    public BigDecimal atCommandTry;
    public List<Map<String, String>> modemInitStrings;
    public String addressSelector;
    public String postDialCommands;
    @XmlJavaTypeAdapter(BaudrateAdapter.class)
    public BaudrateValue baudrate;
    @XmlJavaTypeAdapter(NrOfDataBitsAdapter.class)
    public NrOfDataBits nrOfDataBits;
    @XmlJavaTypeAdapter(NrOfStopBitsAdapter.class)
    public NrOfStopBits nrOfStopBits;
    @XmlJavaTypeAdapter(value = FlowControlAdapter.class)
    public FlowControl flowControl;
    @XmlJavaTypeAdapter(ParitiesAdapter.class)
    public Parities parity;
    public Integer portNumber;
    public Integer bufferSize;
    public Boolean useHttps;
    public String keyStoreFilePath;
    public String trustStoreFilePath;
    public String keyStorePassword;
    public String trustStorePassword;
    public String contextPath;

    public ComPortInfo() {
    }

    public ComPortInfo(ComPort comPort) {
        this.id = comPort.getId();
        this.name = comPort.getName();
        this.description = comPort.getDescription();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comServer_id = comPort.getComServer()!=null?comPort.getComServer().getId():0L;
        this.comServerName = comPort.getComServer() != null ? comPort.getComServer().getName():null;
        this.comPortType = comPort.getComPortType();
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.modificationDate = comPort.getModificationDate();
    }

    protected void writeTo(T source,EngineModelService engineModelService) {
        Optional<String> name = Optional.fromNullable(this.name);
        if(name.isPresent()) {
            source.setName(name.get());
        }
        Optional<String> description = Optional.fromNullable(this.description);
        if(description.isPresent()) {
            source.setDescription(description.get());
        }
        Optional<Boolean> active = Optional.fromNullable(this.active);
        if(active.isPresent()) {
            source.setActive(active.get());
        }
        Optional<ComPortType> comPortType = Optional.fromNullable(this.comPortType);
        if(comPortType.isPresent()) {
            source.setComPortType(comPortType.get());
        }
        Optional<Integer> numberOfSimultaneousConnections = Optional.fromNullable(this.numberOfSimultaneousConnections);
        if(numberOfSimultaneousConnections.isPresent()) {
            source.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections.get());
        }
    }

    protected B build(B builder, EngineModelService engineModelService) {
        return builder.description(this.description).active(this.active);
    }

    protected abstract ComPort createNew(ComServer comServer, EngineModelService engineModelService);

}
