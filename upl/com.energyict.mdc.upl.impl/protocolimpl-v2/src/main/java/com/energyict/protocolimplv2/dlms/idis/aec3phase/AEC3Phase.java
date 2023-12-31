package com.energyict.protocolimplv2.dlms.idis.aec3phase;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolimplv2.common.composedobjects.ComposedMeterInfo;
import com.energyict.protocolimplv2.dlms.idis.aec.AEC;
import com.energyict.protocolimplv2.dlms.idis.aec.registers.AECRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.aec3phase.events.AEC3PhaseLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.aec3phase.profiledata.AEC3PhaseProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.aec3phase.registers.AEC3PhaseRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;

import java.io.IOException;

public class AEC3Phase extends AEC {
    protected static final DLMSAttribute DEFAULT_SERIAL_NUMBER = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");
    protected static final DLMSAttribute DEFAULT_CLOCK = DLMSAttribute.fromString("8:0.0.1.0.0.255:2");
    private ComposedMeterInfo meterInfo;

    public AEC3Phase(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected void readObjectList() {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(new AEC3phaseObjectList().getObjectList());
    }

    @Override
    public String getProtocolDescription() {
        return "AEC DLMS Three phase";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-03-03$";
    }

    @Override
    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new AEC3PhaseLogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return idisLogBookFactory;
    }

    @Override
    public IDISProfileDataReader getIDISProfileDataReader() {
        if (idisProfileDataReader == null) {
            idisProfileDataReader = new AEC3PhaseProfileDataReader(this, this.getCollectedDataFactory(), this.getIssueFactory(), getDlmsSessionProperties().getLimitMaxNrOfDays());
        }
        return idisProfileDataReader;
    }

    @Override
    protected AECRegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AEC3PhaseRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public String getSerialNumber() {
        AbstractDataType attribute = getMeterInfo().getAttribute(DEFAULT_SERIAL_NUMBER);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else if (attribute instanceof VisibleString) {
            return attribute.getVisibleString().getStr();
        } else {
            IOException ioException = new IOException("Expected OctetString or VisualString but was " + attribute.getClass().getSimpleName());
            throw CommunicationException.unexpectedResponse(ioException);
        }
    }

    @Override
    protected ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(),
                    getDlmsSessionProperties().isBulkRequest(),
                    getDlmsSessionProperties().getRoundTripCorrection(),
                    getDlmsSessionProperties().getRetries(),
                    DEFAULT_SERIAL_NUMBER,
                    DEFAULT_CLOCK
            );
        }
        return meterInfo;
    }

}
