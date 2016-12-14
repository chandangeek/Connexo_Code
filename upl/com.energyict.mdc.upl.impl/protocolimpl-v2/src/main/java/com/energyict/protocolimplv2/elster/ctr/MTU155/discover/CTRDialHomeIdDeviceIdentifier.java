package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;


import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.protocol.exceptions.identifier.DuplicateException;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface,
 * specific for the CTR protocol base (MTU155 and EK155 device types). <br></br>
 * These protocols use a Call Home ID to uniquely identify the calling device.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
@XmlRootElement
public class CTRDialHomeIdDeviceIdentifier implements ServerDeviceIdentifier {

    private final String callHomeID;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private CTRDialHomeIdDeviceIdentifier() {
        callHomeID = "";
    }

    public CTRDialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public Device findDevice() {
        if(this.device == null){
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw NotFoundException.notFound(Device.class, this.toString());
            } else {
                if (this.allDevices.size() > 1) {
                    throw DuplicateException.duplicateFoundFor(Device.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        this.allDevices = getDeviceFactory().findByConnectionTypeProperty(CTRInboundDialHomeIdConnectionType.class, LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, callHomeID);
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeID;
    }


    @XmlAttribute
    public String getCallHomeID() {
        return callHomeID;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public List<OfflineDevice> getAllDevices() {
        if (this.allDevices == null) {
            fetchAllDevices();
        }
        List<OfflineDevice> allOfflineDevices = new ArrayList<>();
        OfflineDeviceContext offlineDeviceContext = new DeviceOfflineFlags();
        for (Device deviceToGoOffline : this.allDevices) {
            allOfflineDevices.add(deviceToGoOffline.goOffline(offlineDeviceContext));
        }
        return allOfflineDevices;
    }

    private DeviceFactory getDeviceFactory() {
        return DeviceFactoryProvider.instance.get().getDeviceFactory();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "CallHomeId";
        }

        @Override
        public Object getValue(String role) {
            if ("callHomeId".equals(role)) {
                return callHomeID;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}