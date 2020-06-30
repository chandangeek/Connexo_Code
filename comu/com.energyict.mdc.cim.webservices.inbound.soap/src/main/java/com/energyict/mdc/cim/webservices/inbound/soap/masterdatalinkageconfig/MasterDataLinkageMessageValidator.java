package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice;
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
    static final String END_DEVICE_LIST_ELEMENT = "MasterDataLinkageConfig.EndDevice";

    private final MasterDataLinkageFaultMessageFactory faultMessageFactory;

    @Inject
    public MasterDataLinkageMessageValidator(MasterDataLinkageFaultMessageFactory faultMessageFactory) {
        this.faultMessageFactory = faultMessageFactory;
    }

    void validate(MasterDataLinkageConfigRequestMessageType message, MasterDataLinkageAction linkageAction)
            throws FaultMessage {
        if (message.getPayload() == null) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(linkageAction, MessageSeeds.MISSING_ELEMENT,
                    PAYLOAD_ELEMENT);
        }
        if (message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent() == null && !message.getPayload().getMasterDataLinkageConfig().getUsagePoint().isEmpty()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(linkageAction, MessageSeeds.MISSING_ELEMENT,
                    CONFIGURATION_EVENT_ELEMENT);
        }
        if (message.getPayload().getMasterDataLinkageConfig().getMeter().isEmpty()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(linkageAction, MessageSeeds.EMPTY_LIST,
                    METER_LIST_ELEMENT);
        }
        if (message.getPayload().getMasterDataLinkageConfig().getUsagePoint().isEmpty() && message.getPayload().getMasterDataLinkageConfig().getEndDevice().isEmpty()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(linkageAction, MessageSeeds.EMPTY_USAGE_POINT_OR_END_DEVICE_LIST);
        }
        if (linkageAction == MasterDataLinkageAction.CREATE) {
            if ((message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent() == null
                    || message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent().getCreatedDateTime() == null)
                    && !message.getPayload().getMasterDataLinkageConfig().getUsagePoint().isEmpty()) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(linkageAction,
                        MessageSeeds.MISSING_ELEMENT, CREATED_DATE_TIME_ATTRIBUTE);
            }
        }
        if (linkageAction == MasterDataLinkageAction.CLOSE) {
            if ((message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent() == null
                    || message.getPayload().getMasterDataLinkageConfig().getConfigurationEvent().getEffectiveDateTime() == null)
                    && !message.getPayload().getMasterDataLinkageConfig().getUsagePoint().isEmpty()) {
                throw faultMessageFactory.createMasterDataLinkageFaultMessage(linkageAction,
                        MessageSeeds.MISSING_ELEMENT, EFFECTIVE_DATE_TIME_ATTRIBUTE);
            }
        }
        for (Meter meter : message.getPayload().getMasterDataLinkageConfig().getMeter()) {
            validateIdentificationAttributes(meter, linkageAction);
        }
        for (UsagePoint usagePoint : message.getPayload().getMasterDataLinkageConfig().getUsagePoint()) {
            validateIdentificationAttributes(usagePoint, linkageAction);
        }
        for (EndDevice endDevice : message.getPayload().getMasterDataLinkageConfig().getEndDevice()) {
            validateIdentificationAttributes(endDevice, linkageAction);
        }

    }

    private void validateIdentificationAttributes(Meter meter, MasterDataLinkageAction linkageAction)
            throws FaultMessage {
        if (!StringUtils.isEmpty(meter.getMRID())) {
            return;
        }
        meter.getNames().stream().findFirst().map(Name::getName)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(linkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, METER_ELEMENT));
    }

    private void validateIdentificationAttributes(UsagePoint usagePoint, MasterDataLinkageAction linkageAction)
            throws FaultMessage {
        if (!StringUtils.isEmpty(usagePoint.getMRID())) {
            return;
        }
        usagePoint.getNames().stream().findFirst().map(Name::getName)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(linkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, USAGE_POINT_ELEMENT));
    }

    private void validateIdentificationAttributes(EndDevice endDevice, MasterDataLinkageAction linkageAction)
            throws FaultMessage {
        if (!StringUtils.isEmpty(endDevice.getMRID())) {
            return;
        }
        endDevice.getNames().stream().findFirst().map(Name::getName)
                .orElseThrow(faultMessageFactory.createMasterDataLinkageFaultMessageSupplier(linkageAction,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICE_LIST_ELEMENT));
    }
}
