package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.*;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import com.google.common.base.Optional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModemInboundComPortInfo extends InboundComPortInfo<ModemBasedInboundComPort, ModemBasedInboundComPort.ModemBasedInboundComPortBuilder> {

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
        Optional<Integer> ringCount = Optional.fromNullable(this.ringCount);
        if(ringCount.isPresent()) {
            source.setRingCount(ringCount.get());
        }
        Optional<Integer> maximumNumberOfDialErrors = Optional.fromNullable(this.maximumNumberOfDialErrors);
        if(maximumNumberOfDialErrors.isPresent()) {
            source.setMaximumDialErrors(maximumNumberOfDialErrors.get());
        }
        Optional<TimeDurationInfo> connectTimeout = Optional.fromNullable(this.connectTimeout);
        if(connectTimeout.isPresent()) {
            source.setConnectTimeout(connectTimeout.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> delayAfterConnect = Optional.fromNullable(this.delayAfterConnect);
        if(delayAfterConnect.isPresent()) {
            source.setDelayAfterConnect(delayAfterConnect.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> delayBeforeSend = Optional.fromNullable(this.delayBeforeSend);
        if(delayBeforeSend.isPresent()) {
            source.setDelayBeforeSend(delayBeforeSend.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> atCommandTimeout = Optional.fromNullable(this.atCommandTimeout);
        if(atCommandTimeout.isPresent()) {
            source.setAtCommandTimeout(atCommandTimeout.get().asTimeDuration());
        }
        Optional<BigDecimal> atCommandTry = Optional.fromNullable(this.atCommandTry);
        if(atCommandTry.isPresent()) {
            source.setAtCommandTry(atCommandTry.get());
        }
        Optional<List<Map<String, String>>> modemInitStrings = Optional.fromNullable(this.modemInitStrings);
        if(modemInitStrings.isPresent()) {
            source.setModemInitStrings(fromMaps(MAP_KEY,modemInitStrings.get()));
        }
        Optional<String> addressSelector = Optional.fromNullable(this.addressSelector);
        if(addressSelector.isPresent()) {
            source.setAddressSelector(addressSelector.get());
        }
        Optional<String> postDialCommands = Optional.fromNullable(this.postDialCommands);
        if(postDialCommands.isPresent()) {
            source.setPostDialCommands(postDialCommands.get());
        }
        Optional<SerialPortConfiguration> serialPortConfiguration = Optional.fromNullable(source.getSerialPortConfiguration());
        SerialPortConfiguration updatedSerialPortConfiguration = serialPortConfiguration.or(new SerialPortConfiguration());
        Optional<String> name = Optional.fromNullable(this.name);
        if(name.isPresent()) {
            updatedSerialPortConfiguration.setComPortName(name.get());
        }
        Optional<BaudrateValue> baudrate = Optional.fromNullable(this.baudrate);
        if(baudrate.isPresent()) {
            updatedSerialPortConfiguration.setBaudrate(baudrate.get());
        }
        Optional<NrOfDataBits> nrOfDataBits = Optional.fromNullable(this.nrOfDataBits);
        if(nrOfDataBits.isPresent()) {
            updatedSerialPortConfiguration.setNrOfDataBits(nrOfDataBits.get());
        }
        Optional<NrOfStopBits> nrOfStopBits = Optional.fromNullable(this.nrOfStopBits);
        if(nrOfStopBits.isPresent()) {
            updatedSerialPortConfiguration.setNrOfStopBits(nrOfStopBits.get());
        }
        Optional<Parities> parity = Optional.fromNullable(this.parity);
        if(parity.isPresent()) {
            updatedSerialPortConfiguration.setParity(parity.get());
        }
        Optional<FlowControl> flowControl = Optional.fromNullable(this.flowControl);
        if(flowControl.isPresent()) {
            updatedSerialPortConfiguration.setFlowControl(flowControl.get());
        }
        source.setSerialPortConfiguration(updatedSerialPortConfiguration);
    }

    @Override
    protected ModemBasedInboundComPort.ModemBasedInboundComPortBuilder build(ModemBasedInboundComPort.ModemBasedInboundComPortBuilder builder, EngineModelService engineModelService) {
        super.build(builder, engineModelService);
        Optional<TimeDurationInfo> delayAfterConnect = Optional.fromNullable(this.delayAfterConnect);
        if (delayAfterConnect.isPresent()) {
            builder.delayAfterConnect(delayAfterConnect.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> delayBeforeSend = Optional.fromNullable(this.delayBeforeSend);
        if (delayBeforeSend.isPresent()) {
            builder.delayBeforeSend(delayBeforeSend.get().asTimeDuration());
        }
        builder.atCommandTry(this.atCommandTry);
        builder.atModemInitStrings(fromMaps(MAP_KEY, this.modemInitStrings));
        builder.addressSelector(this.addressSelector);
        builder.postDialCommands(this.postDialCommands);
        return super.build(builder, engineModelService);
    }

    @Override
    protected ModemBasedInboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return build(comServer.newModemBasedInboundComport(
                this.name,
                this.ringCount,
                this.maximumNumberOfDialErrors,
                this.connectTimeout!=null?this.connectTimeout.asTimeDuration():null,
                this.atCommandTimeout!=null?this.atCommandTimeout.asTimeDuration():null,
                new SerialPortConfiguration(
                    this.name,
                    this.baudrate,
                    this.nrOfDataBits,
                    this.nrOfStopBits,
                    this.parity,
                    this.flowControl)), engineModelService).add();
    }

    private List<Map<String, String>> asMap(String key, List<String> strings) {
        List<Map<String, String>> maps = new ArrayList<>();
        for (String string : strings) {
            Map<String, String> map = new HashMap<>();
            map.put(key, string);
            maps.add(map);
        }
        return maps;
    }

    private List<String> fromMaps(String key, List<Map<String, String>> maps) {
        List<String> strings = new ArrayList<>();
        if (maps!=null) {
            for (Map<String, String> map : maps) {
                strings.add(map.get(key));
            }
        }
        return strings;
    }

}
