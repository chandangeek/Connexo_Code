package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.impl.ServerModemBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.rest.impl.TimeDurationInfo;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModemInboundComPortInfo extends InboundComPortInfo<ModemBasedInboundComPort> {

    public static final String MAP_KEY = "modemInitString";

    public ModemInboundComPortInfo() {
        this.comPortType = ComPortType.SERIAL;
    }

    public ModemInboundComPortInfo(ModemBasedInboundComPort comPort) {
        super(comPort);
        this.ringCount = comPort.getRingCount();
        this.maximumNumberOfDialErrors = comPort.getMaximumDialErrors();
        this.connectTimeout = comPort.getConnectTimeout()!=null?new TimeDurationInfo(comPort.getConnectTimeout()):null;
        this.delayAfterConnect = comPort.getDelayAfterConnect()!=null?new TimeDurationInfo(comPort.getDelayAfterConnect()):null;
        this.delayBeforeSend = comPort.getDelayBeforeSend()!=null?new TimeDurationInfo(comPort.getDelayBeforeSend()):null;
        this.atCommandTimeout = comPort.getAtCommandTimeout()!=null?new TimeDurationInfo(comPort.getAtCommandTimeout()):null;
        this.atCommandTry = comPort.getAtCommandTry();
        this.modemInitStrings = asMap(MAP_KEY, comPort.getModemInitStrings());
        this.addressSelector = comPort.getAddressSelector();
        this.postDialCommands = comPort.getPostDialCommands();
        if (comPort.getSerialPortConfiguration()!=null) {
            this.comPortName = comPort.getSerialPortConfiguration().getComPortName();
            this.baudrate = comPort.getSerialPortConfiguration().getBaudrate();
            this.nrOfDataBits = comPort.getSerialPortConfiguration().getNrOfDataBits();
            this.nrOfStopBits = comPort.getSerialPortConfiguration().getNrOfStopBits();
            this.flowControl = comPort.getSerialPortConfiguration().getFlowControl();
            this.parity = comPort.getSerialPortConfiguration().getParity();
        }
    }

    @Override
    protected void writeTo(ModemBasedInboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        source.setRingCount(this.ringCount);
        source.setMaximumDialErrors(this.maximumNumberOfDialErrors);
        if (this.connectTimeout!=null) {
            source.setConnectTimeout(this.connectTimeout.asTimeDuration());
        }
        if (this.delayAfterConnect!=null) {
            source.setDelayAfterConnect(this.delayAfterConnect.asTimeDuration());
        }
        if (this.delayBeforeSend!=null) {
            source.setDelayBeforeSend(this.delayBeforeSend.asTimeDuration());
        }
        if (this.atCommandTimeout!=null) {
            source.setAtCommandTimeout(this.atCommandTimeout.asTimeDuration());
        }
        source.setAtCommandTry(this.atCommandTry);
        source.setModemInitStrings(fromMaps(MAP_KEY,this.modemInitStrings));
        source.setAddressSelector(this.addressSelector);
        source.setPostDialCommands(this.postDialCommands);
        source.setSerialPortConfiguration(new SerialPortConfiguration(
                this.comPortName,
                this.baudrate,
                this.nrOfDataBits,
                this.nrOfStopBits,
                this.parity,
                this.flowControl));
    }

    @Override
    protected ServerModemBasedInboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return engineModelService.newModemBasedInbound(comServer);
    }

    private List<Map<String, String>> asMap(String key, List<String> strings) {
        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
        for (String string : strings) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(key, string);
            maps.add(map);
        }
        return maps;
    }

    private List<String> fromMaps(String key, List<Map<String, String>> maps) {
        List<String> strings = new ArrayList<String>();
        if (maps!=null) {
            for (Map<String, String> map : maps) {
                strings.add(map.get(key));
            }
        }
        return strings;
    }

}
