package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

/**
 * User: heuckeg
 * Date: 08.06.11
 * Time: 13:56
 */
public class MeterLocationMessage extends AbstractDlmsMessage {

    public static final int METERLOCATION_MAX_LENGTH = 30;

    /**
     * RtuMessage tags for the GPRS modem setup message
     */
    public final static String MESSAGE_DESC = "Change meter location";

    public static final String MESSAGE_TAG = "MeterLocation";
    public static final String ATTR_LOCATION = "Location";

    public MeterLocationMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    /**
     * Send the message (containing the apn configuration) to the meter.
     *
     * @param messageEntry: the message containing the apn configuration
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String location = MessagingTools.getContentOfAttribute(messageEntry, ATTR_LOCATION);
        validateMeterLocationMessage(location);

        write(location);
    }

    private void write(String location) throws BusinessException {

        CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(
                new com.elster.dlms.types.basic.ObisCode("7.128.0.0.6.255"), 1, 2);

        //--- Write ---
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        DataAccessResult accessResult;
        try {
            accessResult = layer.setAttribute(attributeDescriptor, new DlmsDataVisibleString(location), null);
            if (accessResult != DataAccessResult.SUCCESS) {
                throw new IOException("setAttribute failure:" + accessResult);
            }
        } catch (IOException e) {
            throw new BusinessException("write meter location: " + e.getMessage());
        }
    }


    private void validateMeterLocationMessage(String location) throws BusinessException {
        if ((location == null) || ("".equals(location))) {
            throw new BusinessException("Parameter Location was 'null' or empty.");
        } else if (location.length() > METERLOCATION_MAX_LENGTH) {
            throw new BusinessException("Parameter Location exceeded the maximum length (30 characters).");
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESC, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);

        // Add attributes Location
        tagSpec.add(new MessageAttributeSpec(ATTR_LOCATION, false));

        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
