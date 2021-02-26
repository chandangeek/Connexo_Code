package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.Data;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.Message;
import com.energyict.protocolimplv2.dlms.common.writers.impl.AbstractMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.util.logging.Level;

public abstract class DSTTime extends AbstractMessage implements Message {

    public static ObisCode DST_WORKING_MODE_OBIS = ObisCode.fromString("0.0.131.0.4.255");
    public static ObisCode DST_GENERIC_PARAMS_OBIS = ObisCode.fromString("0.0.131.0.6.255");

    private final AbstractDlmsProtocol dlmsProtocol;
    protected final PropertySpecService propSpecService;
    protected final NlsService nlsService;
    protected final Converter converter;

    public DSTTime(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter) {
        super(collectedDataFactory, issueFactory);
        this.dlmsProtocol = dlmsProtocol;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    public CollectedMessage execute(OfflineDeviceMessage message, boolean startOfDST) {
        try {
            int month, dayOfMonth, dayOfWeek, hour;
            month = getAsInt(message, DeviceMessageConstants.month);
            dayOfMonth = getAsInt(message, DeviceMessageConstants.dayOfMonth);
            dayOfWeek = getAsInt(message, DeviceMessageConstants.dayOfWeek);
            if (dayOfWeek == 0x07) {
                dayOfWeek = 0x00;       // Sunday is day 0, not 7;
            }
            hour = getAsInt(message, DeviceMessageConstants.hour);

            checkSetDSTWorkingMode();
            Data genericDSTParameters = dlmsProtocol.getDlmsSession().getCosemObjectFactory().getData(DST_GENERIC_PARAMS_OBIS);
            Structure oldStructure = (Structure) genericDSTParameters.getValueAttr();

            OctetString dateAndTime = new OctetString(new byte[]{
                    (byte) 0x7F,
                    (byte) month,
                    (byte) dayOfMonth,
                    (byte) dayOfWeek,
                    (byte) hour
            });
            Structure newStructure = new Structure();

            Structure dateAndTimeStruct = new Structure();
            dateAndTimeStruct.addDataType(dateAndTime);
            dateAndTimeStruct.addDataType(new Unsigned8(startOfDST ? 0 : 1));
            Structure innerStruct = new Structure();
            innerStruct.addDataType(new Unsigned8(33));
            innerStruct.addDataType(new Unsigned8(0));
            dateAndTimeStruct.addDataType(innerStruct);

            Array datesArray = new Array();
            if (startOfDST) {
                datesArray.addDataType(dateAndTimeStruct);
                Structure structure = ((Array) oldStructure.getStructure().getDataType(1)).getDataType(1).getStructure();
                datesArray.addDataType(structure);
            } else {
                datesArray.addDataType(((Array) oldStructure.getStructure().getDataType(1)).getDataType(0).getStructure());
                datesArray.addDataType(dateAndTimeStruct);
            }

            newStructure.addDataType(new Unsigned8(60));
            newStructure.addDataType(datesArray);

            genericDSTParameters.setValueAttr(newStructure);
        } catch (IOException e) {
            super.createErrorCollectedMessage(message, e);
        }
        return super.createConfirmedCollectedMessage(message);
    }

    protected int getAsInt(OfflineDeviceMessage message, String attName) throws ProtocolException {
        int month;
        String monthString = super.getMessageAttribute(message, attName).getValue();
        month = (monthString != null && monthString.length() != 0) ? Integer.parseInt(monthString) : 0x7F;
        return month;
    }


    private void checkSetDSTWorkingMode() throws IOException {
        Data data = dlmsProtocol.getDlmsSession().getCosemObjectFactory().getData(DST_WORKING_MODE_OBIS);
        long mode = data.getValue();
        if (mode != 1) {
            if (mode == 0) {
                dlmsProtocol.journal(Level.WARNING, "Warning: DST switching is not enabled - Please enable switching by using 'Enable Daylight Saving Time' message.");
            } else {
                // Incompatible DST working mode found - set the mode to 1.
                dlmsProtocol.journal(Level.INFO, "Incompatible DST working mode (" + mode + "). The mode will be set to 1.");
                Unsigned8 newMode = new Unsigned8(1);
                data.setValueAttr(newMode);
            }
        }
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
