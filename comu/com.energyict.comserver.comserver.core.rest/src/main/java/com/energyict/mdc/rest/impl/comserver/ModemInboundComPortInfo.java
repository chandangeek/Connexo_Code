/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.ports.ComPortType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModemInboundComPortInfo extends InboundComPortInfo<ModemBasedInboundComPort, ModemBasedInboundComPort.ModemBasedInboundComPortBuilder> {

    public static final String MODEM_INIT_MAP_KEY = "modemInitString";
    public static final String GLOBAL_MODEM_INIT_MAP_KEY = "globalModemInitString";

    public ModemInboundComPortInfo() {
        this.comPortType = new ComPortTypeInfo(ComPortType.SERIAL);
    }

    public ModemInboundComPortInfo(ModemBasedInboundComPort comPort) {
        super(comPort);
        this.ringCount = comPort.getRingCount();
        this.maximumNumberOfDialErrors = comPort.getMaximumDialErrors();
        this.connectTimeout = TimeDurationInfo.of(comPort.getConnectTimeout());
        this.delayAfterConnect = TimeDurationInfo.of(comPort.getDelayAfterConnect());
        this.delayBeforeSend = TimeDurationInfo.of(comPort.getDelayBeforeSend());
        this.atCommandTimeout = TimeDurationInfo.of(comPort.getAtCommandTimeout());
        this.atCommandTry = comPort.getAtCommandTry();
        this.modemInitStrings = asMap(MODEM_INIT_MAP_KEY, comPort.getModemInitStrings());
        this.globalModemInitStrings = asMap(GLOBAL_MODEM_INIT_MAP_KEY, comPort.getGlobalModemInitStrings());
        this.addressSelector = comPort.getAddressSelector();
        this.postDialCommands = comPort.getPostDialCommands();
        if (comPort.getSerialPortConfiguration()!=null) {
            this.baudrate = comPort.getSerialPortConfiguration().getBaudrate();
            this.nrOfDataBits = comPort.getSerialPortConfiguration().getNrOfDataBits();
            this.nrOfStopBits = comPort.getSerialPortConfiguration().getNrOfStopBits();
            this.flowControl = new FlowControlInfo(comPort.getSerialPortConfiguration().getFlowControl());
            this.parity = new ParitiesInfo(comPort.getSerialPortConfiguration().getParity());
        }
    }

    @Override
    protected void writeTo(ModemBasedInboundComPort source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        super.writeTo(source, engineConfigurationService, resourceHelper);
        Optional<Integer> ringCount = Optional.ofNullable(this.ringCount);
        if(ringCount.isPresent()) {
            source.setRingCount(ringCount.get());
        }
        Optional<Integer> maximumNumberOfDialErrors = Optional.ofNullable(this.maximumNumberOfDialErrors);
        if(maximumNumberOfDialErrors.isPresent()) {
            source.setMaximumDialErrors(maximumNumberOfDialErrors.get());
        }
        Optional<TimeDurationInfo> connectTimeout = Optional.ofNullable(this.connectTimeout);
        if(connectTimeout.isPresent()) {
            source.setConnectTimeout(connectTimeout.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> delayAfterConnect = Optional.ofNullable(this.delayAfterConnect);
        if(delayAfterConnect.isPresent()) {
            source.setDelayAfterConnect(delayAfterConnect.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> delayBeforeSend = Optional.ofNullable(this.delayBeforeSend);
        if(delayBeforeSend.isPresent()) {
            source.setDelayBeforeSend(delayBeforeSend.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> atCommandTimeout = Optional.ofNullable(this.atCommandTimeout);
        if(atCommandTimeout.isPresent()) {
            source.setAtCommandTimeout(atCommandTimeout.get().asTimeDuration());
        }
        Optional<BigDecimal> atCommandTry = Optional.ofNullable(this.atCommandTry);
        if(atCommandTry.isPresent()) {
            source.setAtCommandTry(atCommandTry.get());
        }
        Optional<List<Map<String, String>>> modemInitStrings = Optional.ofNullable(this.modemInitStrings);
        if(modemInitStrings.isPresent()) {
            source.setModemInitStrings(fromMaps(MODEM_INIT_MAP_KEY,modemInitStrings.get()));
        }
        Optional<List<Map<String, String>>> globalModemInitStrings = Optional.ofNullable(this.globalModemInitStrings);
        if(globalModemInitStrings.isPresent()){
            source.setGlobalModemInitStrings(fromMaps(GLOBAL_MODEM_INIT_MAP_KEY, globalModemInitStrings.get()));
        }
        Optional<String> addressSelector = Optional.ofNullable(this.addressSelector);
        if(addressSelector.isPresent()) {
            source.setAddressSelector(addressSelector.get());
        }
        Optional<String> postDialCommands = Optional.ofNullable(this.postDialCommands);
        if(postDialCommands.isPresent()) {
            source.setPostDialCommands(postDialCommands.get());
        }
        Optional<SerialPortConfiguration> serialPortConfiguration = Optional.ofNullable(source.getSerialPortConfiguration());
        SerialPortConfiguration updatedSerialPortConfiguration = serialPortConfiguration.orElse(new SerialPortConfiguration());
        Optional<String> name = Optional.ofNullable(this.name);
        if(name.isPresent()) {
            updatedSerialPortConfiguration.setComPortName(name.get());
        }
        Optional<BaudrateValue> baudrate = Optional.ofNullable(this.baudrate);
        if(baudrate.isPresent()) {
            updatedSerialPortConfiguration.setBaudrate(baudrate.get());
        }
        Optional<NrOfDataBits> nrOfDataBits = Optional.ofNullable(this.nrOfDataBits);
        if(nrOfDataBits.isPresent()) {
            updatedSerialPortConfiguration.setNrOfDataBits(nrOfDataBits.get());
        }
        Optional<NrOfStopBits> nrOfStopBits = Optional.ofNullable(this.nrOfStopBits);
        if(nrOfStopBits.isPresent()) {
            updatedSerialPortConfiguration.setNrOfStopBits(nrOfStopBits.get());
        }
        Optional<ParitiesInfo> parity = Optional.ofNullable(this.parity);
        if(parity.isPresent() && parity.get().id != null ) {
            updatedSerialPortConfiguration.setParity(parity.get().id);
        }
        Optional<FlowControlInfo> flowControl = Optional.ofNullable(this.flowControl);
        if(flowControl.isPresent() && flowControl.get().id != null) {
            updatedSerialPortConfiguration.setFlowControl(flowControl.get().id);
        }
        source.setSerialPortConfiguration(updatedSerialPortConfiguration);
    }

    @Override
    protected ModemBasedInboundComPort.ModemBasedInboundComPortBuilder build(ModemBasedInboundComPort.ModemBasedInboundComPortBuilder builder, EngineConfigurationService engineConfigurationService) {
        super.build(builder, engineConfigurationService);
        Optional<TimeDurationInfo> delayAfterConnect = Optional.ofNullable(this.delayAfterConnect);
        if (delayAfterConnect.isPresent()) {
            builder.delayAfterConnect(delayAfterConnect.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> delayBeforeSend = Optional.ofNullable(this.delayBeforeSend);
        if (delayBeforeSend.isPresent()) {
            builder.delayBeforeSend(delayBeforeSend.get().asTimeDuration());
        }
        builder.atCommandTry(this.atCommandTry);
        builder.atModemInitStrings(fromMaps(MODEM_INIT_MAP_KEY, this.modemInitStrings));
        builder.globalAtModemInitStrings(fromMaps(GLOBAL_MODEM_INIT_MAP_KEY, this.globalModemInitStrings));
        builder.addressSelector(this.addressSelector);
        builder.postDialCommands(this.postDialCommands);
        return super.build(builder, engineConfigurationService);
    }

    @Override
    protected ModemBasedInboundComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService) {
        return build(
                    comServer.newModemBasedInboundComport(
                        this.name,
                        this.ringCount,
                        this.maximumNumberOfDialErrors,
                        this.connectTimeout != null ? this.connectTimeout.asTimeDuration() : null,
                        this.atCommandTimeout != null ? this.atCommandTimeout.asTimeDuration() : null,
                        new SerialPortConfiguration(
                            this.name,
                            this.baudrate,
                            this.nrOfDataBits,
                            this.nrOfStopBits,
                            this.parity != null ? this.parity.id : null,
                            this.flowControl != null ? this.flowControl.id : null)),
                    engineConfigurationService)
                .add();
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
