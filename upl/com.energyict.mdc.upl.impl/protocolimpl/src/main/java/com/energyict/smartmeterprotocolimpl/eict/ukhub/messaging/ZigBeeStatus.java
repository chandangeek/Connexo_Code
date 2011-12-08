package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.ObisCodeProvider;

import java.io.IOException;
import java.util.logging.Level;

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
                        throw new IOException("ZigBee backup probably failed, takes to long (30s) before 'HAN Backup Performed'-event is written.");
                    }
                } catch (InterruptedException e) {
                    throw new BusinessException(e);
                }
            }

            Structure backUpData = hanManagement.readBackupData();
            ZigBeeSASStartup sasStartup = getCosemObjectFactory().getZigBeeSASStartup();
            OctetString extendedPanId = sasStartup.readExtendedPanId();
            OctetString linkKey = sasStartup.readLinkKey();
            OctetString networkKey = sasStartup.readNetworkKey();

            HanBackupRestoreData hanBackupData = new HanBackupRestoreData();
            hanBackupData.setBackupData(backUpData);
            hanBackupData.setExtendedPanId(extendedPanId);
            hanBackupData.setLinkKey(linkKey);
            hanBackupData.setNetworkKey(networkKey);

            sb.append("     extendedPanId=").append(extendedPanId).append('\n');
            sb.append("     linkKey=").append(linkKey).append('\n');
            sb.append("     networkKey=").append(networkKey).append('\n');
            sb.append("     backUpData=").append(backUpData).append('\n');

        } catch (IOException e) {
            sb.append("Unable to read ZigbeeHanManagement.backupData: ").append(e.getMessage()).append('\n');
        } catch (BusinessException e) {
            sb.append("Unable to read ZigbeeHanManagement.backupData: ").append(e.getMessage()).append('\n');
        } finally {
            sb.append("\n\n");
        }
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
                    AbstractDataType dataType = activeDevices.getDataType(i);
                    sb.append("    [").append(i).append('/').append(items).append("] ").append(dataType).append('\n');
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
                    sb.append("    [").append(i).append('/').append(items).append("] ").append(dataType).append('\n');
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
