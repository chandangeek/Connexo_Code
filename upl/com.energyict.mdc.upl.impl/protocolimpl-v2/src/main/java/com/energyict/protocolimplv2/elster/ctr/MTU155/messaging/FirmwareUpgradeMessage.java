//package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;
//
//import com.energyict.cpo.Environment;
//import com.energyict.mdc.messages.DeviceMessageStatus;
//import com.energyict.mdc.meterdata.CollectedMessage;
//import com.energyict.mdc.meterdata.ResultType;
//import com.energyict.mdw.offline.OfflineDeviceMessage;
//import com.energyict.protocol.MessageEntry;
//import com.energyict.protocol.ProtocolUtils;
//import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
//import com.energyict.protocolimplv2.MdcManager;
//import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
//import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Identify;
//import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Segment;
//import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
//import org.xml.sax.SAXException;
//
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.logging.Level;
//
///**
//* Copyrights EnergyICT
//* Date: 7/10/11
//* Time: 13:18
//*/
//public class FirmwareUpgradeMessage extends AbstractMTU155Message {
//
//    private Identify newSoftwareIdentifier;         // Version number of the new firmware
//    private Calendar activationDate;                // Activation date of the new firmware
//    private byte[] firmwareUpgradeFile = null;      // the Firmware File
//
//    private int currentFirmwareVersion;             // The current Firmware version (before the upgrade)
//    private int numberOfTotalSegments;              // Then number of segments needed to transfer the whole firmware image.
//    private Segment lastAckedSegment = new Segment(0);               /** If there was already an upgrade pending, this indicates the last segment successful sent to the MTU155
//                                                        -> This indicates the last correctly received segment and guarantees that all previous segments have been correctly received and recorded. **/
//
//    private boolean isPendingMessage = false;       // False: This message has never before been executed. This message should init a new firmware upgrade.
//                                                    // True: The message has been executed before, but was set in state 'Pending'. This indicates the message should continue previous work.
//
//    private SealConfig sealConfig;
//
//    public FirmwareUpgradeMessage(Messaging messaging) {
//        super(messaging);
//        sealConfig = new SealConfig(getFactory());
//    }
//
//
//    @Override
//    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
//        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE.getPrimaryKey().getValue());
//    }
//
//    @Override
//    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
//        CollectedMessage collectedMessage = createCollectedMessage(message);
//        String base64Firmware = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
//        int firmwareVersion  = Integer.parseInt(message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue(), 16);
//        Date activationDate = new Date (Long.parseLong(message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue()));
//
//        try {
//            validateApnSetupParameters(collectedMessage, apn, user, pssw);
//            writeApnSetup(apn, user, pssw);
//        } catch (CTRException e) {
//            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
//            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
//            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
//        }
//        return collectedMessage;
//
//        //TODO: CTRFirmwareUpgradeTimeOutException
//
//        // 1. Extract al info from the messageEntry
//        extractMessageInfo(messageEntry);
//        loadUserFile(messageEntry);
//
//        // Check if this is the first time the message is executed.
//        isPendingMessage = messageEntry.getTrackingId().contains("PendingUpgrade");
//
//        // Before starting an upgrade, the 'download' seal must be broken.
//        breakDownloadSeal();
//
//        // 2. Query the status of the MTU155 to determine the upgrade mode
//        int upgradeMode = CheckUpgradeMode();
//        /** Supported modes
//         * 0: do not upgrade
//         * 1: init a new upgrade
//         * 2: continue with pending upgrade
//         * 3: complete image sent/busy verifying the image
//         * 4: upgrade ready for activation
//         * 5: Firmware upgrade was successful. We can confirm the message. We also reactivate the firmware upgrade seal.
//         **/
//        switch (upgradeMode) {
//            case 1:
//                lastAckedSegment = new Segment(0);
//                doInitFirmwareUpgrade();
//                // break;   -- Do not break, but go to step 2.
//            case 2:
//                doUpgrade();    // This will throw CTRFirmwareUpgradeTimeOutExceptions when the meter goes offline.
//                // break;   -- Do not break, but go to step 3.
//            case 3:
//                // Nothing to do - we must wait until the device has verified the firmware image.
//                throw new CTRFirmwareUpgradeTimeOutException("The device is verifying the firmware image.");
//            case 4:
//                 // Nothing to do - we must wait until the device has activated the new image.
//                throw new CTRFirmwareUpgradeTimeOutException("New firmware version will be activated on "+getActivationDate()+".");
//            case 5:
//                restoreDownloadSeal();
//                break;
//            default:
//                super.getLogger().log(Level.SEVERE, "Download will be aborted.");
//                throw new BusinessException("Unexpected state of 'status_DL' attribute of tge 'Download Parameters' object.");
//        }
//    }
//
//    /** Extract SoftwareIdentifier and Activation Date from the messageEntry;
//     * The Activation Date will be loaded from the field 'TrackingId'
//     * Format of TrackingId field: <integer: version number> <Date: yy/MM/DD>
//     * Example: '201 31/12/11'  - Firmware version 201 who must become active on 31 dec 2011
//     **/
//    private void extractMessageInfo(MessageEntry messageEntry) throws BusinessException {
//        try {
//            super.getLogger().info("Loading the software identifier and the activation date.");
//            String[] token = messageEntry.getTrackingId().trim().split(" ");
//            if (token.length == 3 && token[2].contains("PendingUpgrade")) {
//                    isPendingMessage = true;
//            }
//            newSoftwareIdentifier = new Identify(Integer.parseInt(token[0], 16));
//            String sDate = token[1];
//            activationDate = Calendar.getInstance();
//            DateFormat dateformat = new SimpleDateFormat("dd/MM/yy");
//            activationDate.setTime(dateformat.parse(sDate));
//        } catch (ParseException e) {
//            String errorMessage = "Failed to extract the Software Identifier and Activation Date from the TrackingId.";
//            super.getLogger().log(Level.WARNING, errorMessage);
//            throw new BusinessException(errorMessage);
//        } catch (ArrayIndexOutOfBoundsException e) {
//            String errorMessage = "Failed to extract the Software Identifier and Activation Date from the TrackingId.";
//            super.getLogger().log(Level.WARNING, errorMessage);
//            throw new BusinessException(errorMessage);
//        }
//    }
//
//    private void loadUserFile(MessageEntry messageEntry) throws BusinessException {
//        String errorMessage = "";
//        super.getLogger().info("Loading the Firmware file.");
//        final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder();
//
//        try {
//            builder.initFromXml(messageEntry.getContent());
//        } catch (final IOException e) {
//            if (super.getLogger().isLoggable(Level.SEVERE)) {
//                super.getLogger().log(Level.SEVERE, errorMessage, e);
//            }
//            errorMessage = "Got an IO error when loading firmware message content [" + e.getMessage() + "]";
//            super.getLogger().log(Level.SEVERE, errorMessage, e);
//            throw new BusinessException(errorMessage + e.getMessage());
//        } catch (SAXException e) {
//            errorMessage = "Cannot process firmware upgrade message due to an XML parsing error [" + e.getMessage() + "]";
//            super.getLogger().log(Level.SEVERE, errorMessage, e);
//            throw new BusinessException(errorMessage + e.getMessage());
//        }
//
//		if (builder.getUserFile() != null) {
//            this.firmwareUpgradeFile = builder.getUserFile().loadFileInByteArray();
//			if (this.firmwareUpgradeFile.length == 0) {
//			    errorMessage = "The firmware image file is empty.";
//                if (super.getLogger().isLoggable(Level.WARNING)) {
//				    super.getLogger().log(Level.WARNING, errorMessage);
//				}
//				throw new BusinessException(errorMessage);
//			}
//            this.numberOfTotalSegments = (int) Math.ceil((double) firmwareUpgradeFile.length / (double) (useLongFrameFormat() ? GprsRequestFactory.LENGTH_CODE_PER_REQUEST_LONG_FRAMES : GprsRequestFactory.LENGTH_CODE_PER_REQUEST_SHORT_FRAMES));
//		} else {
//		    errorMessage = "The message did not contain a user file to use for the upgrade, message fails.";
//            super.getLogger().log(Level.WARNING, errorMessage);
//		    throw new BusinessException(errorMessage);
//		}
//    }
//
//    private int CheckUpgradeMode() throws BusinessException {
//        try {
//            //A. If this is a new message - check if the firmware should/can be upgraded.
//
//            // 1. Ask for the current active Firmware version of the MTU155. This can be done by reading out the 'Software Identifiers' object.
//            currentFirmwareVersion = super.getFactory().queryRegister("9.2.5").getValue(0).getIntValue();
//            if (!isPendingMessage) {
//                // 2. Check if currentFirmwareVersion > 0x75
//                if (currentFirmwareVersion < 0x75) {
//                    String errorMessage = "The device firmware version (" + Integer.toHexString(currentFirmwareVersion) + ") does not support Firmware Upgrade messages.";
//                    super.getLogger().log(Level.SEVERE, errorMessage);
//                    throw new BusinessException(errorMessage);
//                }
//
//                // 3. check if firmware version of upgrade message equals the version present in MTU1555 > we consider the firmware upgrade as successful.
//                if (currentFirmwareVersion == newSoftwareIdentifier.getIdentify()) {
//                        super.getLogger().log(Level.WARNING, "The device has already firmware version " + Integer.toHexString(currentFirmwareVersion) + " equipped. An upgrade is not necessary.");
//                    return 5;
//                }
//
//                // 4. Check if firmware version of upgrade message is higher than the active version. If not the case we let the firmware upgrade message fail.
//                if (currentFirmwareVersion > newSoftwareIdentifier.getIdentify()) {
//                    String errorMessage = "The device firmware version " + Integer.toHexString(currentFirmwareVersion) + " is higher than the version of the upgrade (" + newSoftwareIdentifier.getHexIdentify() + "). The firmware will not be upgraded.";
//                    super.getLogger().log(Level.SEVERE, errorMessage);
//                    throw new BusinessException(errorMessage);
//                }
//
//                // 5. if the current firmware version is 0x75, the activation dates day of month should be lower than 25. (This due to a firmware bug).
//                if (currentFirmwareVersion == 0x75) {
//                    Calendar calActivateNextGasDay = Calendar.getInstance();
//                    Calendar calActivateImmediately = Calendar.getInstance();
//
//                    try {
//                        DateFormat dateformat = new SimpleDateFormat("dd/MM/yy");
//                        calActivateImmediately.setTime(dateformat.parse("01/01/01"));
//                        int year = Calendar.getInstance().get(Calendar.YEAR) - 2000;
//                        calActivateNextGasDay.setTime(dateformat.parse("01/01/" + Integer.toString(year)));
//                    } catch (ParseException e) {
//                        throw new BusinessException();
//                    }
//
//                    if ((activationDate.getTimeInMillis() != calActivateImmediately.getTimeInMillis()) && (activationDate.getTimeInMillis() != calActivateNextGasDay.getTimeInMillis())) {
//                        Calendar currentTime = Calendar.getInstance();
//                        if (activationDate.get(Calendar.DAY_OF_MONTH) >= 25) {
//                            String errorMessage = "For devices with firmware version 075 the activation dates day of month should be below the 25th.";
//                            super.getLogger().log(Level.SEVERE, errorMessage);
//                            throw new BusinessException(errorMessage);
//                        }
//                        if ((activationDate.get(Calendar.MONTH) != currentTime.get(Calendar.MONTH)) || (activationDate.get(Calendar.YEAR) != currentTime.get(Calendar.YEAR))) {
//                            String errorMessage = "For devices with firmware version 075 the activation date must have the same month and same year in which the activation command is sent (current date).";
//                            super.getLogger().log(Level.SEVERE, errorMessage);
//                            throw new BusinessException(errorMessage);
//                        }
//                    }
//                }
//                super.getLogger().log(Level.INFO, "No download pending - will init a new download.");
//                return 1;
//            }
//
//            // B. Pending download found
//            // 1. Check if firmware process has finished.
//            if (currentFirmwareVersion == newSoftwareIdentifier.getIdentify()) {
//                super.getLogger().log(Level.INFO, "The device firmware version has been upgraded to version " + newSoftwareIdentifier.getHexIdentify() + ".");
//                return 5;
//            }
//
//            // 2. Check the 'Download Parameters' object of the MTU155 to determine the state of the already pending upgrade.
//            CTRAbstractValue[] downloadParameters = super.getFactory().queryRegister("9.5.0").getValue();
//            int statusOfDownload = downloadParameters[0].getIntValue();
//            Identify identifier = new Identify(downloadParameters[1].getBytes());
//            lastAckedSegment = new Segment(downloadParameters[6].getIntValue());
//
//            if (identifier.getIdentify() != newSoftwareIdentifier.getIdentify()) {
//                super.getLogger().log(Level.INFO, "Pending download found, but didn't matched the firmware version of upgrade. Will initialize a new download.");
//                return 1;
//            }
//
//            if (statusOfDownload == 0 || identifier.getIdentify() == 0) {
//                // No download pending > a new Download can start
//                super.getLogger().log(Level.INFO, "No download pending - will init a new download.");
//                return 1;
//            }
//            if (statusOfDownload == 1) {
//                // There is already a download pending.
//                super.getLogger().log(Level.INFO, "Pending download found - will resume the pending download");
//                return 2;
//            }
//            if (statusOfDownload == 2) {
//                // There is already a complete firmware image sent to the MTU155. The image is in verification state.
//                super.getLogger().log(Level.INFO, "The complete image has been sent to the device. The device is busy verifying the received image.");
//                return 3;
//            }
//            if (statusOfDownload == 3) {
//                // There is already a complete firmware image sent to the MTU155. The image is awaiting activation.
//                super.getLogger().log(Level.INFO, "The complete image has been sent to the device. The image is ready to be activated.");
//                return 4;
//            }
//            if (statusOfDownload == 4) {
//                String msg = "The firmware image (" + identifier.getHexIdentify() + ") could not be verified by the MTU155. The firmware upgrade will fail.";
//                super.getLogger().log(Level.INFO, msg);
//                throw new BusinessException(msg);
//            }
//
//            return 0;   // We should never come here.
//        } catch (CTRException e) {
//            super.getLogger().log(Level.SEVERE, e.getMessage());
//            throw new BusinessException(e);
//        }
//    }
//
//    private String getActivationDate() throws BusinessException {
//        // Read out the 'Software identifiers' object
//        // This object holds the software identifiers for the Metering unit which is active/ is to become active, together with the date of activation.
//        try {
//            byte[] bytes = super.getFactory().queryRegister("9.2.5").getValue(2).getBytes();
//            Calendar cal = ProtocolUtils.getCleanCalendar(getFactory().getTimeZone());
//            cal.set(Calendar.YEAR, (2000 + (int) bytes[0]));
//            cal.set(Calendar.MONTH, (int) bytes[1] -1);
//            cal.set(Calendar.DAY_OF_MONTH, (int) bytes[2]);
//            return cal.getTime().toString();
//        } catch (CTRException e) {
//            throw new BusinessException(e);
//        }
//    }
//
//    /**
//     * Before starting the download process, the 'download seal' must be deactivated.
//     */
//    private void breakDownloadSeal() throws BusinessException {
//        try {
//            sealConfig.breakSealPermanent(SealStatusBit.DOWNLOAD_PROGRAM);
//        } catch (CTRException e) {
//            throw new BusinessException(e);
//        }
//    }
//
//    /**
//     * After a complete download process, the 'download seal' must be reactivated.
//     */
//    private void restoreDownloadSeal() throws BusinessException {
//        try {
//            sealConfig.restoreSeal(SealStatusBit.DOWNLOAD_PROGRAM);
//        } catch (CTRException e) {
//            throw new BusinessException(e);
//        }
//    }
//
//    /**
//     * Do the first Initialization step to initialize the download of new firmware
//     */
//    private void doInitFirmwareUpgrade() throws BusinessException {
//        try {
//            super.getFactory().doInitFirmwareUpgrade(newSoftwareIdentifier, null, null, activationDate, firmwareUpgradeFile.length, useLongFrameFormat());
//        } catch (CTRException e) {
//            super.getLogger().log(Level.SEVERE, e.getMessage());
//            throw new BusinessException(e.getMessage());
//        }
//    }
//
//    /**
//     * Continue an existing download process by sending code segments, until the MTU155 goes back offline
//     */
//    private void doUpgrade() throws BusinessException, CTRFirmwareUpgradeTimeOutException {
//        int retryCount = 0;
//
//        try {
//            // Loop over all segments & send them out - 1 by 1.
//            while (lastAckedSegment.getSegment() < (numberOfTotalSegments)) {
//                if (retryCount > 5) {
//                    throw new CTRException("Failed to send image to the MTU155. Received 5 times an NACK.");
//                }
//                boolean success = super.getFactory().doSendFirmwareSegment(newSoftwareIdentifier, firmwareUpgradeFile, lastAckedSegment, useLongFrameFormat());
//                if (success) {
//                    lastAckedSegment.setSegment(lastAckedSegment.getSegment() +1);
//                    retryCount = 0;
//                } else {
//                    retryCount += 1;
//                }
//            }
//        } catch (CTRConnectionException e) {
//            throw new CTRFirmwareUpgradeTimeOutException("Got an CTRException [" + e.getMessage() + "] while sending firmware image segments to the MTU155 - " +
//                    lastAckedSegment.getSegment() + " out of " + numberOfTotalSegments + " segments are already send out to the device.");
//        } catch (CTRException e) {
//            throw new BusinessException(e);
//        }
//    }
//
//    private boolean useLongFrameFormat() {
//        return getRtu().getProperties().getProperty(MTU155Properties.USE_LONG_FRAME_FORMAT, MTU155Properties.DEFAULT_USE_LONG_FRAME_FORMAT).equalsIgnoreCase("1");
//    }
//}
