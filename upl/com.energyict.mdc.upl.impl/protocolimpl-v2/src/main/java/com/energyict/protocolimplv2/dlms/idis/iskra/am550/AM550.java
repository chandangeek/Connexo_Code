package com.energyict.protocolimplv2.dlms.idis.iskra.am550;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.iskra.am550.events.Am550LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.Mx382;

import java.io.IOException;

public class AM550 extends Mx382 {

    public AM550(PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                 CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor,
                 DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new Am550LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return idisLogBookFactory;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        CollectedBreakerStatus result = super.getBreakerStatus();
        if (hasBreaker()) {
            try {
                Disconnector disconnector = getDlmsSession().getCosemObjectFactory().getDisconnector();
                TypeEnum controlState = disconnector.doReadControlState();
                switch (controlState.getValue()) {
                    case 0:
                        result.setBreakerStatus(BreakerStatus.DISCONNECTED);
                        break;
                    case 1:
                        result.setBreakerStatus(BreakerStatus.CONNECTED);
                        break;
                    case 2:
                        result.setBreakerStatus(BreakerStatus.ARMED);
                        break;
                    default:
                        ObisCode source = Disconnector.getDefaultObisCode();
                        result.setFailureInformation(ResultType.InCompatible, this.getIssueFactory()
                                .createProblem(source, "issue.protocol.readingOfBreakerStateFailed", "received value '" + controlState.getValue() + "', expected either 0, 1 or 2."));
                        break;
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                    ObisCode source = Disconnector.getDefaultObisCode();
                    result.setFailureInformation(ResultType.InCompatible, this.getIssueFactory().createProblem(source, "issue.protocol.readingOfBreakerStateFailed", e.toString()));
                }
            }
        }
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco AM550 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-02-01$";
    }

}
