package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.mdc.rest.impl.TimeDurationInfo;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public abstract class ComPortInfo<T extends ComPort, B extends ComPort.Builder<B,T>> {

    public long id;
    @JsonProperty("direction")
    public String direction;
    @JsonProperty("name")
    public String name;
    @JsonProperty("description")
    public String description;
    @JsonProperty("active")
    public boolean active;
    @JsonProperty("bound")
    public boolean bound;
    @JsonProperty("comPortType")
    @XmlJavaTypeAdapter(ComPortTypeAdapter.class)
    public ComPortType comPortType;
    @JsonProperty("comServer_id")
    public long comServer_id;
    @JsonProperty("numberOfSimultaneousConnections")
    public int numberOfSimultaneousConnections;
    public Date modificationDate;
    @JsonProperty("ringCount")
    public Integer ringCount;
    @JsonProperty("maximumNumberOfDialErrors")
    public Integer maximumNumberOfDialErrors;
    @JsonProperty("connectTimeout")
    public TimeDurationInfo connectTimeout;
    @JsonProperty("delayAfterConnect")
    public TimeDurationInfo delayAfterConnect;
    @JsonProperty("delayBeforeSend")
    public TimeDurationInfo delayBeforeSend;
    @JsonProperty("atCommandTimeout")
    public TimeDurationInfo atCommandTimeout;
    @JsonProperty("atCommandTry")
    public BigDecimal atCommandTry;
    @JsonProperty("modemInitStrings")
    public List<Map<String, String>> modemInitStrings;
    @JsonProperty("addressSelector")
    public String addressSelector;
    @JsonProperty("postDialCommands")
    public String postDialCommands;
    @JsonProperty("baudrate")
    @XmlJavaTypeAdapter(BaudrateAdapter.class)
    public BaudrateValue baudrate;
    @JsonProperty("nrOfDataBits")
    @XmlJavaTypeAdapter(NrOfDataBitsAdapter.class)
    public NrOfDataBits nrOfDataBits;
    @JsonProperty("nrOfStopBits")
    @XmlJavaTypeAdapter(NrOfStopBitsAdapter.class)
    public NrOfStopBits nrOfStopBits;
    @JsonProperty("flowControl")
    @XmlJavaTypeAdapter(value = FlowControlAdapter.class)
    public FlowControl flowControl;
    @JsonProperty("parity")
    @XmlJavaTypeAdapter(ParitiesAdapter.class)
    public Parities parity;
    @JsonProperty("comPortPool_id")
    public Long comPortPool_id;
    @JsonProperty("portNumber")
    public Integer portNumber;
    @JsonProperty("bufferSize")
    public Integer bufferSize;
    @JsonProperty("useHttps")
    public Boolean useHttps;
    @JsonProperty("keyStoreFilePath")
    public String keyStoreFilePath;
    @JsonProperty("trustStoreFilePath")
    public String trustStoreFilePath;
    @JsonProperty("keyStorePassword")
    public String keyStorePassword;
    @JsonProperty("trustStorePassword")
    public String trustStorePassword;
    @JsonProperty("contextPath")
    public String contextPath;

    public ComPortInfo() {
    }

    public ComPortInfo(ComPort comPort) {
        this.id = comPort.getId();
        this.name = comPort.getName();
        this.description = comPort.getDescription();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comServer_id = comPort.getComServer()!=null?comPort.getComServer().getId():0;
        this.comPortType = comPort.getComPortType();
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.modificationDate = comPort.getModificationDate();
    }

    protected void writeTo(T source,EngineModelService engineModelService) {
        source.setName(name);
        source.setDescription(description);
        source.setActive(active);
        source.setComPortType(this.comPortType);
    }

    protected B build(B builder, EngineModelService engineModelService) {
        return builder.description(description).active(active);
    }

    protected abstract ComPort createNew(ComServer comServer, EngineModelService engineModelService);

}
