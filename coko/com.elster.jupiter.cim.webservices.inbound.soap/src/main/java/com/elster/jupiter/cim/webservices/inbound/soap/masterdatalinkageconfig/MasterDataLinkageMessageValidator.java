package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.Name;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import org.apache.cxf.common.util.StringUtils;

import javax.inject.Inject;

public class MasterDataLinkageMessageValidator {
    static final String PAYLOAD_ELEMENT = "Payload";
    static final String CONFIGURATION_EVENT_ELEMENT = "MasterDataLinkageConfig.ConfigurationEvent";
    static final String METER_LIST_ELEMENT = "MasterDataLinkageConfig.Meter";
    static final String USAGE_POINT_LIST_ELEMENT = "MasterDataLinkageConfig.UsagePoint";
    static final String METER_ELEMENT = "MasterDataLinkageConfig.Meter[0]";
    static final String USAGE_POINT_ELEMENT = "MasterDataLinkageConfig.UsagePoint[0]";
    static final String CREATED_DATE_TIME_ATTRIBUTE = "MasterDataLinkageConfig.ConfigurationEvent.createdDateTime";
    static final String EFFECTIVE_DATE_TIME_ATTRIBUTE = "MasterDataLinkageConfig.ConfigurationEvent.effectiveDateTime";

    private final MasterDataLinkageFaultMessageFactory faultMessageFactory;
    private MasterDataLinkageAction currentLinkageAction;

    @Inject
    public MasterDataLinkageMessageValidator(MasterDataLinkageFaultMessageFactory faultMessageFactory) {
        this.faultMessageFactory = faultMessageFactory;
    }

    void validate(MasterDataLinkageConfigRequestMessageType message, MasterDataLinkageAction linkageAction)
            throws FaultMessage {
        currentLinkageAction = linkageAction;
        if (message.getPayload() == null) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.MISSING_ELEMENT, PAYLOAD_ELEMENT);
        }
        if (message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent() == null) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                    MessageSeeds.MISSING_ELEMENT, CONFIGURATION_EVENT_ELEMENT);
        }
        if (message.getPayload().getMasterDataLinkageConfig().getMeter().isEmpty()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction, MessageSeeds.EMPTY_LIST,
                    METER_LIST_ELEMENT);
        }
        if (message.getPayload().getMasterDataLinkageConfig().getUsagePoint().isEmpty()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction, MessageSeeds.EMPTY_LIST,
                    USAGE_POINT_LIST_ELEMENT);
        }
        if (currentLinkageAction == MasterDataLinkageAction.CREATE) {
            if (message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent()
                    .getCreatedDateTime() == null) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.MISSING_ELEMENT, CREATED_DATE_TIME_ATTRIBUTE);
            }
        }
        if (currentLinkageAction == MasterDataLinkageAction.CLOSE) {
            if (message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent()
                    .getEffectiveDateTime() == null) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(currentLinkageAction,
                        MessageSeeds.MISSING_ELEMENT, EFFECTIVE_DATE_TIME_ATTRIBUTE);
            }
        }
        for (Meter meter : message.getPayload().getMasterDataLinkageConfig().getMeter()) {
            validateIdentificationAttributes(meter);
        }
        for (UsagePoint usagePoint : message.getPayload().getMasterDataLinkageConfig().getUsagePoint()) {
            validateIdentificationAttributes(usagePoint);
        }

    }

    private void validateIdentificationAttributes(Meter meter) throws FaultMessage {
        if (!StringUtils.isEmpty(meter.getMRID())) {
            return;
        }
        meter.getNames().stream().findFirst().map(Name::getName)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, METER_ELEMENT));
    }

    private void validateIdentificationAttributes(UsagePoint usagePoint) throws FaultMessage {
        if (!StringUtils.isEmpty(usagePoint.getMRID())) {
            return;
        }
        usagePoint.getNames().stream().findFirst().map(Name::getName)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(currentLinkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, USAGE_POINT_ELEMENT));
    }
}
