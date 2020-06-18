package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.ConfigEventInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.EndDeviceInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import org.apache.cxf.common.util.StringUtils;

import javax.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MasterDataLinkageHandler {
    private static final String END_DEVICE_AND_USAGE_POINT = "End device and Usage point nodes";
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final MetrologyConfigurationService metrologyConfService;
    private final com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory faultMessageFactory;
    private final ReplyTypeFactory replyTypeFactory;
    private final TopologyService topologyService;

    ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent configurationEventNode;
    List<ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice> endDeviceNodes;
    List<ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint> usagePointNodes;
    List<ch.iec.tc57._2011.masterdatalinkageconfig.Meter> meterNodes;

    private ConfigEventInfo configurationEvent;
    private EndDeviceInfo endDevice;
    private UsagePointInfo usagePoint;
    private MeterInfo meter;
    private String correlationId;

    private MasterDataLinkageAction currentLinkageAction;

    @Inject
    public MasterDataLinkageHandler(MeteringService meteringService, DeviceService deviceService,
                                    com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory faultMessageFactory,
                                    MetrologyConfigurationService metrologyConfService, ReplyTypeFactory replyTypeFactory,
                                    TopologyService topologyService) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.metrologyConfService = metrologyConfService;
        this.faultMessageFactory = faultMessageFactory;
        this.replyTypeFactory = replyTypeFactory;
        this.topologyService = topologyService;
    }

    private boolean shouldCreateResponse() {
        return configurationEvent == null;
    }

    MasterDataLinkageHandler forMessage(MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        configurationEventNode = message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent();
        endDeviceNodes = message.getPayload().getMasterDataLinkageConfig().getEndDevice();
        usagePointNodes = message.getPayload().getMasterDataLinkageConfig().getUsagePoint();
        meterNodes = message.getPayload().getMasterDataLinkageConfig().getMeter();
        configurationEvent = null;
        correlationId = message.getHeader().getCorrelationID();
        return this;
    }

    public MasterDataLinkageHandler from(ConfigEventInfo configurationEvent, EndDeviceInfo endDevice, UsagePointInfo usagePoint, MeterInfo meter)
            throws FaultMessage {
        this.configurationEvent = configurationEvent;
        this.endDevice = endDevice;
        this.usagePoint = usagePoint;
        this.meter = meter;
        return this;
    }

    public MasterDataLinkageConfigResponseMessageType createLinkage() throws FaultMessage {
        currentLinkageAction = MasterDataLinkageAction.CREATE;
        if (shouldCreateResponse()) {
            if (usagePointNodes.isEmpty() && endDeviceNodes.isEmpty()){
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICE_AND_USAGE_POINT);
            }
            if (!usagePointNodes.isEmpty()) {
                linkMeterToUsagePoint(transform(meterNodes.get(0)), getMeterRoleForKey(meterNodes.get(0).getRole()),
                        transform(usagePointNodes.get(0)), configurationEventNode.getCreatedDateTime());
            }
            if (!endDeviceNodes.isEmpty()) {
                linkGatewayToDevice(transformToDevice(meterNodes.get(0)), transform(endDeviceNodes.get(0)));
            }
            return createSuccessfulResponseWithVerb(HeaderType.Verb.CREATED, correlationId);
        } else {
            if (usagePoint == null && endDevice == null) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICE_AND_USAGE_POINT);
            }
            if (usagePoint != null) {
                linkMeterToUsagePoint(transform(meter), getMeterRoleForKey(meter.getRole()), transform(usagePoint),
                        configurationEvent.getCreatedDateTime());
            }
            if (endDevice != null) {
                linkGatewayToDevice(transformToDevice(meter), transform(endDevice));
            }
            return null;
        }
    }

    public MasterDataLinkageConfigResponseMessageType closeLinkage() throws FaultMessage {
        currentLinkageAction = MasterDataLinkageAction.CLOSE;
        if (shouldCreateResponse()) {
            if (usagePointNodes.isEmpty() && endDeviceNodes.isEmpty()) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICE_AND_USAGE_POINT);
            }
            if (!usagePointNodes.isEmpty()) {
                unlinkMeterFromUsagePoint(transform(meterNodes.get(0)), transform(usagePointNodes.get(0)),
                        configurationEventNode.getEffectiveDateTime());
            }
            if (!endDeviceNodes.isEmpty()){
                unlinkDeviceFromGateway(transformToDevice(meterNodes.get(0)), transform(endDeviceNodes.get(0)));
            }
            return createSuccessfulResponseWithVerb(HeaderType.Verb.CLOSED, correlationId);
        } else {
            if (usagePoint == null && endDevice == null) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICE_AND_USAGE_POINT);
            }
            if (usagePoint != null) {
                unlinkMeterFromUsagePoint(transform(meter), transform(usagePoint),
                        configurationEvent.getEffectiveDateTime());
            }
            if (endDevice != null){
                unlinkDeviceFromGateway(transformToDevice(meter), transform(endDevice));
            }
            return null;
        }
    }

    private void linkMeterToUsagePoint(Meter meter, MeterRole role, UsagePoint usagePoint, Instant instant)
            throws FaultMessage {
        Optional<UsagePoint> existingUsagePoint = meter.getUsagePoint(instant);
        if (existingUsagePoint.isPresent() && existingUsagePoint.get().equals(usagePoint)) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.SAME_USAGE_POINT_ALREADY_LINKED, meter.getName(), usagePoint.getName(),
                    XsdDateTimeConverter.marshalDateTime(instant));
        }
        usagePoint.linkMeters().activate(instant, meter, role).complete();
    }

    private void unlinkMeterFromUsagePoint(Meter meter, UsagePoint usagePoint, Instant instant) throws FaultMessage {
        for (MeterActivation activation : usagePoint.getMeterActivations(instant)) {
            Optional<Meter> linkedMeter = activation.getMeter();
            if (linkedMeter.isPresent() && linkedMeter.get().equals(meter)) {
                usagePoint.linkMeters().clear(instant, activation.getMeterRole().get()).complete();
                return;
            }
        }
        throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                MessageSeeds.METER_AND_USAGE_POINT_NOT_LINKED, meter.getName(), usagePoint.getName(),
                XsdDateTimeConverter.marshalDateTime(instant));
    }

    private void linkGatewayToDevice(Device gateway, Device slave)
            throws FaultMessage {
        if (!gateway.getDeviceConfiguration().canActAsGateway()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.NOT_SUPPORTED_MASTER, gateway.getName(), gateway.getSerialNumber());
        }
        if (slave.equals(gateway)) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.CAN_NOT_BE_GATEWAY_TO_ITSELF, slave.getName(), slave.getSerialNumber());
        }
        if (slave.getDeviceConfiguration().isDirectlyAddressable()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.NOT_SUPPORTED_SLAVE, slave.getName(), slave.getSerialNumber());
        }
        Optional<Device> currentGateway = topologyService.getPhysicalGateway(slave);
        if (currentGateway.isPresent()) {
            if (currentGateway.get().equals(gateway)) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.METER_ALREADY_LINKED_TO_END_DEVICE, slave.getName(), slave.getSerialNumber(), gateway.getName(), gateway.getSerialNumber());
            } else {
                topologyService.clearPhysicalGateway(slave);
            }
        }
        topologyService.setPhysicalGateway(slave, gateway);
    }

    private void unlinkDeviceFromGateway(Device gateway, Device slave) throws FaultMessage {
        if (slave.equals(gateway)) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.CAN_NOT_UNLINK_ITSELF, slave.getName(), slave.getSerialNumber());
        }
        if (slave.getDeviceConfiguration().isDirectlyAddressable()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.NOT_SUPPORTED_SLAVE, slave.getName(), slave.getSerialNumber());
        }
        Optional<Device> currentGateway = topologyService.getPhysicalGateway(slave);
        if (currentGateway.isPresent()) {
            if (!currentGateway.get().equals(gateway)) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.METER_ALREADY_LINKED_TO_END_DEVICE, slave.getName(), slave.getSerialNumber(), currentGateway.get().getName(), currentGateway.get().getSerialNumber());
            } else {
                topologyService.clearPhysicalGateway(slave);
            }
        } else {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.END_DEVICE_IS_NOT_LINKED, slave.getName(), slave.getSerialNumber(), gateway.getName(), gateway.getSerialNumber());
        }
    }

    private Meter transform(ch.iec.tc57._2011.masterdatalinkageconfig.Meter meterNode) throws FaultMessage {
        return transformMeter(meterNode.getMRID(), () -> meterNode.getNames().get(0).getName());
    }

    private Meter transform(MeterInfo meter) throws FaultMessage {
        return transformMeter(meter.getMrid(), meter::getName);
    }

    private Device transformToDevice(ch.iec.tc57._2011.masterdatalinkageconfig.Meter meterNode) throws FaultMessage {
        return transformToDevice(meterNode.getMRID(), () -> meterNode.getNames().get(0).getName());
    }

    private Device transformToDevice(MeterInfo meter) throws FaultMessage {
        return transformToDevice(meter.getMrid(), meter::getName);
    }

    private Meter transformMeter(String mrid, Supplier<String> nameSupplier) throws FaultMessage {
        if (mrid != null) {
            return meteringService.findMeterByMRID(mrid)
                    .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                            MessageSeeds.NO_METER_WITH_MRID, mrid));
        }
        String name = nameSupplier.get();
        return meteringService.findMeterByName(name)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                        MessageSeeds.NO_METER_WITH_NAME, name));
    }

    private Device transformToDevice(String mrid, Supplier<String> nameSupplier) throws FaultMessage {
        if (mrid != null) {
            return deviceService.findDeviceByMrid(mrid)
                    .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                            MessageSeeds.NO_METER_WITH_MRID, mrid));
        }
        String name = nameSupplier.get();
        return deviceService.findDeviceByName(name)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                        MessageSeeds.NO_METER_WITH_NAME, name));
    }

    private Device transform(ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice endDeviceNode)
            throws FaultMessage {
        return transformEndDevice(endDeviceNode.getMRID(), () -> endDeviceNode.getNames().get(0).getName());
    }

    private Device transform(EndDeviceInfo endDevice) throws FaultMessage {
        return transformEndDevice(endDevice.getMrid(), endDevice::getName);
    }

    private Device transformEndDevice(String mrid, Supplier<String> nameSupplier) throws FaultMessage {
        if (mrid != null) {
            return deviceService.findDeviceByMrid(mrid)
                    .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                            MessageSeeds.NO_END_DEVICE_WITH_MRID, mrid));
        }
        String name = nameSupplier.get();
        return deviceService.findDeviceByName(name)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                        MessageSeeds.NO_END_DEVICE_WITH_NAME, name));
    }

    private UsagePoint transform(ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint usagePointNode)
            throws FaultMessage {
        return transformUsagePoint(usagePointNode.getMRID(), () -> usagePointNode.getNames().get(0).getName());
    }

    private UsagePoint transform(UsagePointInfo usagePoint) throws FaultMessage {
        return transformUsagePoint(usagePoint.getMrid(), usagePoint::getName);
    }

    private UsagePoint transformUsagePoint(String mrid, Supplier<String> nameSupplier) throws FaultMessage {
        if (mrid != null) {
            return meteringService.findUsagePointByMRID(mrid)
                    .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                            MessageSeeds.NO_USAGE_POINT_WITH_MRID, mrid));
        }
        String name = nameSupplier.get();
        return meteringService.findUsagePointByName(name)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                        MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
    }

    private MeterRole getMeterRoleForKey(String key) throws FaultMessage {
        if (StringUtils.isEmpty(key)) {
            return metrologyConfService.findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        }
        return metrologyConfService.findMeterRole(key)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                        MessageSeeds.NO_METER_ROLE_WITH_KEY, key));
    }

    private MasterDataLinkageConfigResponseMessageType createSuccessfulResponseWithVerb(HeaderType.Verb verb, String correlationId) {
        ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
        ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory linkageMessageFactory = new ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory();

        MasterDataLinkageConfigResponseMessageType response = linkageMessageFactory
                .createMasterDataLinkageConfigResponseMessageType();

        HeaderType header = cimMessageFactory.createHeaderType();
        header.setVerb(verb);
        header.setCorrelationID(correlationId);
        header.setNoun(ExecuteMasterDataLinkageConfigEndpoint.NOUN);
        response.setHeader(header);

        ReplyType replyType = cimMessageFactory.createReplyType();
        List<ErrorType> warnings = collectWarnings();
        replyType.setResult(warnings.isEmpty() ? ReplyType.Result.OK : ReplyType.Result.PARTIAL);
        replyType.getError().addAll(warnings);
        response.setReply(replyType);

        return response;
    }

    public MasterDataLinkageConfigResponseMessageType createQuickResponseMessage(HeaderType.Verb verb, String correlationId) {
        ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
        ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory linkageMessageFactory = new ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory();

        MasterDataLinkageConfigResponseMessageType responseMessage = linkageMessageFactory
                .createMasterDataLinkageConfigResponseMessageType();

        HeaderType header = cimMessageFactory.createHeaderType();
        header.setVerb(verb);
        header.setCorrelationID(correlationId);
        header.setNoun(ExecuteMasterDataLinkageConfigEndpoint.NOUN);
        responseMessage.setHeader(header);

        responseMessage.setReply(replyTypeFactory.okReplyType());

        return responseMessage;
    }

    private List<ErrorType> collectWarnings() {
        List<ErrorType> warnings = new ArrayList<>();
        if (meterNodes.size() > 1) {
            warnings.add(replyTypeFactory.errorType(MessageSeeds.UNSUPPORTED_BULK_OPERATION,
                    MasterDataLinkageMessageValidator.METER_LIST_ELEMENT));
        }
        if (usagePointNodes.size() > 1) {
            warnings.add(replyTypeFactory.errorType(MessageSeeds.UNSUPPORTED_BULK_OPERATION,
                    MasterDataLinkageMessageValidator.USAGE_POINT_LIST_ELEMENT));
        }
        if (endDeviceNodes.size() > 1) {
            warnings.add(replyTypeFactory.errorType(MessageSeeds.UNSUPPORTED_BULK_OPERATION,
                    MasterDataLinkageMessageValidator.END_DEVICE_LIST_ELEMENT));
        }
        return warnings;
    }
}
