package com.energyict.protocolimplv2.dlms.idis.am540.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.composedobjects.ComposedData;
import com.energyict.protocolimplv2.common.composedobjects.ComposedObject;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;

import java.io.IOException;
import java.util.*;

/**
 * RegisterFactory created for the AM540 protocol <br/>
 * Note: extends from AM130RegisterFactory - which enables readout of all common elements -
 * this class adds readout of the various PLC objects.
 *
 * @author sva
 * @since 11/08/2015 - 15:46
 */
public class AM540RegisterFactory extends AM130RegisterFactory {

    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));

    /**
     * OBIS code of the image transfer instance.
     */
    private static final ObisCode OBIS_IMAGE_TRANSFER = ObisCode.fromString("0.0.44.0.0.255");

    /**
     * Image block size attribute.
     */
    private static final byte ATTRIBUTE_IMAGE_BLOCK_SIZE = 2;

    /**
     * Mapped register (0.2.44.0.0.255), maps to the image block size.
     */
    private static final ObisCode MAPPED_IMAGE_TRANSFER_BLOCK_SIZE = ProtocolTools.setObisCodeField(OBIS_IMAGE_TRANSFER, 1, ATTRIBUTE_IMAGE_BLOCK_SIZE);

    private AM540PLCRegisterMapper plcRegisterMapper;

    public AM540RegisterFactory(AM540 am540, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(am540, collectedDataFactory, issueFactory);
    }

    @Override
    protected Boolean addComposedObjectToComposedRegisterMap(Map<ObisCode, ComposedObject> composedObjectMap, List<DLMSAttribute> dlmsAttributes, OfflineRegister register) {
        G3Mapping g3Mapping = getPLCRegisterMapper().getG3Mapping(register.getObisCode());

        if (g3Mapping != null && this.isNotMirroredOnDC()) {
            ComposedRegister composedRegister = new ComposedRegister();
            int[] attributeNumbers = g3Mapping.getAttributeNumbers();

            if (dlmsAttributes.size() + attributeNumbers.length > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                return null; //Don't add the new attributes, no more room
            }

            for (int index = 0; index < attributeNumbers.length; index++) {
                int attributeNumber = attributeNumbers[index];
                DLMSAttribute dlmsAttribute = new DLMSAttribute(g3Mapping.getBaseObisCode(), attributeNumber, g3Mapping.getDLMSClassId());
                dlmsAttributes.add(dlmsAttribute);

                //If the mapping contains more than 1 attribute, the order is always value, unit, captureTime
                if (index == 0) {
                    composedRegister.setRegisterValue(dlmsAttribute);
                } else if (index == 1) {
                    composedRegister.setRegisterUnit(dlmsAttribute);
                } else if (index == 2) {
                    composedRegister.setRegisterCaptureTime(dlmsAttribute);
                }
            }
            composedObjectMap.put(register.getObisCode(), composedRegister);
            return true;
        } else {
            return super.addComposedObjectToComposedRegisterMap(composedObjectMap, dlmsAttributes, register);
        }
    }

    @Override
    protected RegisterValue getRegisterValueForComposedRegister(OfflineRegister offlineRegister, Date captureTime, AbstractDataType attributeValue, Unit unit) {
        AM540Properties am540Properties = (AM540Properties) getMeterProtocol().getDlmsSessionProperties();
        if (captureTime != null && am540Properties.useBeaconMirrorDeviceDialect()) {
            // for composed registers:
            // - readTime is the value stored in attribute#5=captureTime = the metrological date
            // - eventTime is the communication time -> not used in macrology
            if (attributeValue.isOctetString()) {
                return new RegisterValue(offlineRegister,
                        null, //quantity
                        new Date(), // eventTime = read-out time,
                        null, null, // fromTime, toTime
                        captureTime, // readTime
                        0,
                        attributeValue.getOctetString().stringValue());
            } else {
                return new RegisterValue(offlineRegister, new Quantity(attributeValue.toBigDecimal(), unit),
                        new Date(), // eventTime = read-out time
                        null,       // fromTime
                        null,       // toTime
                        captureTime); // readTime
            }
        } else {
            return super.getRegisterValueForComposedRegister(offlineRegister, captureTime, attributeValue, unit);
        }
    }

    @Override
    //Add a warning if it's an "old" register value
    protected void validateRegisterResult(OfflineRegister offlineRegister, CollectedRegister collectedRegister, Date captureTime) {
        if (isNotMirroredOnDC())
            return;
        if (offlineRegister.getLastReadingDate().isPresent() && offlineRegister.getLastReadingDate().get().getEpochSecond() == (captureTime.getTime() / 1000)) {
            collectedRegister.setFailureInformation(ResultType.Other, getIssueFactory().createWarning(offlineRegister.getObisCode(), "registerXissue", offlineRegister.getObisCode(), "Received an old register value from the mirror device. This could mean that the Beacon DC was not able to read out new register values from the actual device. Please check the logbook of the Beacon device for issues."));
        }
    }

    /**
     * Treat the image transfer block size mapping as a data object.
     *
     * @return The image transfer block size mapping.
     */
    private final ComposedData getImageTransferBlockSizeMapping() {
        return new ComposedData(new DLMSAttribute(OBIS_IMAGE_TRANSFER, 2, DLMSClassId.IMAGE_TRANSFER.getClassId()));
    }

    /**
     * Filter out the following registers:
     * - MBus devices (by serial number) that are not installed on the e-meter
     * - Obiscode 0.0.128.0.2.255, this register value will be filled in by executing the path request message, not by the register reader
     * - Obiscode 0.3.44.0.128.255, this register value will be filled in by executing the 'read DC multicast progress' message on the Beacon protocol
     */
    @Override
    protected List<CollectedRegister> filterOutAllInvalidRegistersFromList(List<OfflineRegister> offlineRegisters) {
        final List<CollectedRegister> invalidRegisters = super.filterOutAllInvalidRegistersFromList(offlineRegisters);

        Iterator<OfflineRegister> it = offlineRegisters.iterator();
        while (it.hasNext()) {
            OfflineRegister register = it.next();
            if (register.getObisCode().equals(G3NetworkManagement.getDefaultObisCode())) {
                invalidRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, "Register with obiscode " + register.getObisCode() + " cannot be read out, use the path request message for this."));
                it.remove();
            }
            if (register.getObisCode().equals(MULTICAST_METER_PROGRESS)) {
                invalidRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, "Register with obiscode " + register.getObisCode() + " cannot be read out, use the 'read DC multicast progress' message on the Beacon protocol for this."));
                it.remove();
            }
        }
        return invalidRegisters;
    }

    public final List<CollectedRegister> readRegisters(final List<OfflineRegister> offlineRegisters) {
        final List<CollectedRegister> registers = super.readRegisters(offlineRegisters);

        if (!this.isNotMirroredOnDC()) {
            // If we don't map from a profile, but read directly, from a DC, we'll need to perform a couple of fixups.
            // For FW version >= 1.12.3, we have the Beacon read time in A- regs, the MDI reset time in A+ regs, and the event time in P regs.
            // For older versions, both A+ and A- contain the MDI reset time (aka the billing time). We don't have the Beacon read time on these.
            fixupBillingRegisterTimestamps(registers);
        }

        return registers;
    }

    @Override
    protected CollectedRegister createCollectedRegisterFor(OfflineRegister offlineRegister, Map<ObisCode, ComposedObject> composedObjectMap, ComposedCosemObject composedCosemObject) {
        ComposedObject composedObject = composedObjectMap.get(offlineRegister.getObisCode());
        G3Mapping g3Mapping = getPLCRegisterMapper().getG3Mapping(offlineRegister.getObisCode());

        if (g3Mapping != null && this.isNotMirroredOnDC()) {
            if (composedObject == null) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);    // Should never occur, but safety measure
            } else {
                ComposedRegister composedRegister = (ComposedRegister) composedObject;
                try {
                    Unit unit = null;
                    if (composedRegister.getRegisterUnitAttribute() != null) {
                        unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
                    }
                    Date captureTime = null;
                    if (composedRegister.getRegisterCaptureTime() != null) {
                        AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
                        captureTime = captureTimeOctetString.getOctetString().getDateTime(getMeterProtocol().getDlmsSession().getTimeZone()).getValue().getTime();
                    }

                    AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
                    RegisterValue registerValue = g3Mapping.parse(attributeValue, unit, captureTime);
                    return createCollectedRegister(registerValue, offlineRegister);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                        if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                            return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                        } else if (DLMSIOExceptionHandler.isTemporaryFailure(e)) {
                            return this.dataNotAvailable(offlineRegister);
                        } else {
                            return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                        }
                    } // else a proper connectionCommunicationException is thrown
                    return null;
                }
            }
        } else {
            return super.createCollectedRegisterFor(offlineRegister, composedObjectMap, composedCosemObject);
        }
    }

    private AM540PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new AM540PLCRegisterMapper(getMeterProtocol().getDlmsSession());
        }
        return plcRegisterMapper;
    }

    /**
     * Fixes up the billing register time stamps.
     */
    private static final void fixupBillingRegisterTimestamps(final List<CollectedRegister> registers) {
        final Set<CollectedRegister> pRegisters = new HashSet<>();
        final Set<CollectedRegister> aPlusRegisters = new HashSet<>();
        final Set<CollectedRegister> aMinusRegisters = new HashSet<>();

        for (int index = 0; index < 15; index++) {
            for (final CollectedRegister register : registers) {
                if (isPBillingRegister(register.getRegisterIdentifier().getRegisterObisCode(), index)) {
                    pRegisters.add(register);
                } else if (isAPlusBillingRegister(register.getRegisterIdentifier().getRegisterObisCode(), index)) {
                    aPlusRegisters.add(register);
                } else if (isAMinusBillingRegister(register.getRegisterIdentifier().getRegisterObisCode(), index)) {
                    aMinusRegisters.add(register);
                }
            }

            if (aPlusRegisters.size() > 0 && aMinusRegisters.size() > 0) {
                final Date toTime = aPlusRegisters.iterator().next().getReadTime();
                Date readTime = aMinusRegisters.iterator().next().getReadTime();

                if (readTime == null || readTime.equals(toTime)) {
                    // Beacon FW version < 1.12.3 doesn't keep the Beacon read time, meaning we have to use now().
                    readTime = new Date();
                }

                for (final CollectedRegister pRegister : pRegisters) {
                    pRegister.setCollectedTimeStamps(readTime, null, toTime, pRegister.getReadTime());
                }

                for (final CollectedRegister aPlusRegister : aPlusRegisters) {
                    aPlusRegister.setCollectedTimeStamps(readTime, null, toTime);
                }

                for (final CollectedRegister aMinusRegister : aMinusRegisters) {
                    aMinusRegister.setCollectedTimeStamps(readTime, null, toTime);
                }
            }

            pRegisters.clear();
            aPlusRegisters.clear();
            aMinusRegisters.clear();
        }
    }

    /**
     * Indicates whether or not this concerns a P+- billing register.
     *
     * @param logicalName The logical name.
     * @param index       The index.
     * @return <code>true</code> if this is a P+ or P- register, <code>false</code> if not.
     */
    private static final boolean isPBillingRegister(final ObisCode logicalName, final int index) {
        return logicalName.getA() == 1 &&
                logicalName.getB() == 0 &&
                (logicalName.getC() == 1 || logicalName.getC() == 2) &&
                logicalName.getD() == 6 &&
                logicalName.getE() == 0 &&
                logicalName.getF() == index;
    }

    /**
     * Indicates whether or not this concerns an A+ billing register.
     *
     * @param logicalName The logical name.
     * @param index       The index.
     * @return        <code>true</code> if this is an A+ register, <code>false</code> if not.
     */
    private static final boolean isAPlusBillingRegister(final ObisCode logicalName, final int index) {
        return logicalName.getA() == 1 &&
                logicalName.getB() == 0 &&
                logicalName.getC() == 1 &&
                logicalName.getD() == 8 &&
                (logicalName.getE() == 0 || logicalName.getE() == 1 || logicalName.getE() == 2) &&
                logicalName.getF() == index;
    }

    /**
     * Indicates whether or not this concerns an A- billing register.
     *
     * @param logicalName The logical name.
     * @param        index            The index.
     * @return        <code>true</code> if this is an A- register, <code>false</code> if not.
     */
    private static final boolean isAMinusBillingRegister(final ObisCode logicalName, final int index) {
        return logicalName.getA() == 1 &&
                logicalName.getB() == 0 &&
                logicalName.getC() == 2 &&
                logicalName.getD() == 8 &&
                (logicalName.getE() == 0 || logicalName.getE() == 1 || logicalName.getE() == 2) &&
                logicalName.getF() == index;
    }

    /**
     * Indicates whether or not we are mapping billing registers from a billing profile.
     *
     * @return <code>true</code> if we are mapping, <code>false</code> if we are not (for example when we have a mirror on a Beacon).
     */
    @Override
    protected final boolean isNotMirroredOnDC() {
        // Don't map these if we are using a mirror, because they have already been mapped by the Beacon itself (as it runs the AM540 protocol).
        return !((AM540) this.getMeterProtocol()).getDlmsSessionProperties().useBeaconMirrorDeviceDialect();
    }
}