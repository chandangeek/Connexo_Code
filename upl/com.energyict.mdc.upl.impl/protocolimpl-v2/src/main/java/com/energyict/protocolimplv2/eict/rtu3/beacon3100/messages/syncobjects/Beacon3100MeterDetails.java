package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:07
 */
@XmlRootElement
public class Beacon3100MeterDetails {

    private String macAddress;
    /**
     * For Beacon firmware version < R10.1
     */
    private long deviceTypeId;

    public void setDeviceTypeAssignments(List<DeviceTypeAssignment> deviceTypeAssignments) {
        this.deviceTypeAssignments = deviceTypeAssignments;
    }

    /**
     * For Beacon firmware version >= R10.1
     */
    private List<DeviceTypeAssignment> deviceTypeAssignments;
    private String deviceTimeZone;
    private String serialNumber;
    private String llsSecret;
    private String authenticationKey;
    private String encryptionKey;
    private List<Beacon3100ClientDetails> clientDetails;

    public Beacon3100MeterDetails(String macAddress, long deviceTypeId, String deviceTimeZone, String serialNumber, List<Beacon3100ClientDetails> clientDetails, String llsSecret, String authenticationKey, String encryptionKey) {
        this.macAddress = macAddress;
        this.deviceTypeId = deviceTypeId;
        this.deviceTimeZone = deviceTimeZone;
        this.serialNumber = serialNumber;
        this.clientDetails = clientDetails;
        this.llsSecret = llsSecret;
        this.authenticationKey = authenticationKey;
        this.encryptionKey = encryptionKey;
    }

    public Beacon3100MeterDetails(String macAddress, List<DeviceTypeAssignment> deviceTypeAssignments, String deviceTimeZone, String serialNumber, List<Beacon3100ClientDetails> clientDetails, String llsSecret, String authenticationKey, String encryptionKey) {
        this.macAddress = macAddress;
        this.deviceTimeZone = deviceTimeZone;
        this.deviceTypeAssignments = deviceTypeAssignments;
        this.serialNumber = serialNumber;
        this.clientDetails = clientDetails;
        this.llsSecret = llsSecret;
        this.authenticationKey = authenticationKey;
        this.encryptionKey = encryptionKey;
    }

    //JSon constructor
    private Beacon3100MeterDetails() {
    }

    @XmlAttribute
    public String getMacAddress() {
        return macAddress;
    }

    @XmlAttribute
    public String getDeviceTimeZone() {
        return deviceTimeZone;
    }

    @XmlAttribute
    public long getDeviceTypeId() {
        return deviceTypeId;
    }

    @XmlAttribute
    public String getLlsSecret() {
        return llsSecret;
    }

    @XmlAttribute
    public String getAuthenticationKey() {
        return authenticationKey;
    }

    @XmlAttribute
    public String getEncryptionKey() {
        return encryptionKey;
    }

    @XmlAttribute
    public String getSerialNumber() {
        return serialNumber;
    }

    @XmlAttribute
    public List<Beacon3100ClientDetails> getClientDetails() {
        return clientDetails;
    }

    @XmlAttribute
    public List<DeviceTypeAssignment> getDeviceTypeAssignments() {
        return deviceTypeAssignments;
    }


    public Structure toStructure(boolean isFirmwareVersion140OrAbove) {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getMacAddress(), "")));
        structure.addDataType(new Unsigned32(getDeviceTypeId()));
        structure.addDataType(new VisibleString(getDeviceTimeZone()));
        structure.addDataType(OctetString.fromString(getSerialNumber()));
        if(isFirmwareVersion140OrAbove){
            final Array clientDetailsArray = new Array();
            for (Beacon3100ClientDetails beacon3100ClientDetails : getClientDetails()) {
                clientDetailsArray.addDataType(beacon3100ClientDetails.toStructure());
            }
            structure.addDataType(clientDetailsArray);
        } else {
            structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getLlsSecret(), "")));
            structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getAuthenticationKey(), "")));
            structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getEncryptionKey(), "")));
        }
        return structure;
    }

    public Structure toStructureNewFW() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getMacAddress(), "")));
        final Array deviceTypeAssignmentArray = new Array();
        for (DeviceTypeAssignment deviceTypeAssignment : getDeviceTypeAssignments()) {
            deviceTypeAssignmentArray.addDataType(deviceTypeAssignment.toStructure());
        }
        structure.addDataType(deviceTypeAssignmentArray);
        structure.addDataType(new VisibleString(getDeviceTimeZone()));
        structure.addDataType(OctetString.fromString(getSerialNumber()));

        final Array clientDetailsArray = new Array();
        for (Beacon3100ClientDetails beacon3100ClientDetails : getClientDetails()) {
            clientDetailsArray.addDataType(beacon3100ClientDetails.toStructure());
        }
        structure.addDataType(clientDetailsArray);

        return structure;
    }

    public boolean containsDeviceAssignment(long configurationId, String startTime, String endTime) {
        if(deviceTypeAssignments == null){
            return false;
        }
        for(DeviceTypeAssignment deviceTypeAssignment : deviceTypeAssignments){
            if(deviceTypeAssignment.getDeviceTypeId() == configurationId &&
               deviceTypeAssignment.getStartDate().equals(startTime) &&
               deviceTypeAssignment.getEndDate().equals(endTime)){
                return true;
            }
        }
        return false;
    }

    public boolean containsDeviceAssignment(long configurationId) {
        if(deviceTypeAssignments == null){
            return false;
        }
        for(DeviceTypeAssignment deviceTypeAssignment : deviceTypeAssignments){
            if(deviceTypeAssignment.getDeviceTypeId() == configurationId){
                return true;
            }
        }
        return false;
    }
}