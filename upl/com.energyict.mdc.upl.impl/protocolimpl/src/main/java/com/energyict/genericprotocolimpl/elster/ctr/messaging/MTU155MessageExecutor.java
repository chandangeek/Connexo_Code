package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.elster.ctr.RequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.SmsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRFirmwareUpgradeTimeOutException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.OldDeviceMessage;
import com.energyict.mdw.shadow.OldDeviceMessageShadow;
import com.energyict.protocol.MessageEntry;

import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 3-dec-2010
 * Time: 14:41:00
 */
public class MTU155MessageExecutor extends GenericMessageExecutor {

    private Logger logger;
    private RequestFactory factory;
    private Device rtu;
    private StoreObject storeObject;

    public MTU155MessageExecutor(Logger logger, RequestFactory factory, Device rtu, StoreObject storeObject) {
        this.factory = factory;
        this.logger = logger;
        this.rtu = rtu;
        this.storeObject = storeObject;
    }

    @Override
    public void doMessage(OldDeviceMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        boolean pending = false;
        String timeOutMsg = "";
        try {
            String content = rtuMessage.getContents();
            String trackingId = rtuMessage.getTrackingId();
            MessageEntry messageEntry = new MessageEntry(content, trackingId);

            AbstractMTU155Message[] messages = new AbstractMTU155Message[]{
                    // Device configuration group
                    new WriteConverterMasterDataMessage(this),
                    new WriteMeterMasterDataMessage(this),
                    new WriteGasParametersMessage(this),
                    new ChangeDSTMessage(this),
                    new WritePDRMessage(this),

                    // Connectivity setup group
                    new DevicePhoneNumberSetupMessage(this),
                    new ApnSetupMessage(this),
                    new SMSCenterSetupMessage(this),

                    // Key management
                    new ActivateTemporaryKeyMessage(this),
                    new ChangeExecutionKeyMessage(this),
                    new ChangeTemporaryKeyMessage(this),

                    // Seals management group
                    new TemporaryBreakSealMessage(this),
                    new ChangeSealStatusMessage(this),

                    // Tariff management
                    new TariffUploadPassiveMessage(this),
                    new TariffDisablePassiveMessage(this),

                    // Maintenance group
                    new WakeUpFrequency(this),
                    new ForceSyncClockMessage(this),
                    new ReadPartialProfileDataMessage(this),

                    // Firmware Upgrade
                    new MTU155FirmwareUpgradeMessage(this)
            };

            boolean messageFound = false;
            for (AbstractMTU155Message message : messages) {
                if (message.canExecuteThisMessage(messageEntry)) {
                    messageFound = true;
                    message.executeMessage(messageEntry);
                    success = true;
                    break;
                }
            }
            if (!messageFound) {
                throw new BusinessException("Received unknown message: " + rtuMessage.toString());
            }
        } catch(CTRFirmwareUpgradeTimeOutException timeOut) {
            timeOutMsg = timeOut.getMessage();
            pending = true;
        } finally {
            if (success) {
                if (getFactory() instanceof SmsRequestFactory) {
                    rtuMessage.setSent();
                } else {
                    rtuMessage.confirm();
                }
                getLogger().info("Message " + rtuMessage.displayString() + " has finished successfully.");
            } else if (pending) {
                // Add "PendingUpgrade" to the message TrackingId.
                // This to ensure next time the message is executed, we can easily recontinue the pending firmware instead of starting a new one.
                OldDeviceMessageShadow shadow = rtuMessage.getShadow();
                if (!shadow.getTrackingId().contains("PendingUpgrade")) {
                    shadow.setTrackingId(shadow.getTrackingId() + " PendingUpgrade");
                    rtuMessage.update(shadow);
                }
                getLogger().log(Level.INFO, timeOutMsg + " - The firmware upgrade process will continue next communication session.");
                rtuMessage.setPending();
            } else{
                rtuMessage.setFailed();
                getLogger().info("Message " + rtuMessage.displayString() + " has failed.");
            }
        }
    }


    @Override
    protected TimeZone getTimeZone() {
        return getFactory().getTimeZone();
    }

    public RequestFactory getFactory() {
        return factory;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Device getRtu() {
        return rtu;
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }
}
