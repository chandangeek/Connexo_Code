package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileFactory;
import com.energyict.mdw.core.LoadProfileFactoryProvider;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Implementation of a {@link LoadProfileIdentifier} that uniquely identifies a {@link LoadProfile}
 * based on the ObisCode of the LoadProfile(type) and the {@link DeviceIdentifier}.<br/>
 * <b>Note: </b> we assume that it is never possible that two LoadProfiles with the same ObisCode are configured on the Device.<br/>
 * <b>Note2: </b> if the B-field of the ObisCode is marked as a wildcard, then make sure the provided loadProfileObisCode also has the wildcard!
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:30
 */
@XmlRootElement
public class LoadProfileIdentifierByObisCodeAndDevice implements LoadProfileIdentifier {

    private final ObisCode loadProfileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private LoadProfile loadProfile;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public LoadProfileIdentifierByObisCodeAndDevice() {
        this.loadProfileObisCode = null;
        this.deviceIdentifier = null;
    }

    public LoadProfileIdentifierByObisCodeAndDevice(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.loadProfileObisCode = loadProfileObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public LoadProfile getLoadProfile() {
        if(loadProfile == null){
            final List<LoadProfile> loadProfiles = getLoadProfileFactory().findByRtu(deviceIdentifier.findDevice());
            for (LoadProfile profile : loadProfiles) {
                if (profile.getDeviceObisCode().equals(this.loadProfileObisCode)) {
                    this.loadProfile = profile;
                    break;
                }
            }
        }
        if (this.loadProfile == null) {
            throw new NotFoundException("LoadProfile with ObisCode " + loadProfileObisCode + " for device with " + deviceIdentifier.toString() + " not found");
        }
        return loadProfile;
    }

    @XmlAttribute
    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + deviceIdentifier + " and ObisCode = " + loadProfileObisCode;
    }

    private LoadProfileFactory getLoadProfileFactory() {
        return LoadProfileFactoryProvider.instance.get().getLoadProfileFactory();
    }
}
