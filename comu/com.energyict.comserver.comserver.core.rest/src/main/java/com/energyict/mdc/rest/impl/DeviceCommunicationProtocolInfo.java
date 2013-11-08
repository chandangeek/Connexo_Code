package com.energyict.mdc.rest.impl;

import com.energyict.cbo.BusinessException;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdw.shadow.PluggableClassShadow;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.SQLException;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 11:21
 */
@XmlRootElement
public class DeviceCommunicationProtocolInfo {

    public int id;
    public String name;
    public String javaClassName;
    public String deviceProtocolVersion;

    public DeviceCommunicationProtocolInfo() {
    }

    public DeviceCommunicationProtocolInfo(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        this.name = deviceProtocolPluggableClass.getName();
        this.javaClassName = deviceProtocolPluggableClass.getJavaClassName();
        this.id = deviceProtocolPluggableClass.getId();
        this.deviceProtocolVersion = deviceProtocolPluggableClass.getVersion();
    }

    public void update(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        PluggableClassShadow shadow = deviceProtocolPluggableClass.getShadow();
        shadow.setName(this.name);
        try {
            deviceProtocolPluggableClass.update(shadow);
        } catch (SQLException e) {
            System.out.print("TODO - No logger yet on DeviceCommunicationProtocolInfo -> " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (BusinessException e) {
            System.out.print("TODO - No logger yet on DeviceCommunicationProtocolInfo -> " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
