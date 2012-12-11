package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.cbo.BusinessException;
import com.energyict.interval.IntervalRecord;
import com.energyict.mdw.core.*;
import com.energyict.protocol.IntervalStateBits;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 28/02/11
 * Time: 9:29
 */
public class ValidateProfile {

    private Folder parentFolder = null;
    private List<Device> rtus;
    private ValidationProperties properties;

    public ValidateProfile(ValidationProperties properties) {
        this.properties = properties;
        if (getProperties().isWriteToFile()) {
            try {
                String fileName = "VALIDATION_" + ValidationUtils.formatDate(new Date()) + ".txt";
                fileName = fileName.replace(' ', '_');
                fileName = fileName.replace(':', 'h');
                PrintStream out = new PrintStream(fileName);
                System.setOut(out);
            } catch (FileNotFoundException e) {
                ValidationUtils.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    public ValidationProperties getProperties() {
        return properties;
    }

    public void doValidate() throws BusinessException {
        for (int i = 0; i < getAllRtus().size(); i++) {
            Device rtu = getAllRtus().get(i);
            ValidationUtils.log(Level.SEVERE, "[" + (i + 1) + "/" + getAllRtus().size() + "] Validating rtu: " + rtu.getPath() + rtu.getNameSeparator() + rtu.getName());
            validateProfileForRtu(rtu);
        }
        Date before = getProperties().getListDevicesInstalledBeforeDate();
        Date after = getProperties().getListDevicesInstalledAfterDate();
        if ((before != null) && (after != null) && before.after(after)) {
            listDevicesCreatedBetween();
        } else {
            if (before != null) {
                listDevicesCreatedBefore();
            }
            if (after != null) {
                listDevicesCreatedAfter();
            }
        }
    }

    private void listDevicesCreatedBefore() throws BusinessException {
        for (int i = 0; i < getAllRtus().size(); i++) {
            Device rtu = getAllRtus().get(i);
            Date installationDate = ValidationUtils.getInstallationDate(rtu, getProperties().getOnlyFrom());
            Date before = getProperties().getListDevicesInstalledBeforeDate();
            if (installationDate.before(before)) {
                ValidationUtils.log(Level.SEVERE, "[" + "INVALID_CALL_HOME_ID" + "/" + rtu.getName() + "] installed on [" + ValidationUtils.formatDate(installationDate) + "] and before [" + ValidationUtils.formatDate(before) + "]");
            }
        }
    }

    private void listDevicesCreatedAfter() throws BusinessException {
        for (int i = 0; i < getAllRtus().size(); i++) {
            Device rtu = getAllRtus().get(i);
            Date installationDate = ValidationUtils.getInstallationDate(rtu, getProperties().getOnlyFrom());
            Date after = getProperties().getListDevicesInstalledAfterDate();
            if (installationDate.after(after)) {
                ValidationUtils.log(Level.SEVERE, "[" + "INVALID_CALL_HOME_ID" + "/" + rtu.getName() + "] installed on [" + ValidationUtils.formatDate(installationDate) + "] and after [" + ValidationUtils.formatDate(after) + "]");
            }
        }
    }

    private void listDevicesCreatedBetween() throws BusinessException {
        for (int i = 0; i < getAllRtus().size(); i++) {
            Device rtu = getAllRtus().get(i);
            Date installationDate = ValidationUtils.getInstallationDate(rtu, getProperties().getOnlyFrom());
            Date before = getProperties().getListDevicesInstalledBeforeDate();
            Date after = getProperties().getListDevicesInstalledAfterDate();
            if (installationDate.before(before) && installationDate.after(after)) {
                ValidationUtils.log(Level.SEVERE, "[" + "INVALID_CALL_HOME_ID" + "/" + rtu.getName() + "] installed on [" + ValidationUtils.formatDate(installationDate) + "] between [" + ValidationUtils.formatDate(after) + "] and [" + ValidationUtils.formatDate(before) + "]");
            }
        }
    }

    private void validateProfileForRtu(Device rtu) throws BusinessException {
        List<Channel> channels = rtu.getChannels();
        for (Channel channel : channels) {
            if (getProperties().getChannelsToValidate().contains(channel.getLoadProfileIndex())) {
                checkChannel(channel);
            } else {
                ValidationUtils.log(Level.INFO, "Skipping channel: " + channel);
            }
        }
    }

    private void checkChannel(Channel channel) throws BusinessException {
        ValidationUtils.log(Level.INFO, "Validating channel: " + channel);
        List<IntervalRecord> intervalData = null;
        if (getProperties().isValidateInstallationDate()) {
            if (intervalData == null) {
                intervalData = channel.getIntervalData(new Date(0), ValidationUtils.now());
            }
            validateInstallationDate(channel, intervalData);
        }
        if (getProperties().isValidateMissingData()) {
            if (intervalData == null) {
                intervalData = channel.getIntervalData(new Date(0), ValidationUtils.now());
            }
            validateMissingData(channel, intervalData);
        }
        if (getProperties().isValidateWrongData()) {
            if (intervalData == null) {
                intervalData = channel.getIntervalData(new Date(0), ValidationUtils.now());
            }
            validateWrongData(channel, intervalData);
        }
        if (getProperties().isValidateWrongData()) {
            if (intervalData == null) {
                intervalData = channel.getIntervalData(new Date(0), ValidationUtils.now());
            }
            validateWrongData(channel, intervalData);
        }
    }

    /**
     * @param channel
     * @param intervalData
     * @throws BusinessException
     */
    private void validateInstallationDate(Channel channel, List<IntervalRecord> intervalData) throws BusinessException {
        Date installationDate = ValidationUtils.getInstallationDate(channel, getProperties().getOnlyFrom());
        Date firstIntervalDate = ValidationUtils.getFirstIntervalDate(intervalData, getProperties().getOnlyFrom());
        if ((firstIntervalDate != null) && (firstIntervalDate.after(installationDate))) {
            ValidationUtils.log(Level.SEVERE, "No data from 'installationdate' for channel [" + channel + "] [I=" + ValidationUtils.formatDate(installationDate) + "], [F=" + ValidationUtils.formatDate(firstIntervalDate) + "]");
        }
    }

    /**
     * @param channel
     * @param intervalData
     */
    private void validateWrongData(Channel channel, List<IntervalRecord> intervalData) {
        for (IntervalRecord record : intervalData) {
            if (isValidIntervalState(record)) {
                Number value = record.getValue();
                long longValue = value.longValue();
                if (channel.getLoadProfileIndex() == 3) { // Channel P
                    if ((longValue > 10) || (value.doubleValue() <= 0)) {
                        ValidationUtils.log(Level.SEVERE, "Data incorrect [" + value + "] for " + ValidationUtils.formatDate(record.getDate()) + " [" + channel + "]");
                    }
                } else if (channel.getLoadProfileIndex() == 4) { // Channel T
                    if ((longValue > 350) || (longValue < 200)) {
                        ValidationUtils.log(Level.SEVERE, "Data incorrect [" + value + "] for " + ValidationUtils.formatDate(record.getDate()) + " [" + channel + "]");
                    }
                }
            }
        }
    }

    private boolean isValidIntervalState(IntervalRecord record) {
        boolean isNotCorrupted = (record.getIntervalState() & IntervalStateBits.CORRUPTED) == 0x00;
        boolean isNotOther = (record.getIntervalState() & IntervalStateBits.OTHER) == 0x00;
        return isNotCorrupted && isNotOther;
    }

    private void validateMissingData(Channel channel, List<IntervalRecord> intervalData) {
        Date oldestDate = ValidationUtils.getFirstIntervalDate(intervalData, getProperties().getOnlyFrom());
        if (oldestDate != null) {
            ValidationUtils.log(Level.INFO, "First interval = " + ValidationUtils.formatDate(oldestDate));
            Date lastReading = channel.getLastReading();
            while (oldestDate.before(lastReading == null ? ValidationUtils.now() : lastReading)) {
                oldestDate = new Date(oldestDate.getTime() + (channel.getIntervalInSeconds() * 1000));
                if (!ValidationUtils.isIntervalInIntervalData(intervalData, oldestDate)) {
                    ValidationUtils.log(Level.SEVERE, "Data missing for " + ValidationUtils.formatDate(oldestDate) + " [" + channel + "]");
                }
            }
        } else {
            ValidationUtils.log(Level.INFO, "Channel has no profileData");
        }
    }

    public List<Device> getAllRtus() throws BusinessException {
        if (rtus == null) {
            String groupExternalName = getProperties().getGroupExternalName();
            Group group;
            if (groupExternalName == null) {
                group = null;
            } else {
                group = ValidationUtils.getMeteringWarehouse().getGroupFactory().findByExternalName(groupExternalName);
            }
            if (group == null) {
                Folder parentFolder = getParentFolder();
                rtus = ValidationUtils.getMeteringWarehouse().getDeviceFactory().findAllInTree(parentFolder);
            } else {
                rtus = new ArrayList<Device>();
                List groupMembers = group.getMembers();
                for (Object groupMember : groupMembers) {
                    if (groupMember instanceof Device) {
                        rtus.add((Device) groupMember);
                    }
                }
            }
        }
        return rtus;
    }

    private Folder getParentFolder() throws BusinessException {
        if (parentFolder == null) {
            parentFolder = ValidationUtils.getMeteringWarehouse().getFolderFactory().findByExternalName(getProperties().getParentFolderExtName());
            if (parentFolder == null) {
                throw new BusinessException("Unable to find parent folder with external name: " + getProperties().getParentFolderExtName());
            }
        }
        return parentFolder;
    }

    public static void main(String[] args) throws BusinessException {
        ValidationProperties properties = new ValidationProperties("validation.properties");
        ValidateProfile validateProfile = new ValidateProfile(properties);
        validateProfile.doValidate();
    }

}
