package com.elster.genericprotocolimpl.dlms.ek280.discovery;

import com.elster.genericprotocolimpl.dlms.ek280.discovery.core.Parameters;
import com.elster.genericprotocolimpl.dlms.ek280.discovery.core.RequestParameters;
import com.energyict.mdw.core.*;

/**
 * Copyrights
 * Date: 8/06/11
 * Time: 8:47
 *
 * 15/11/2011 added evaluation of parameter "type" in request telegram - gh
 */
public class DeviceDiscoverInfo {

    private final Parameters parameters;
    private RtuType rtuType = null;
    private Folder folder = null;

    public DeviceDiscoverInfo(Parameters serialNumber) {
        this.parameters = serialNumber;

        if (serialNumber instanceof RequestParameters) {
            String rtuTypeName = ((RequestParameters) serialNumber).getRtuType();
            rtuType = MeteringWarehouse.getCurrent().getRtuTypeFactory().find(rtuTypeName);
        }
    }

    public String getCallHomeId() {
        return parameters != null ? parameters.getSerialId() : "";
    }

    public void setRtuType(RtuType rtuType) {
        this.rtuType = rtuType;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public RtuType getRtuType() {
        return rtuType;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getSerialNumber() {
        if (getCallHomeId() != null) {
            String[] parts = getCallHomeId().split(":");
            if ((parts != null) && (parts.length > 0)) {
                return parts[0];
            }
        }
        return null;
    }

}
