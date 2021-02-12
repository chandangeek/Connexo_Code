package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.Message;
import com.energyict.protocolimplv2.dlms.common.writers.impl.GenericNoParamMethodInvoke;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

public class BillingReset extends GenericNoParamMethodInvoke implements Message {

    public BillingReset(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter) {
        super(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter, ObisCode.fromString("0.0.10.0.1.255"), DLMSClassId.SCRIPT_TABLE, 1, DeviceActionMessage.BILLING_RESET);
    }
}
