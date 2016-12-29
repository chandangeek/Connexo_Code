package com.energyict.protocolimplv2.dlms.idis.iskra.mx382;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;

import java.io.IOException;

/**
 * Created by cisac on 1/14/2016.
 */
public class Mx382RegisterFactory extends AM130RegisterFactory{

    public Mx382RegisterFactory(Mx382 mx382, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(mx382, collectedDataFactory, issueFactory);
    }

    protected CollectedRegister readBillingRegister(OfflineRegister offlineRegister) {
        try {
            HistoricalValue historicalValue = ((Mx382) getMeterProtocol()).getStoredValues().getHistoricalValue(offlineRegister.getObisCode());
            RegisterValue registerValue = new RegisterValue(offlineRegister.getObisCode(), historicalValue.getQuantityValue(), historicalValue.getEventTime());
            return createCollectedRegister(registerValue, offlineRegister);
        } catch (NoSuchRegisterException e) {
            return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported, e.getMessage());
        } catch (NotInObjectListException e) {
            return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        } catch (IOException e) {
            return handleIOException(offlineRegister, e);
        }
    }
}
