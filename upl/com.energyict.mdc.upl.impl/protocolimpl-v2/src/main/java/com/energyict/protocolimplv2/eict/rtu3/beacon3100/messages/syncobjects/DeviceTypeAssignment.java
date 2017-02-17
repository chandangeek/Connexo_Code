package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTime;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * For Beacon firmware version >= R10.1
 */
@XmlRootElement
public class DeviceTypeAssignment {
    private long deviceTypeId;
    private Date startDate;
    private Date endDate;

    public DeviceTypeAssignment(long deviceTypeId, Date startDate, Date endDate) {
        this.deviceTypeId = deviceTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    //Needed by JSon
    public DeviceTypeAssignment(){}

    public long getDeviceTypeId() {
        return deviceTypeId;
    }

    @XmlAttribute
    public void setDeviceTypeId(long deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    @XmlAttribute
    public Date getStartDate() {
        return startDate;
    }


    @XmlAttribute
    public Date getEndDate() {
        return endDate;
    }


    public AbstractDataType toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(deviceTypeId));
        if(startDate != null) {
            structure.addDataType(new DateTime(startDate));
        }else{
            structure.addDataType(new NullData());
        }
        if(endDate != null) {
            structure.addDataType(new DateTime(endDate));
        }else{
            structure.addDataType(new NullData());
        }
        return structure;
    }
}
