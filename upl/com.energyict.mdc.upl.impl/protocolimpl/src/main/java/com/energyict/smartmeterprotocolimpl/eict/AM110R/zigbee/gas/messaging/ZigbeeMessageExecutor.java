package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging.xml.XMLParser;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ObisCodeProvider;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2011
 * Time: 15:02:34
 */
public class ZigbeeMessageExecutor extends MessageParser {

    private static final List<ObisCode> allowedTimeOfUseObjects = new ArrayList<ObisCode>();
    private static final List<ObisCode> allowedPriceMatrixObjects = new ArrayList<ObisCode>();

    static {
        allowedTimeOfUseObjects.add(ObisCodeProvider.ActivityCalendarObisCode);
        allowedTimeOfUseObjects.add(ObisCodeProvider.PassiveSpecialDayObisCode);
        allowedTimeOfUseObjects.add(ObisCodeProvider.PassiveScriptTableObisCode);
        allowedTimeOfUseObjects.add(ObisCodeProvider.TariffRateLabelObisCode);
        allowedTimeOfUseObjects.add(ObisCodeProvider.TariffInformationObisCode);
        allowedTimeOfUseObjects.add(ObisCodeProvider.BlockTariffObisCode);

        allowedPriceMatrixObjects.add(ObisCodeProvider.PriceMatrix);
    }

    private static final String NORESUME = "noresume";

    private final AbstractSmartDlmsProtocol protocol;
    private ActivityCalendarController activityCalendarController;

    private boolean success;

    public ZigbeeMessageExecutor(final AbstractSmartDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getDlmsSession().getCosemObjectFactory();
    }

    private DlmsSession getDlmsSession() {
        return getProtocol().getDlmsSession();
    }

    public AbstractSmartDlmsProtocol getProtocol() {
        return this.protocol;
    }

    public MessageResult executeMessageEntry(final MessageEntry messageEntry) {
        String content = messageEntry.getContent();
        success = true;

        try {
            if (isTimeOfUseMessage(content)) {
                updateTimeOfUse(messageEntry);
            } else if (isSendNewPriceMatrixMessage(content)) {
                  sendNewPriceMatrix(messageEntry);
              } else if (isSetStandingCharge(content)) {
                  setStandingCharge(messageEntry);
              } else if (isSetCurrency(content)) {
                  setCurrency(messageEntry);
            } else if (isChangeOfSupplier(content)) {
                changeOfTenantOrSupplier(content, ObisCodeProvider.ChangeOfSupplier);
            } else if (isChangeOfTenant(content)) {
                changeOfTenantOrSupplier(content, ObisCodeProvider.ChangeOfTenant);
            } else if (isSetCF(content)) {
                setCF(content);
            } else if (isSetCV(content)) {
                setCV(content);
            } else if (isFirmwareUpgradeMessage(content)) {
                updateFirmware(messageEntry);
            }
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (InterruptedException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        }

        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void updateTimeOfUse(final MessageEntry messageEntry) throws IOException {
        try {
            getLogger().log(Level.INFO, "Send new TOU tariff message received.");
            String xmlData = getIncludedContent(messageEntry.getContent());
            XMLParser parser = new XMLParser(getProtocol());

            parser.parseXML(xmlData, allowedTimeOfUseObjects);
            List<Object[]> parsedObjects = parser.getParsedObjects();

            boolean encounteredError = false;
            getLogger().info("Transferring the TOU tariff objects to the device.");
            for (Object[] each : parsedObjects) {
                AbstractDataType value = (AbstractDataType) each[1];
                if (each[0].getClass().equals(GenericWrite.class)) {
                    GenericWrite genericWrite = (GenericWrite) each[0];
                    try {
                        genericWrite.write(value.getBEREncodedByteArray());
                    } catch (DataAccessResultException e) {
                        encounteredError = true;
                        getLogger().severe("ERROR: Failed to write DLMS object " + genericWrite.getObjectReference() + " , attribute " + genericWrite.getAttr() + " : " + e);
                    }
                } else if (each[0].getClass().equals(GenericInvoke.class)) {
                    GenericInvoke genericInvoke = (GenericInvoke) each[0];
                    try {
                        genericInvoke.invoke(value.getBEREncodedByteArray());
                    } catch (DataAccessResultException e) {
                        encounteredError = true;
                        getLogger().severe("ERROR: Failed to execute action on DLMS object " + genericInvoke.getObjectReference() + " , method " + genericInvoke.getMethod() + " : " + e);
                    }
                }
            }

            getLogger().log(Level.INFO, "Send new TOU tariff message finished.");
            if (encounteredError) {
                throw new IOException("Send new TOU tariff message failed: Failed to write one or more DLMS objects.");
            }
        } catch (IOException e) {
            throw new NestedIOException(e, "Send new TOU tariff message failed: " + e.getMessage());
        }
    }

    private void sendNewPriceMatrix(MessageEntry messageEntry) throws IOException {
        try {
            getLogger().log(Level.INFO, "Send new price matrix message received.");
            String xmlData = getIncludedContent(messageEntry.getContent());
            XMLParser parser = new XMLParser(getProtocol());

            parser.parseXML(xmlData, allowedPriceMatrixObjects);
            List<Object[]> parsedObjects = parser.getParsedObjects();

            boolean encounteredError = false;
            getLogger().info("Transferring the price matrix objects to the device.");
            for (Object[] each : parsedObjects) {
                AbstractDataType value = (AbstractDataType) each[1];
                if (each[0].getClass().equals(GenericWrite.class)) {
                    GenericWrite genericWrite = (GenericWrite) each[0];
                    try {
                        genericWrite.write(value.getBEREncodedByteArray());
                    } catch (DataAccessResultException e) {
                        encounteredError = true;
                        getLogger().severe("ERROR: Failed to write DLMS object " + genericWrite.getObjectReference() + " , attribute " + genericWrite.getAttr() + " : " + e);
                    }
                } else if (each[0].getClass().equals(GenericInvoke.class)) {
                    GenericInvoke genericInvoke = (GenericInvoke) each[0];
                    try {
                        genericInvoke.invoke(value.getBEREncodedByteArray());
                    } catch (DataAccessResultException e) {
                        encounteredError = true;
                        getLogger().severe("ERROR: Failed to execute action on DLMS object " + genericInvoke.getObjectReference() + " , method " + genericInvoke.getMethod() + " : " + e);
                    }
                }
            }

            getLogger().log(Level.INFO, "Send new price matrix message finished.");
            if (encounteredError) {
                throw new IOException("Send new price matrix message failed: Failed to write one or more DLMS objects.");
            }
        } catch (IOException e) {
            throw new NestedIOException(e, "Send new price matrix message failed: " + e.getMessage());
        }
    }

    private void setStandingCharge(MessageEntry messageEntry) throws IOException {
        getLogger().log(Level.INFO, "Set standing charge message received.");

        ActivePassive standingChargeActivePassive = getCosemObjectFactory().getActivePassive(ObisCodeProvider.StandingCharge);
        int standingChargeValue;
        try {
            standingChargeValue = Integer.parseInt(getValueFromXMLAttribute(RtuMessageConstant.STANDING_CHARGE, messageEntry.getContent()));
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing the new standing charge value: " + e.getMessage());
        }

        Date activationDate = null;
        String activationDateString = getValueFromXMLAttribute(RtuMessageConstant.ACTIVATION_DATE, messageEntry.getContent());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the activation date: " + e.getMessage());
        }

        log(Level.FINEST, "Writing new standing charge value");
        standingChargeActivePassive.writePassiveScalerUnit(new ScalerUnit(-5, 10)); // Local currency - scale -5
        standingChargeActivePassive.writePassiveValue(new Unsigned32(standingChargeValue));
        if (activationDate != null) {
            log(Level.FINEST, "Writing new activation time");
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            standingChargeActivePassive.writeActivationDate(new DateTime(cal));
        } else {
            log(Level.FINEST, "No activation date specified, the changes will be activated immediately.");
            standingChargeActivePassive.activate();
        }
        getLogger().log(Level.INFO, "Set standing charge message finished.");
    }

    private void setCurrency(MessageEntry messageEntry) throws IOException {
        getLogger().log(Level.INFO, "Set currency message received.");

        ActivePassive currencyActivePassive = getCosemObjectFactory().getActivePassive(ObisCodeProvider.Currency);
        int currencyValue;
        try {
            currencyValue = Integer.parseInt(getValueFromXMLAttribute(RtuMessageConstant.CURRENCY, messageEntry.getContent()));
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing the new currency value: " + e.getMessage());
        }

        Date activationDate = null;
        String activationDateString = getValueFromXMLAttribute(RtuMessageConstant.ACTIVATION_DATE, messageEntry.getContent());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the activation date: " + e.getMessage());
        }

        log(Level.FINEST, "Writing new currency value");
        currencyActivePassive.writePassiveScalerUnit(new ScalerUnit(-3, 10));   // Local currency, scale -3
        currencyActivePassive.writePassiveValue(new Unsigned32(currencyValue));
        if (activationDate != null) {
            log(Level.FINEST, "Writing new activation time");
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            currencyActivePassive.writeActivationDate(new DateTime(cal));
        } else {
            log(Level.FINEST, "No activation date specified, the changes will be activated immediately.");
            currencyActivePassive.activate();
        }
        getLogger().log(Level.INFO, "Set currency message finished.");
    }

    private void changeOfTenantOrSupplier(final String content, ObisCode activePassiveObisCode) throws IOException {
        log(Level.INFO, "Change of tenancy or supplier message received.");
        String tenantReference;
        String supplierReference;
        int supplierId;
        Array scriptsToExecute = new Array();
        Date activationDate = null;

        tenantReference = getValueFromXMLAttribute(RtuMessageConstant.TENANT_REFERENCE, content);
        supplierReference = getValueFromXMLAttribute(RtuMessageConstant.SUPPLIER_REFERENCE, content);

        try {
            supplierId = Integer.parseInt(getValueFromXMLAttribute(RtuMessageConstant.SUPPLIER_ID, content));
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing the new supplier ID: " + e.getMessage());
        }

        String activationDateString = getValueFromXMLAttribute(RtuMessageConstant.ACTIVATION_DATE, content);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
                activationDate.setTime(activationDate.getTime() - (activationDate.getTime() % 60000));  // remove the seconds & milliseconds
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the activation date: " + e.getMessage());
        }

        String scriptsToExecuteString = getValueFromXMLAttribute(RtuMessageConstant.SCRIPT_EXECUTED, content);
        try {
            String[] scriptNumbers = scriptsToExecuteString.split(",");
            for (String scriptNumber : scriptNumbers) {
                int script = Integer.parseInt(scriptNumber.trim());

                Structure structure = new Structure();
                structure.addDataType(new OctetString(ObisCodeProvider.COTSPredefinedScriptTable.getLN()));
                structure.addDataType(new Unsigned16(script));
                scriptsToExecute.addDataType(structure);
            }
        } catch (NumberFormatException e) {
            protocol.getLogger().log(Level.SEVERE, "Error parsing the script executed: " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        ChangeOfTenancyOrSupplierManagement tenancyOrSupplierManagement = getCosemObjectFactory().getChangeOfTenancyOrSupplierManagement(activePassiveObisCode);
        log(Level.FINEST, "Writing new tenant reference");
        tenancyOrSupplierManagement.writePassiveTenantReference(OctetString.fromString(tenantReference));
        log(Level.FINEST, "Writing new supplier reference");
        tenancyOrSupplierManagement.writePassiveSupplierReference(OctetString.fromString(supplierReference));
        log(Level.FINEST, "Writing new supplier ID");
        tenancyOrSupplierManagement.writePassiveSupplierId(new Unsigned16(supplierId));
        log(Level.FINEST, "Writing new script executed");
        tenancyOrSupplierManagement.writePassiveScriptExecuted(scriptsToExecute);

        if (activationDate != null) {
            log(Level.FINEST, "Writing new activation time");
            tenancyOrSupplierManagement.writePassiveStartTime(new AXDRDateTime(activationDate));
            tenancyOrSupplierManagement.writeActivationDate(new AXDRDateTime(activationDate));
        } else {
            log(Level.FINEST, "No activation date specified, the changes will be activated immediately.");
            activationDate = Calendar.getInstance(protocol.getTimeZone()).getTime();
            activationDate.setTime(activationDate.getTime() - (activationDate.getTime() % 60000));  //remove the seconds & milliseconds
            tenancyOrSupplierManagement.writePassiveStartTime(new AXDRDateTime(activationDate));
            tenancyOrSupplierManagement.activate();
        }
        log(Level.INFO, "Change of tenancy or supplier message finished.");
    }

    private void setCF(String content) throws IOException {
        log(Level.INFO, "Set conversion factor message received.");
        int conversionFactorValue;
        try {
            conversionFactorValue = Integer.parseInt(getValueFromXMLAttribute(RtuMessageConstant.CONVERSION_FACTOR, content));
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing the new conversion factor: " + e.getMessage());
        }

        Date activationDate = null;
        String activationDateString = getValueFromXMLAttribute(RtuMessageConstant.ACTIVATION_DATE, content);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the activation date: " + e.getMessage());
        }

        ActivePassive conversionFactor = getCosemObjectFactory().getActivePassive(ObisCodeProvider.ConversionFactor);
        log(Level.FINEST, "Writing new conversion factor");
        conversionFactor.writePassiveScalerUnit(new ScalerUnit(-3, 255)); // unitless - scale -3
        conversionFactor.writePassiveValue(new Unsigned32(conversionFactorValue));         //Double long, signed
        if (activationDate != null) {
            log(Level.FINEST, "Writing new activation time");
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            conversionFactor.writeActivationDate(new DateTime(cal));
        } else {
            log(Level.FINEST, "No activation date specified, the changes will be activated immediately.");
            conversionFactor.activate();
        }
        log(Level.INFO, "Set conversion factor message finished.");
    }

    private void setCV(String content) throws IOException {
        log(Level.INFO, "Set calorific value message received.");
        int calorificValue;
        try {
            calorificValue = Integer.parseInt(getValueFromXMLAttribute(RtuMessageConstant.CALORIFIC_VALUE, content));
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }

        Date activationDate = null;
        String activationDateString = getValueFromXMLAttribute(RtuMessageConstant.ACTIVATION_DATE, content);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the given date: " + e.getMessage());
        }

        ActivePassive calorificValueObject = getCosemObjectFactory().getActivePassive(ObisCodeProvider.CalorificValue);
        calorificValueObject.writePassiveScalerUnit(new ScalerUnit(3, 61)); // Joule per normal cubic meter - scale 3
        calorificValueObject.writePassiveValue(new Unsigned32(calorificValue));         //Double long, signed
        if (activationDate != null) {
            log(Level.FINEST, "Writing new activation time");
            Calendar cal = Calendar.getInstance(protocol.getTimeZone());
            cal.setTime(activationDate);
            calorificValueObject.writeActivationDate(new DateTime(cal));
        } else {
            log(Level.FINEST, "No activation date specified, the changes will be activated immediately.");
            calorificValueObject.activate();
        }
        log(Level.INFO, "Set calorific value message finished.");
    }


    private void updateFirmware(MessageEntry messageEntry) throws IOException, InterruptedException {
        log(Level.INFO, "Upgrade firmware message received.");
        String userFileContent = getIncludedContent(messageEntry.getContent());

        Date activationDate = null;
        String activationDateString = getValueFromXMLTag(RtuMessageConstant.ACTIVATION_DATE, messageEntry.getContent());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (!activationDateString.equalsIgnoreCase("")) {
                activationDate = formatter.parse(activationDateString);
            }
        } catch (ParseException e) {
            throw new IOException("Error parsing the activation date: " + e.getMessage());
        }

        byte[] imageData = new Base64EncoderDecoder().decode(userFileContent);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        if (messageEntry.getTrackingId() != null && !messageEntry.getTrackingId().toLowerCase().contains(NORESUME)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber);
            }
        }
        it.setUsePollingVerifyAndActivate(true);
        it.upgrade(imageData, false);
        if (activationDate != null && activationDate.after(new Date())) {
            log(Level.INFO, "Writing the upgrade activation date.");
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(ObisCodeProvider.ImageActivationScheduler);
            Array dateArray = convertUnixToDateTimeArray(String.valueOf(activationDate.getTime() / 1000));
            sas.writeExecutionTime(dateArray);
        } else {
            log(Level.INFO, "Immediately activating the image.");
            it.imageActivation();
        }
        log(Level.INFO, "Upgrade firmware message finished.");
    }

    private String getValueFromXMLTag(String tag, String content) {
        int startIndex = content.indexOf("<" + tag);
        if (startIndex == -1) {
            return "";  // Optional value is not specified
        }
        int endIndex = content.indexOf("</" + tag);
        try {
            return content.substring(startIndex + tag.length() + 2, endIndex);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private String getValueFromXMLAttribute(String tag, String content) throws IOException {
        int startIndex, endIndex;
        startIndex = content.indexOf(tag + "=\"");
        if (startIndex == -1) {
            return "";  // Optional value is not specified
        }
        endIndex = content.indexOf("\"", startIndex + tag.length() + 2);
        try {
            return content.substring(startIndex + tag.length() + 2, endIndex);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private String getIncludedContent(final String content) {
        int begin = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG) + GenericMessaging.INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(GenericMessaging.INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
    }

    protected void log(Level level, String message) {
        getLogger().log(level, message);
    }

    private Logger getLogger() {
        return this.protocol.getLogger();
    }

    private boolean isTimeOfUseMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.TOU_SEND_NEW_TARIFF);
    }

    private boolean isSendNewPriceMatrixMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.SEND_NEW_PRICE_MATRIX);
    }

    private boolean isSetStandingCharge(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.SET_STANDING_CHARGE);
    }

    private boolean isSetCurrency(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.SET_CURRENCY);
    }

    private boolean isSetCF(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.SET_CONVERSION_FACTOR);
    }

    private boolean isSetCV(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.SET_CALORIFIC_VALUE);
    }

    private boolean isChangeOfTenant(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.CHANGE_OF_TENANT);
    }

    private boolean isChangeOfSupplier(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.CHANGE_OF_SUPPLIER);
    }

    private boolean isFirmwareUpgradeMessage(final String messageContent) {
        return (messageContent != null) && messageContent.contains(RtuMessageConstant.FIRMWARE_UPGRADE);
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.protocol.getTimeZone();
    }
}