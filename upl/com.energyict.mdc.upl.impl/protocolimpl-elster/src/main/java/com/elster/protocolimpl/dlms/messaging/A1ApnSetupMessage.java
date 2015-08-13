package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.energyict.protocol.MessageEntry;

import java.io.IOException;

public class A1ApnSetupMessage extends ApnSetupMessage {

    private static final String function = "SetGprsConfiguration";

    public A1ApnSetupMessage(DlmsMessageExecutor messageExecutor)
    {
        super(messageExecutor);
    }

    public boolean canExecuteThisMessage(MessageEntry messageEntry)
    {
        return isMessageTag(GPRS_MODEM_SETUP, messageEntry.getContent());
    }

    protected void write(String apn, String user, String password) throws IOException
    {

        CosemApplicationLayer layer = getExecutor().getDlms().getCosemApplicationLayer();
        ObjectPool pool = getExecutor().getDlms().getObjectPool();
        int a1Version = getExecutor().getDlms().getSoftwareVersion();

        IReadWriteObject rwObject = pool.findByFunction(a1Version, function);
        if (rwObject == null)
        {
            throw new IOException("This A1 doesn't support function '" + function + "'");
        }

        rwObject.write(layer, new Object[]
                {
                        apn, user, password
                });
    }
}
