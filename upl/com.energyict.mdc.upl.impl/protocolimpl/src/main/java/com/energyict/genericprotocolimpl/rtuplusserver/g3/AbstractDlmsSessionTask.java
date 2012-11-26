package com.energyict.genericprotocolimpl.rtuplusserver.g3;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.Device;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 8/03/12
 * Time: 9:24
 */
public abstract class AbstractDlmsSessionTask {

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    private final DlmsSession session;
    private final RtuPlusServerTask task;

    protected AbstractDlmsSessionTask(DlmsSession dlmsSession, RtuPlusServerTask task) {
        this.session = dlmsSession;
        this.task = task;
    }

    protected final DlmsSession getSession() {
        return session;
    }

    protected final RtuPlusServerTask getTask() {
        return task;
    }

    protected final Logger getLogger() {
        return session.getLogger();
    }

    protected final CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }
    
    protected final TimeZone getTimeZone() {
        return session.getTimeZone();
    }
    
    protected final CommunicationProfile getCommunicationProfile() {
        return task.getCommunicationProfile();
    }
    
    protected final String format(Date date) {
        return format.format(date);
    }

    protected final Device getGateway() {
        return task.getGateway();
    }

    protected final String getGatewaySerialNumber() {
        return task.getGatewaySerialNumber();
    }

}
