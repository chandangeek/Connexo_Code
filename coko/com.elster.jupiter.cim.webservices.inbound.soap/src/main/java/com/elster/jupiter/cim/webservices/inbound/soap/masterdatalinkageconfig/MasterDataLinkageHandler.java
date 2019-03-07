package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.ConfigEventInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;

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
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfService;
    private final com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory faultMessageFactory;
    private final ReplyTypeFactory replyTypeFactory;

    ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent configurationEventNode;
    List<ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint> usagePointNodes;
    List<ch.iec.tc57._2011.masterdatalinkageconfig.Meter> meterNodes;

    private ConfigEventInfo configurationEvent;
    private UsagePointInfo usagePoint;
    private MeterInfo meter;

    private MasterDataLinkageAction currentLinkageAction;

    @Inject
    public MasterDataLinkageHandler(MeteringService meteringService,
            com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory faultMessageFactory,
            MetrologyConfigurationService metrologyConfService, ReplyTypeFactory replyTypeFactory) {
        this.meteringService = meteringService;
        this.metrologyConfService = metrologyConfService;
        this.faultMessageFactory = faultMessageFactory;
        this.replyTypeFactory = replyTypeFactory;
    }

    private boolean shouldCreateResponse() {
        return configurationEvent == null;
    }

    MasterDataLinkageHandler forMessage(MasterDataLinkageConfigRequestMessageType message) throws FaultMessage {
        configurationEventNode = message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent();
        usagePointNodes = message.getPayload().getMasterDataLinkageConfig().getUsagePoint();
        meterNodes = message.getPayload().getMasterDataLinkageConfig().getMeter();
        configurationEvent = null;
        return this;
    }

    public MasterDataLinkageHandler from(ConfigEventInfo configurationEvent, UsagePointInfo usagePoint, MeterInfo meter)
            throws FaultMessage {
        this.configurationEvent = configurationEvent;
        this.usagePoint = usagePoint;
        this.meter = meter;
        return this;
    }

    public MasterDataLinkageConfigResponseMessageType createLinkage() throws FaultMessage {
        currentLinkageAction = MasterDataLinkageAction.CREATE;
        if (shouldCreateResponse()) {
            linkMeterToUsagePoint(transform(meterNodes.get(0)), getMeterRoleForKey(meterNodes.get(0).getRole()),
                    transform(usagePointNodes.get(0)), configurationEventNode.getCreatedDateTime());
            return createSuccessfulResponseWithVerb(HeaderType.Verb.CREATED);
        } else {
            linkMeterToUsagePoint(transform(meter), getMeterRoleForKey(meter.getRole()), transform(usagePoint),
                    configurationEvent.getCreatedDateTime());
            return null;
        }
    }

    public MasterDataLinkageConfigResponseMessageType closeLinkage() throws FaultMessage {
        currentLinkageAction = MasterDataLinkageAction.CLOSE;
        if (shouldCreateResponse()) {
            unlinkMeterFromUsagePoint(transform(meterNodes.get(0)), transform(usagePointNodes.get(0)),
                    configurationEventNode.getEffectiveDateTime());
            return createSuccessfulResponseWithVerb(HeaderType.Verb.CLOSED);
        } else {
            unlinkMeterFromUsagePoint(transform(meter), transform(usagePoint),
                    configurationEvent.getEffectiveDateTime());
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

    private Meter transform(ch.iec.tc57._2011.masterdatalinkageconfig.Meter meterNode) throws FaultMessage {
        return transformMeter(meterNode.getMRID(), () -> meterNode.getNames().get(0).getName());
    }

    private Meter transform(MeterInfo meter) throws FaultMessage {
        return transformMeter(meter.getMrid(), meter::getName);
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

    private MasterDataLinkageConfigResponseMessageType createSuccessfulResponseWithVerb(HeaderType.Verb verb) {
        ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
        ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory linkageMessageFactory = new ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory();

        MasterDataLinkageConfigResponseMessageType response = linkageMessageFactory
                .createMasterDataLinkageConfigResponseMessageType();

        HeaderType header = cimMessageFactory.createHeaderType();
        header.setVerb(verb);
        header.setNoun(ExecuteMasterDataLinkageConfigEndpoint.NOUN);
        response.setHeader(header);

        ReplyType replyType = cimMessageFactory.createReplyType();
        List<ErrorType> warnings = collectWarnings();
        replyType.setResult(warnings.isEmpty() ? ReplyType.Result.OK : ReplyType.Result.PARTIAL);
        replyType.getError().addAll(warnings);
        response.setReply(replyType);

        return response;
    }

    public MasterDataLinkageConfigResponseMessageType createQuickResponseMessage(HeaderType.Verb verb) {
        ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
        ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory linkageMessageFactory = new ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory();

        MasterDataLinkageConfigResponseMessageType responseMessage = linkageMessageFactory
                .createMasterDataLinkageConfigResponseMessageType();

        HeaderType header = cimMessageFactory.createHeaderType();
        header.setVerb(verb);
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
        return warnings;
    }
}
