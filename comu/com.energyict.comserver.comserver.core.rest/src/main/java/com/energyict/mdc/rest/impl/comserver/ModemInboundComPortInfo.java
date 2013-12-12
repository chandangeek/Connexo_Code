package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.rest.impl.TimeDurationInfo;
import com.energyict.mdc.shadow.ports.ModemBasedInboundComPortShadow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModemInboundComPortInfo extends InboundComPortInfo<ModemBasedInboundComPortShadow> {

    private static final BaudRateValueConverter baudrateValueConverter = new BaudRateValueConverter();
    private static final NrOfDataBitsConverter nrOfDataBitsConverter = new NrOfDataBitsConverter();
    private static final NrOfStopBitsConverter nrOfStopBitsConverter = new NrOfStopBitsConverter();

    public static final String MAP_KEY = "modemInitString";

    public ModemInboundComPortInfo() {
        this.comPortType = ComPortType.SERIAL;
    }

    public ModemInboundComPortInfo(ModemBasedInboundComPort comPort) {
        super(comPort);
        this.ringCount = comPort.getRingCount();
        this.maximumNumberOfDialErrors = comPort.getMaximumNumberOfDialErrors();
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
            this.baudrate = baudrateValueConverter.fromServerValue(comPort.getSerialPortConfiguration().getBaudrate());
            this.nrOfDataBits = nrOfDataBitsConverter.fromServerValue(comPort.getSerialPortConfiguration().getNrOfDataBits());
            this.nrOfStopBits = nrOfStopBitsConverter.fromServerValue(comPort.getSerialPortConfiguration().getNrOfStopBits());
            this.flowControl = comPort.getSerialPortConfiguration().getFlowControl();
            this.parity = comPort.getSerialPortConfiguration().getParity();
        }
    }

    @Override
    protected void writeToShadow(ModemBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setRingCount(this.ringCount);
        shadow.setMaximumNumberOfDialErrors(this.maximumNumberOfDialErrors);
        if (this.connectTimeout!=null) {
            shadow.setConnectTimeout(this.connectTimeout.asTimeDuration());
        }
        if (this.delayAfterConnect!=null) {
            shadow.setDelayAfterConnect(this.delayAfterConnect.asTimeDuration());
        }
        if (this.delayBeforeSend!=null) {
            shadow.setDelayBeforeSend(this.delayBeforeSend.asTimeDuration());
        }
        if (this.atCommandTimeout!=null) {
            shadow.setAtCommandTimeout(this.atCommandTimeout.asTimeDuration());
        }
        shadow.setAtCommandTry(this.atCommandTry);
        shadow.setModemInitStrings(fromMaps(MAP_KEY,this.modemInitStrings));
        shadow.setAddressSelector(this.addressSelector);
        shadow.setPostDialCommands(this.postDialCommands);
        shadow.setSerialPortConfiguration(new SerialPortConfiguration(
                this.comPortName,
                baudrateValueConverter.toServerValue(this.baudrate),
                nrOfDataBitsConverter.toServerValue(this.nrOfDataBits),
                nrOfStopBitsConverter.toServerValue(this.nrOfStopBits),
                this.parity,
                this.flowControl));
    }

    @Override
    public ModemBasedInboundComPortShadow asShadow() {
        ModemBasedInboundComPortShadow shadow = new ModemBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
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
