package com.elster.genericprotocolimpl.dlms.ek280.discovery;

import com.elster.genericprotocolimpl.dlms.ek280.EK280;
import com.elster.genericprotocolimpl.dlms.ek280.EK280Properties;
import com.elster.genericprotocolimpl.dlms.ek280.discovery.core.Parameters;
import com.elster.genericprotocolimpl.dlms.ek280.discovery.core.ProtocolMeterDiscover;
import com.energyict.dialer.core.LinkException;
import com.energyict.mdw.core.*;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 8/06/11
 * Time: 8:47
 */
public class DeviceDiscover {

    private final EK280 ek280;

    private Folder folder = null;
    private DeviceType rtuType = null;

    public DeviceDiscover(EK280 ek280) {
        this.ek280 = ek280;
    }

    /**
     * Retrieve all the info we can that could needed to identify the incoming device.
     *
     * @return The information about the device, grouped together in a DeviceDiscoverInfo object
     *
     * @throws IOException in case of errors
     */
    public DeviceDiscoverInfo discoverDevice() throws IOException {
        try {
            ProtocolMeterDiscover meterDiscover = new ProtocolMeterDiscover(getEk280().getLink());
            Parameters parameters = meterDiscover.receiveParametersFromMeter();
            DeviceDiscoverInfo info = new DeviceDiscoverInfo(parameters);
            if (info.getRtuType() == null) {
                info.setRtuType(getRtuType());
            }
            info.setFolder(getFolder());
            return info;
        } catch (LinkException e) {
            throw new IOException(e.getMessage());
        }
    }

    public DeviceType getRtuType() {
        if (rtuType == null) {
            String rtuTypeName = getEk280().getProperties().getRtuTypeName();
            if (rtuTypeName != null) {
                rtuType = MeteringWarehouse.getCurrent().getRtuTypeFactory().find(rtuTypeName);
                if (rtuType == null) {
                    getLogger().severe("No matching rtu type found for [" + EK280Properties.RTU_TYPE + "=" + rtuTypeName + "].");
                }
            } else {
                getLogger().severe("No value found for property [" + EK280Properties.RTU_TYPE + "]. Unable to get DeviceType.");
            }
        }
        return rtuType;
    }

    private Logger getLogger() {
        return getEk280().getLogger();
    }

    public Folder getFolder() {
        if (folder == null) {
            String folderExternalName = getEk280().getProperties().getFolderExternalName();
            if (folderExternalName != null) {
                folder = MeteringWarehouse.getCurrent().getFolderFactory().findByExternalName(folderExternalName);
            }
        }
        return folder;
    }

    public EK280 getEk280() {
        return ek280;
    }
}
