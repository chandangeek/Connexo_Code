package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ZigBeeSASStartup;
import com.energyict.dlms.cosem.ZigBeeSETCControl;
import com.energyict.dlms.cosem.ZigbeeHanManagement;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.ObisCodeProvider;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/12/11
 * Time: 17:10
 */
public class ZigBeeStatus {

    private final CosemObjectFactory cof;

    public ZigBeeStatus(CosemObjectFactory cosemObjectFactory) {
        this.cof = cosemObjectFactory;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cof;
    }


    public String readStatus() {
        StringBuilder sb = new StringBuilder();

        sb.append(readBlackList());
        sb.append(readActiveDevices());
        sb.append(readBackupData());
        sb.append(readZigBeeSASStartupParameters());
        sb.append(readZigBeeSETCControl());

        return sb.toString();
    }

    private String readBackupData() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("ZigbeeHanManagement.backupData:\n");
            ZigbeeHanManagement hanManagement = getCosemObjectFactory().getZigbeeHanManagement();
            hanManagement.backup();

            boolean backedUp = false;
            int counter = 0;
            while (!backedUp) {
                try {
                    Thread.sleep(1000);
                    Long eventValue = getCosemObjectFactory().getData(ObisCodeProvider.HanManagementEventObject).getValue();
                    if (eventValue == 0x0126) {    //Should be event 'HAN Backup Performed'
                        backedUp = true;
                    } else if (counter++ >= 30) {
                        throw new IOException("ZigBee backup probably failed, takes too long (30s) before 'HAN Backup Performed'-event is written.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
            }

            Structure backUpData = hanManagement.readBackupData();
            if (backUpData == null || backUpData.getDataType(1) == null || backUpData.getDataType(1).getArray() == null) {
                sb.append("          No devices in backup data").append('\n');
                return sb.toString();
            }

            Array backUpDevices = backUpData.getDataType(1).getArray();
            AbstractDataType macAddress;
            AbstractDataType linkKey;
            sb.append("     backUpData:").append('\n');
            int numberOfBackupDevices = backUpDevices.nrOfDataTypes();
            if (numberOfBackupDevices > 0) {
                for (int deviceIndex = 0; deviceIndex < numberOfBackupDevices; deviceIndex++) {
                    sb.append("         Device ").append(String.valueOf(deviceIndex + 1)).append(":").append('\n');
                    Structure backupDevice = backUpDevices.getDataType(deviceIndex).getStructure();
                    macAddress = backupDevice.getDataType(0);
                    linkKey = backupDevice.getDataType(1);
                    sb.append("          Mac address = ").append(macAddress);
                    sb.append("          Link key = ").append(linkKey);
                }
            } else {
                sb.append("          No devices in backup data").append('\n');
            }

        } catch (IOException e) {
            sb.append("Unable to read ZigbeeHanManagement.backupData: ").append(e.getMessage()).append('\n');
        } finally {
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private String readZigBeeSASStartupParameters() {
        StringBuilder sb = new StringBuilder();
        sb.append("ZigbeeSASStartup:\n");

        ZigBeeSASStartup sasStartup;
        try {
            sasStartup = getCosemObjectFactory().getZigBeeSASStartup();
        } catch (IOException e) {
            sb.append("Unavailable\n");
            return sb.toString();
        }
        try {
            OctetString extendedPanId = sasStartup.readExtendedPanId();
            sb.append("     extendedPanId = ").append(extendedPanId);
        } catch (IOException e) {
            sb.append("     extendedPanId = ").append("unavailable").append('\n');
        }
        try {
            Unsigned16 panId = sasStartup.readPanId();
            sb.append("     panId = ").append(panId);
        } catch (IOException e) {
            sb.append("     panId = ").append("unavailable").append('\n');
        }
        try {
            Unsigned32 channelMask = sasStartup.readChannelMask();
            sb.append("     channelMask = ").append(channelMask);
        } catch (IOException e) {
            sb.append("     channelMask = ").append("unavailable").append('\n');
        }
        try {
            Unsigned8 protocolVersion = sasStartup.readProtocolVersion();
            sb.append("     protocol_version = ").append(protocolVersion);
        } catch (IOException e) {
            sb.append("     protocol_version = ").append("unavailable").append('\n');
        }
        try {
            Unsigned8 stack_profile = sasStartup.readStackProfile();
            sb.append("     stack_profile = ").append(stack_profile);
        } catch (IOException e) {
            sb.append("     stack_profile = ").append("unavailable").append('\n');
        }
        try {
            Unsigned8 startUpControl = sasStartup.readStartUpControl();
            sb.append("     start_up_control = ").append(startUpControl);
        } catch (IOException e) {
            sb.append("     start_up_control = ").append("unavailable").append('\n');
        }
        try {
            OctetString trust_centre_address = sasStartup.readTrustCentreAddress();
            sb.append("     trust_centre_address = ").append(trust_centre_address);
        } catch (IOException e) {
            sb.append("     trust_centre_address = ").append("unavailable").append('\n');
        }
        try {
            OctetString linkKey = sasStartup.readLinkKey();
            sb.append("     linkKey = ").append(linkKey);
        } catch (IOException e) {
            sb.append("     linkKey = ").append("unavailable").append('\n');
        }
        try {
            OctetString networkKey = sasStartup.readNetworkKey();
            sb.append("     networkKey = ").append(networkKey);
        } catch (IOException e) {
            sb.append("     networkKey = ").append("unavailable").append('\n');
        }
        try {
            BooleanObject useInsecureJoin = sasStartup.readUseInsecureJoin();
            sb.append("     use_insecure_join = ").append(useInsecureJoin);
        } catch (IOException e) {
            sb.append("     use_insecure_join = ").append("unavailable").append('\n');
        }
        sb.append("\n\n");
        return sb.toString();
    }

    private String readZigBeeSETCControl() {
        StringBuilder sb = new StringBuilder();
        sb.append("ZigBeeSETCControl:\n");

        ZigBeeSETCControl zigBeeSETCControl;
        try {
            zigBeeSETCControl = getCosemObjectFactory().getZigBeeSETCControl();
        } catch (IOException e) {
            sb.append("Unavailable\n");
            return sb.toString();
        }
        try {
            BooleanObject enable_disable_joining = zigBeeSETCControl.getEnableDisableJoining();
            sb.append("     enable_disable_joining = ").append(enable_disable_joining);
        } catch (IOException e) {
            sb.append("     enable_disable_joining = ").append("unavailable").append('\n');
        }
        try {
            Unsigned16 joinTimeout = zigBeeSETCControl.getJoinTimeout();
            sb.append("     joinTimeout = ").append(joinTimeout);
        } catch (IOException e) {
            sb.append("     joinTimeout = ").append("unavailable").append('\n');
        }
        sb.append("\n\n");
        return sb.toString();
    }

    private String readActiveDevices() {
        StringBuilder sb = new StringBuilder();
        try {
            ZigbeeHanManagement zigbeeHanManagement = getCosemObjectFactory().getZigbeeHanManagement();
            Array activeDevices = zigbeeHanManagement.getActiveDevices();
            sb.append("ZigbeeHanManagement.activeDevices:\n");
            int items = activeDevices.nrOfDataTypes();
            if (items > 0) {
                for (int i = 0; i < items; i++) {
                    Structure activeDevice = activeDevices.getDataType(i).getStructure();
                    sb.append("    Active device [").append(i + 1).append('/').append(items).append("]").append('\n');
                    if (activeDevice == null) {
                        sb.append("        null");
                    } else {
                        sb.append("        Mac address: ").append(activeDevice.getDataType(0));
                        sb.append("        Max RSSI: ").append(activeDevice.getDataType(1));
                        sb.append("        Avg RSSI: ").append(activeDevice.getDataType(2));
                        sb.append("        Min RSSI: ").append(activeDevice.getDataType(3));
                        sb.append("        Max LQI: ").append(activeDevice.getDataType(4));
                        sb.append("        Avg LQI: ").append(activeDevice.getDataType(5));
                        sb.append("        Min LQI: ").append(activeDevice.getDataType(6));
                    }
                }
            } else {
                sb.append("    No devices in activeDevices");
            }
        } catch (IOException e) {
            sb.append("Unable to read ZigbeeHanManagement.activeDevices: ").append(e.getMessage()).append('\n');
        } finally {
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private String readBlackList() {
        StringBuilder sb = new StringBuilder();
        try {
            ZigbeeHanManagement zigbeeHanManagement = getCosemObjectFactory().getZigbeeHanManagement();
            Array blackList = zigbeeHanManagement.getBlackList();
            sb.append("ZigbeeHanManagement.blacklist:\n");
            int items = blackList.nrOfDataTypes();
            if (items > 0) {
                for (int i = 0; i < items; i++) {
                    AbstractDataType dataType = blackList.getDataType(i);
                    sb.append("    [").append(i + 1).append('/').append(items).append("] Mac address: ").append(dataType);
                }
            } else {
                sb.append("    No devices in black list");
            }
        } catch (IOException e) {
            sb.append("Unable to read ZigbeeHanManagement.blacklist: ").append(e.getMessage()).append('\n');
        } finally {
            sb.append("\n\n");
        }
        return sb.toString();
    }

}
