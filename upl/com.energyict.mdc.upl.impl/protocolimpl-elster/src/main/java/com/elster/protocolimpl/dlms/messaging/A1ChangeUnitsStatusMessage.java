package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.data.DlmsData;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.elster.protocolimpl.dlms.objects.a1.UNITSStatusChanger;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

public class A1ChangeUnitsStatusMessage extends AbstractDlmsMessage
{

    private static final String function = "UnitsStatus";
    public static final String MESSAGE_TAG = "UnitsStatus";
    public static final String MESSAGE_DESCRIPTION = "Change UNITS status";
    public static final String ATTR_UNITS_STATUS = "UNITS_Status";

    public A1ChangeUnitsStatusMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    public boolean canExecuteThisMessage(MessageEntry messageEntry)
    {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    public void executeMessage(MessageEntry messageEntry) throws BusinessException
    {
        String unitsStatus = MessagingTools.getContentOfAttribute(messageEntry, ATTR_UNITS_STATUS);
        validateMessageData(unitsStatus);
        try
        {
            write(unitsStatus);
        }
        catch (IOException e)
        {
            throw new BusinessException("Unable to set new UNITS status: " + e.getMessage());
        }
    }

    protected void write(String newStatus) throws IOException
    {
        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null) {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }

        UNITSStatusChanger.DeviceState newState = StringToDeviceState(newStatus);
        getExecutor().getLogger().info("Change UNITS state - new  state:" + newState.toString());

        if (rwObject instanceof UNITSStatusChanger) {
            UNITSStatusChanger.DeviceState currState = ((UNITSStatusChanger) rwObject).getCurrentState(layer, false);
            getExecutor().getLogger().info("Change UNITS state - curr state:" + currState.toString());
            DlmsData dd = ((UNITSStatusChanger) rwObject).getParameters(layer, newState);
            getExecutor().getLogger().info("Change UNITS state - func param:" + dd.toString());
        }
        rwObject.write(layer, new Object[]{newState});
    }

    private void validateMessageData(String unitsStatus) throws BusinessException
    {
        UNITSStatusChanger.DeviceState state = StringToDeviceState(unitsStatus);
        if (state == UNITSStatusChanger.DeviceState.UNKNOWN)
        {
            throw new BusinessException(ATTR_UNITS_STATUS + ": error in definition");
        }
    }

    private UNITSStatusChanger.DeviceState StringToDeviceState(String unitsStatus)
    {
        if (unitsStatus.equalsIgnoreCase("NORMAL"))
        {
            return UNITSStatusChanger.DeviceState.NORMAL;
        }
        if (unitsStatus.equalsIgnoreCase("MAINTENANCE"))
        {
            return UNITSStatusChanger.DeviceState.MAINTENANCE;
        }
        return UNITSStatusChanger.DeviceState.UNKNOWN;
    }

    public static MessageSpec getMessageSpec(boolean advanced)
    {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_UNITS_STATUS, true));

        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
