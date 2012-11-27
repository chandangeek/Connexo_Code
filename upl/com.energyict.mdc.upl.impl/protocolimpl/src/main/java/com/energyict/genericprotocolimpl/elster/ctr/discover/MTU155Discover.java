package com.energyict.genericprotocolimpl.elster.ctr.discover;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.ShadowList;
import com.energyict.cpo.TypedProperties;
import com.energyict.genericprotocolimpl.elster.ctr.*;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRDiscoverException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.info.ConverterType;
import com.energyict.genericprotocolimpl.elster.ctr.info.MeterType;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ChangeDSTMessage;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationResponseStructure;
import com.energyict.mdw.core.*;
import com.energyict.mdw.coreimpl.DeviceFactoryImpl;
import com.energyict.mdw.shadow.*;
import com.energyict.metadata.*;
import com.energyict.metadata.SearchFilter;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 15/02/11
 * Time: 16:16
 */
public class MTU155Discover {

    private static final int PDR_LENGTH = 14;

    private static final String EXTERNAL_ID_PREFIX = "rtu/";

    private static final String INVALID_PDR = "00000000000000";
    private static final String INVALID_MTU_SERIAL = "ELS000000";

    private static final String CONVERTER_NUMBER_OF_DIGITS = "ConverterNumberOfDigits";
    private static final String METER_NUMBER_OF_DIGITS = "MeterNumberOfDigits";
    private static final String PULSE_WEIGHT_VBS = "PulseWeightVbs";
    private static final String PULSE_WEIGHT_VM = "PulseWeightVm";

    private static boolean infoTypesAvailable = false;

    private final MTU155 mtu155;

    private String pdr;
    private String mtuSerial;
    private String convertorSerial;
    private String meterSerial;
    private ConverterType converterType;
    private MeterType meterType;
    private Quantity meterCaliber;
    private Quantity weightVm;
    private Quantity weightVbs;

    private DeviceType rtuType;
    private Folder lostAndFoundFolder;

    /**
     * Create a new MTU155Discover object for a given protocol instantiation
     *
     * @param mtu155 The current protocol to use during the discovery process
     */
    public MTU155Discover(MTU155 mtu155) {
        this.mtu155 = mtu155;
    }

    /**
     * Fetch all the needed parameters from the rtu, validate them, check for duplicates in the database
     * and if all the previous tests pass, create the new rtu in EIServer.
     *
     * @return The new created rtu
     * @throws CTRDiscoverException if there was a problem during the discovery process
     */
    public Device doDiscover() throws CTRDiscoverException {
        try {
            fetchAndValidatePdr();
            fetchAndValidateMeterSerialNumber();
            fetchAndValidateMTUSerialNumber();
            fetchAndValidateConverterSerialNumber();
            fetchMeterCharacteristics();
            validateExistingRtus();
            Device rtu = createRtuAndAddFields();
            if (rtu == null) {
                throw new CTRDiscoverException("Created RTU returned 'null'");
            }
            log("Discovered device: " + toString());
            return rtu;
        } catch (CTRDiscoverException e) {
            throw new CTRDiscoverException("Unable to AutoDiscover device!", e);
        }
        //throw new CTRDiscoverException(toString());
    }

    /**
     * Log message to protocol logger. The default level is Level.INFO
     *
     * @param message The message to log
     */
    private void log(String message) {
        getLogger().log(Level.INFO, message);
    }

    private Logger getLogger() {
        return mtu155.getLogger();
    }

    /**
     * Log message to protocol logger. The default level is Level.INFO
     *
     * @param message The message to log
     */
    private void severe(String message) {
        mtu155.severe(message);
    }

    /**
     * Read the needed meter parameters from the MTU. These parameters are:
     * <ul>
     * <li>weightVbs</li>
     * <li>weightVm</li>
     * <li>meterCaliber</li>
     * <li>meterType</li>
     * <li>converterType</li>
     * </ul>
     */
    private void fetchMeterCharacteristics() {
        this.weightVbs = getRequestFactory().getMeterInfo().getPulseWeightVbs();
        this.weightVm = getRequestFactory().getMeterInfo().getPulseWeightVm();
        this.meterCaliber = getRequestFactory().getMeterInfo().getMeterCaliber();
        this.meterType = getRequestFactory().getMeterInfo().getMeterType();
        this.converterType = getRequestFactory().getMeterInfo().getConverterType();
    }

    /**
     * Check in the database if the new discovered mtu, convertor or meter already exists
     *
     * @throws CTRDiscoverException when there is a conflicting device in EIServer
     */
    private void validateExistingRtus() throws CTRDiscoverException {
        DeviceFactoryImpl factory = (DeviceFactoryImpl) mw().getDeviceFactory();
        TypeDescriptor typeDescr = factory.getTypeDescriptor();
        SearchFilter filter = new SearchFilter(typeDescr);

        Criterium externalName = typeDescr.getAttributeDescriptor("externalName").eq(createExternalName());
        Criterium deviceId = typeDescr.getAttributeDescriptor("deviceId").eq(mtuSerial);
        Criterium name = typeDescr.getAttributeDescriptor("name").eq(convertorSerial);
        Criterium callHomeId = typeDescr.getAttributeDescriptor("dialHomeId").eq(pdr);

        filter.addAnd(externalName.or(deviceId).or(name).or(callHomeId));
        List<Device> result = factory.findBySearchFilter(filter);
        if (result.size() > 0) {
            String ids = null;
            for (Device rtu : result) {
                if (ids == null) {
                    ids = "" + rtu.getId();
                } else {
                    ids += ", " + rtu.getId();
                }
            }
            throw new CTRDiscoverException("Duplicate external name and/or pdr found for [" + result.size() + "] existing rtu(s) with id(s): [" + ids + "]");
        }

    }

    /**
     * Gets the current MeteringWarehouse, used to search in EIServer and create the new device
     *
     * @return the active MeteringWarehouse
     */
    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

    /**
     * Create the new device in EIServer, using the discovered serials and pdr
     * This method should only be called after all the parameters are validated.
     *
     * @return the created Device
     * @throws CTRDiscoverException when there is an error during the creation of the device in EIServer.
     */
    private Device createRtuAndAddFields() throws CTRDiscoverException {
        try {
            checkAndCreateInfoFields();
            DeviceShadow shadow = getRtuType().newDeviceShadow();
            shadow.setName(convertorSerial);
            shadow.setExternalName(createExternalName());
            shadow.setSerialNumber(meterSerial);
            shadow.setDeviceId(mtuSerial);
            shadow.setNodeAddress(pdr);
            shadow.setDialHomeId(pdr);
            shadow.setIpAddress(getRequestFactory().getIPAddress());
            shadow.getProperties().setProperty(PULSE_WEIGHT_VM, weightVm.getAmount().toString());
            shadow.getProperties().setProperty(PULSE_WEIGHT_VBS, weightVbs.getAmount().toString());
            shadow.getProperties().setProperty(METER_NUMBER_OF_DIGITS, "" + DigitsCalculator.getMeterNumberOfDigits(meterType, meterCaliber, weightVm));
            shadow.getProperties().setProperty(CONVERTER_NUMBER_OF_DIGITS, "" + DigitsCalculator.getConvertorNumberOfDigits());
            ShadowList<ChannelShadow> channelShadows = shadow.getChannelShadows();
            shadow.setLastReading(getCalculatedLastReading());
            shadow.setLastLogbook(null);
            for (ChannelShadow channelShadow : channelShadows) {
                channelShadow.setLastReading(getCalculatedLastReading());
            }
            if (getFolder() != null) {
                shadow.setFolderId(getFolder().getId());
            }
            return mw().getDeviceFactory().create(shadow);
        } catch (SQLException e) {
            throw new CTRDiscoverException("Unable to create rtu", e);
        } catch (BusinessException e) {
            throw new CTRDiscoverException("Unable to create rtu", e);
        }

    }

    private Date getCalculatedLastReading() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, (getProperties().getChannelBacklog()) * (-1));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private MTU155Properties getProperties() {
        return mtu155.getProtocolProperties();
    }

    /**
     * Checks if the required InfoTypes exists, and if not, create them
     *
     * @throws BusinessException If there was an error while creating the InfoType
     * @throws SQLException      If there was an SQL error while creating the InfoType
     */
    private void checkAndCreateInfoFields() throws BusinessException, SQLException {
        if (!infoTypesAvailable) {
            List<InfoType> objects = mw().getInfoTypeFactory().findAll();
            boolean vm = containsNamedObject(objects, PULSE_WEIGHT_VM);
            boolean vbs = containsNamedObject(objects, PULSE_WEIGHT_VBS);
            boolean meterDigits = containsNamedObject(objects, METER_NUMBER_OF_DIGITS);
            boolean converterDigits = containsNamedObject(objects, CONVERTER_NUMBER_OF_DIGITS);
            if (vm && vbs && meterDigits && converterDigits) {
                infoTypesAvailable = true;
            } else {
                if (!vm) {
                    createInfoType(PULSE_WEIGHT_VM);
                }
                if (!vbs) {
                    createInfoType(PULSE_WEIGHT_VBS);
                }
                if (!meterDigits) {
                    createInfoType(METER_NUMBER_OF_DIGITS);
                }
                if (!converterDigits) {
                    createInfoType(CONVERTER_NUMBER_OF_DIGITS);
                }
            }
        }
    }

    /**
     * Create an InfoType in EIServer with the given name
     *
     * @param infoTypeName The new of the InfoType to create
     * @return The new InfoType
     * @throws BusinessException If there was an error while creating the InfoType
     * @throws SQLException      If there was an SQL error while creating the InfoType
     */
    private InfoType createInfoType(String infoTypeName) throws BusinessException, SQLException {
        InfoTypeFactory factory = mw().getInfoTypeFactory();
        InfoTypeShadow shadow = new InfoTypeShadow();
        shadow.setName(infoTypeName);
        shadow.setProperties(new TypedProperties(

        ));
        return factory.create(shadow);
    }

    /**
     * Checks if an object (NamedBusinessObject) from a given list has the same name as the given name
     *
     * @param infoTypes The list of NamedBusinessObject
     * @param name
     * @return
     */
    private boolean containsNamedObject(List<InfoType> infoTypes, String name) {
        for (InfoType infoType : infoTypes) {
            if (infoType.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The external name of the rtu should always have a given format: 'rtu/<convertorSerial>'
     *
     * @return the external name that should be used while creating the new device in EIServer
     */
    private String createExternalName() {
        return EXTERNAL_ID_PREFIX + convertorSerial;
    }

    /**
     * Get the rtuType from EIServer that matches the rtuType given in the properties
     *
     * @return
     * @throws CTRDiscoverException
     */
    private DeviceType getRtuType() throws CTRDiscoverException {
        if (rtuType == null) {
            String rtuTypeName = getProperties().getRtuType();
            rtuType = mw().getDeviceTypeFactory().find(rtuTypeName);
            if (rtuType == null) {
                throw new CTRDiscoverException("Unable to find DeviceType with name [" + rtuTypeName + "].");
            }
        }
        return rtuType;
    }

    /**
     * Get the folder from EIServer that matches the folder given in the properties
     * This folder is used to create the new discovered device in.
     *
     * @return
     */
    private Folder getFolder() {
        if (lostAndFoundFolder == null) {
            String folderExtName = getProperties().getFolderExternalName();
            lostAndFoundFolder = mw().getFolderFactory().findByExternalName(folderExtName);
        }
        return lostAndFoundFolder;
    }

    /**
     * Read the serial number of the (dump) gas meter from the rtu.
     *
     * @throws CTRDiscoverException when the serial number could not be fetched, or has an invalid format
     */
    private void fetchAndValidateMeterSerialNumber() throws CTRDiscoverException {
        IdentificationResponseStructure identStruct = getRequestFactory().getIdentificationStructure();
        this.meterSerial = identStruct != null ? identStruct.getMeterSerialNumber() : null;
        validateMeterSerialNumber();
    }

    /**
     * Validate the serial number of the gas meter.
     * The serial number should not be null or empty
     *
     * @throws CTRDiscoverException when the serial number is invalid
     */
    private void validateMeterSerialNumber() throws CTRDiscoverException {
        if (meterSerial == null) {
            throw new CTRDiscoverException("meterSerial is [null].");
        } else if (meterSerial.length() == 0) {
            throw new CTRDiscoverException("meterSerial is empty.");
        }
    }

    /**
     * Read the MTU's serial number from the device (grouped in the MeterInfo object)
     * and validate the received value.
     *
     * @throws CTRDiscoverException if the serial number is invalid or could not be fetched
     */
    private void fetchAndValidateMTUSerialNumber() throws CTRDiscoverException {
        if (getProperties().isGenerateRandomMTUSerial()) {
            this.mtuSerial = "2010-" + ProtocolTools.addPaddingAndClip(System.currentTimeMillis() + "", '0', 6, false);
            severe("Property 'GenerateRandomMTUSerial' is enabled! mtuSerial will be generated, and not read from the device! [" + mtuSerial + "]");
        } else {
            this.mtuSerial = getRequestFactory().getMeterInfo().getMTUSerialNumber();
        }
        validateAndFormatMTUSerialNumber();
    }

    /**
     * Check if the serial number ogf the rtu is a valid serial number,
     * and if it is, format it to the correct 'ELSxxxxxx' format
     *
     * @throws CTRDiscoverException if the serial number is invalid
     */
    private void validateAndFormatMTUSerialNumber() throws CTRDiscoverException {
        quickValidateMTUSerialNumber();
        String formattedSerial = MTUSerialFormatter.formatMTUSerialNumber(mtuSerial);
        if (formattedSerial.equals(INVALID_MTU_SERIAL)) {
            throw new CTRDiscoverException("mtuSerial format is invalid: [" + mtuSerial + "]. This will result in the invalid [" + INVALID_MTU_SERIAL + "] mtuSerial.");
        }
        mtuSerial = formattedSerial;
    }

    /**
     * Do a quick validation on the serial number. It cannot be empty or null.
     * Ignore the serial formatting
     *
     * @throws CTRDiscoverException if the serial number is empty or null
     */
    private void quickValidateMTUSerialNumber() throws CTRDiscoverException {
        if (mtuSerial == null) {
            throw new CTRDiscoverException("mtuSerial is [null].");
        } else if (mtuSerial.length() == 0) {
            throw new CTRDiscoverException("mtuSerial is empty.");
        }
    }

    /**
     * Fetch the converter's serial number from the identification response, and run the validate method on the result
     *
     * @throws CTRDiscoverException if the serial number is invalid or could not be fetched
     */
    private void fetchAndValidateConverterSerialNumber() throws CTRDiscoverException {
        convertorSerial = getRequestFactory().getMeterInfo().getConverterSerialNumber();
        validateConverterSerialNumber();
    }

    /**
     * Validate the converters serial number
     *
     * @throws CTRDiscoverException if the serial number is invalid
     */
    private void validateConverterSerialNumber() throws CTRDiscoverException {
        if (convertorSerial == null) {
            throw new CTRDiscoverException("convertorSerial is [null].");
        } else if (convertorSerial.length() == 0) {
            throw new CTRDiscoverException("convertorSerial is empty.");
        }
    }

    /**
     * Fetch the pdr from the identification response, and run the validate method on the result
     *
     * @throws CTRDiscoverException if the pdr is invalid or could not be fetched
     */
    private void fetchAndValidatePdr() throws CTRDiscoverException {
        IdentificationResponseStructure identStruct = getRequestFactory().getIdentificationStructure();
        CTRAbstractValue<String> pdrObject = identStruct != null ? identStruct.getPdr() : null;
        this.pdr = pdrObject != null ? pdrObject.getValue() : null;
        validatePdr();
    }

    /**
     * Validate the PDR
     * The pdr cannot be null or empty, and should be exactly 14 characters long.
     * All characters should be digits (0-9)
     *
     * @throws CTRDiscoverException if the pdr is invalid
     */
    private void validatePdr() throws CTRDiscoverException {
        if (pdr == null) {
            throw new CTRDiscoverException("pdr is null");
        } else if (pdr.length() != PDR_LENGTH) {
            throw new CTRDiscoverException("pdr [" + pdr + "] should be 14 characters but was [" + pdr.length() + "].");
        } else if (!ProtocolTools.isNumber(pdr)) {
            throw new CTRDiscoverException("pdr [" + pdr + "] should only contain digits (0-9)");
        } else if (pdr.equalsIgnoreCase(INVALID_PDR)) {
            throw new CTRDiscoverException("pdr [" + pdr + "] is not a valid pdr.");
        }
    }

    /**
     * Getter for the requestFactory
     *
     * @return the GprsRequestFactory from the mtu155 protocol
     */
    private RequestFactory getRequestFactory() {
        return mtu155.getRequestFactory();
    }

    /**
     * Disable the usage of the DST in the MTU
     *
     * @param pdr the pdr of the mtu, used for logging
     */
    public void disableDSTForKnockingDevice(String pdr) {
        try {
            boolean dstStatus = getDSTStatusFromKnockingDevice(pdr);
            if (dstStatus) {
                getLogger().severe("DST for meter with PDR [" + pdr + "] is enabled! Disabling DST ...");
                new ChangeDSTMessage(mtu155.getMessageExecuter()).writeDST(false);
                getLogger().severe("DST for meter with PDR [" + pdr + "] disabled successfully.");
            } else {
                getLogger().severe("DST for meter with PDR [" + pdr + "] already disabled. Ignoring.");
            }
        } catch (Exception e) {
            getLogger().severe("Unable to disable the DST for knocking meter with PDR [" + pdr + "]: " + e.getMessage());
        }
    }

    /**
     * Read the DST status from te MTU. If we cant read the DST, or there was an error, return the default value.
     *
     * @param pdr The PRD of the mtu155
     * @return true if DST is enabled, or could not be read. False if the DST is disabled.
     */
    private boolean getDSTStatusFromKnockingDevice(String pdr) {
        try {
            AbstractCTRObject object = getRequestFactory().queryRegister("8.2.0");
            if ((object != null) && (object.getValue() != null) && (object.getValue().length > 0) && (object.getValue()[0] != null)) {
                CTRAbstractValue ctrAbstractValue = object.getValue()[0];
                boolean dstStatus = ctrAbstractValue.getIntValue() != 0;
                getLogger().severe("DST for meter with PDR [" + pdr + "] is [" + dstStatus + "].");
                return dstStatus;
            } else {
                getLogger().severe("Unable to get the DST status from the device! Object was null or empty. For all safety we'll assume DST is enabled.");
            }
        } catch (CTRException e) {
            getLogger().severe("An error occurred while reading the DST status from the device! For all safety we'll assume DST is enabled. " + e.getMessage());
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MTU155Discover");
        sb.append("{converterType=").append(converterType);
        sb.append(", convertorSerial='").append(convertorSerial).append('\'');
        sb.append(", meterCaliber=").append(meterCaliber);
        sb.append(", meterSerial='").append(meterSerial).append('\'');
        sb.append(", meterType=").append(meterType);
        sb.append(", mtuSerial='").append(mtuSerial).append('\'');
        sb.append(", pdr='").append(pdr).append('\'');
        sb.append(", weightVbs=").append(weightVbs);
        sb.append(", weightVm=").append(weightVm);
        sb.append('}');
        return sb.toString();
    }
}
