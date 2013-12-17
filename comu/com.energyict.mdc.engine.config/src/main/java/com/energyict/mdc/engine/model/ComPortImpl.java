package com.energyict.mdc.engine.model;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.common.TimeDuration;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ComPortImpl {

    public int id;
    public String direction;
    public String name;
    public String description;
    public boolean active;
    public boolean bound;
//    public ComPortType comPortType;
    public int comServer_id;
    public int numberOfSimultaneousConnections;
    public Date modificationDate;
    public Integer ringCount;
    public Integer maximumNumberOfDialErrors;
    public TimeDuration connectTimeout;
    public TimeDuration delayAfterConnect;
    public TimeDuration delayBeforeSend;
    public TimeDuration atCommandTimeout;
    public BigDecimal atCommandTry;
    public List<Map<String, String>> modemInitStrings;
    public String addressSelector;
    public String postDialCommands;
    public String comPortName;
    public BaudrateValue baudrate;
    public NrOfDataBits nrOfDataBits;
    public NrOfStopBits nrOfStopBits;
    public FlowControl flowControl;
    public Parities parity;
    public Integer comPortPool_id;
    public Integer portNumber;
    public Integer bufferSize;
    public Boolean useHttps;
    public String keyStoreFilePath;
    public String trustStoreFilePath;
    public String keyStorePassword;
    public String trustStorePassword;
    public String contextPath;

    public ComPortImpl() {
    }

}
