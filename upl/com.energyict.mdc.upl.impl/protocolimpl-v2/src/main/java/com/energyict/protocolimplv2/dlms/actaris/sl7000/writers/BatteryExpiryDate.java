package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Data;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
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
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BatteryExpiryDate extends AbstractMessage implements Message {

    private static final ObisCode BATTERY_EXPIRY_OBIS = ObisCode.fromString("0.0.96.6.2.255");

    private final AbstractDlmsProtocol dlmsProtocol;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public BatteryExpiryDate(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter) {
        super(collectedDataFactory, issueFactory);
        this.dlmsProtocol = dlmsProtocol;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public CollectedMessage execute(OfflineDeviceMessage message) {
        try {
            int year, month, dayOfMonth;
            OfflineDeviceMessageAttribute messageAttribute = super.getMessageAttribute(message, DeviceMessageConstants.ConfigurationChangeDate);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(messageAttribute.getValue());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH);
            year = cal.get(Calendar.YEAR);

            Data expiryObject = dlmsProtocol.getDlmsSession().getCosemObjectFactory().getData(BATTERY_EXPIRY_OBIS);

            byte[] expiryDate = new byte[12];
            expiryDate[0] = (byte) ((year & 0xFF00) >> 8);
            expiryDate[1] = (byte) (year & 0xFF);
            expiryDate[2] = (byte) month;
            expiryDate[3] = (byte) dayOfMonth;
            for (int i = 4; i < 12; i++) {
                expiryDate[i] = (byte) 0xFF;
            }
            OctetString data = new OctetString(expiryDate);
            expiryObject.setValueAttr(data);

            return super.createCollectedMessage(message);
        } catch (ParseException | IOException e) {
            return super.createErrorCollectedMessage(message, e);
        }
    }

    @Override
    public DeviceMessageSpec asMessageSpec() {
        return ConfigurationChangeDeviceMessage.ProgramBatteryExpiryDate.get(propSpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
