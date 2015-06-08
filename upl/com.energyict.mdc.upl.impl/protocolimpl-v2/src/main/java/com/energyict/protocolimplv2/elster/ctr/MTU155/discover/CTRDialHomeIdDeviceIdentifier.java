package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;


import com.energyict.cbo.NotFoundException;
import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierType;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocolimplv2.MdcManager;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface,
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
                throw new NotFoundException("Device with callHomeId " + this.callHomeID + " not found");
            } else {
                if (this.allDevices.size() > 1) {
                    throw MdcManager.getComServerExceptionFactory().createDuplicateException(Device.class, this.toString());
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

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public String getCallHomeID() {
        return callHomeID;
    }

    @Override
    public String getIdentifier() {
        return callHomeID;
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.CallHomeId;
    }

    @Override
    public List<OfflineDevice> getAllDevices() {
        if(this.allDevices == null){
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
}