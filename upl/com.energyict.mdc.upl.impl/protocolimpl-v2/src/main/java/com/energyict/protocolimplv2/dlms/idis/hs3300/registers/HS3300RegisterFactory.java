package com.energyict.protocolimplv2.dlms.idis.hs3300.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.composedobjects.ComposedData;
import com.energyict.protocolimplv2.common.composedobjects.ComposedObject;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.hs3300.HS3300;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300Properties;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.io.IOException;
import java.util.*;

public class HS3300RegisterFactory implements DeviceRegisterSupport {

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

    /**
     * The number of attributes in a bulk request should be smaller than 16.
     */
    private static final int BULK_REQUEST_ATTRIBUTE_LIMIT = 16;

    private final HS3300 hs3300;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private HS3300PLCRegisterMapper plcRegisterMapper;

    public HS3300RegisterFactory(HS3300 hs3300, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.hs3300 = hs3300;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    // TODO @Override
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
            return null;// TODO super.addComposedObjectToComposedRegisterMap(composedObjectMap, dlmsAttributes, register);
        }
    }

    // TODO @Override
    protected RegisterValue getRegisterValueForComposedRegister(OfflineRegister offlineRegister, Date captureTime, AbstractDataType attributeValue, Unit unit) {
        HS3300Properties hs330Properties = (HS3300Properties) getMeterProtocol().getDlmsSessionProperties();
        if (captureTime != null && hs330Properties.useBeaconMirrorDeviceDialect()) {
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
            return null;// TODO super.getRegisterValueForComposedRegister(offlineRegister, captureTime, attributeValue, unit);
        }
    }

    //Add a warning if it's an "old" register value
    private void validateRegisterResult(OfflineRegister offlineRegister, CollectedRegister collectedRegister, Date captureTime) {
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
    private ComposedData getImageTransferBlockSizeMapping() {
        return new ComposedData(new DLMSAttribute(OBIS_IMAGE_TRANSFER, 2, DLMSClassId.IMAGE_TRANSFER.getClassId()));
    }

    /**
     * Filter out the following registers:
     * - MBus devices (by serial number) that are not installed on the e-meter
     * - Obiscode 0.0.128.0.2.255, this register value will be filled in by executing the path request message, not by the register reader
     * - Obiscode 0.3.44.0.128.255, this register value will be filled in by executing the 'read DC multicast progress' message on the Beacon protocol
     */
    protected List<CollectedRegister> filterOutAllInvalidRegistersFromList(List<OfflineRegister> offlineRegisters) {
        final List<CollectedRegister> invalidRegisters = new ArrayList<>();

        Iterator<OfflineRegister> it = offlineRegisters.iterator();
        while (it.hasNext()) {
            OfflineRegister register = it.next();
            if (getMeterProtocol().getPhysicalAddressFromSerialNumber(register.getSerialNumber()) == -1) {
                invalidRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, "Register " + register + " is not supported because MbusDevice " + register.getSerialNumber() + " is not installed on the physical device."));
                it.remove();
            }
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
        List<OfflineRegister> subSet;
        List<CollectedRegister> registers = new ArrayList<>();

        if (this.isNotMirroredOnDC()) {
            registers.addAll(readBillingRegisters(offlineRegisters));      // Cause these cannot be read out in bulk
            // TODO filterOutAllAllBillingRegistersFromList(offlineRegisters);  // Cause they are already read out (see previous line)
        }

        registers.addAll(filterOutAllInvalidRegistersFromList(offlineRegisters)); // For each invalid one, an 'Incompatible' collectedRegister will be added

        int from = 0;
        while (from < offlineRegisters.size()) {    //Read out in steps of x registers
            subSet = offlineRegisters.subList(from, offlineRegisters.size());
            List<CollectedRegister> collectedRegisters = null;// TODO readSubSetOfRegisters(subSet);
            from += collectedRegisters.size();
            registers.addAll(collectedRegisters);
        }

        if (!this.isNotMirroredOnDC()) {
            // If we don't map from a profile, but read directly, from a DC, we'll need to perform a couple of fixups.
            // For FW version >= 1.12.3, we have the Beacon read time in A- regs, the MDI reset time in A+ regs, and the event time in P regs.
            // For older versions, both A+ and A- contain the MDI reset time (aka the billing time). We don't have the Beacon read time on these.
            fixupBillingRegisterTimestamps(registers);
        }

        return registers;
    }

    // TODO @Override
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
            return null; // TODO super.createCollectedRegisterFor(offlineRegister, composedObjectMap, composedCosemObject);
        }
    }

    private HS3300PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new HS3300PLCRegisterMapper(getMeterProtocol().getDlmsSession());
        }
        return plcRegisterMapper;
    }

    private List<CollectedRegister> readBillingRegisters(List<OfflineRegister> offlineRegisters) {
        List<CollectedRegister> collectedBillingRegisters = new ArrayList<>();
        for (OfflineRegister offlineRegister : offlineRegisters) {
            if (offlineRegister.getObisCode().getF() != 255) {
                collectedBillingRegisters.add(readBillingRegister(offlineRegister));
            }
        }
        return collectedBillingRegisters;
    }

    protected CollectedRegister readBillingRegister(OfflineRegister offlineRegister) {
        try {
            HistoricalValue historicalValue = null;// TODO ((HS330) getMeterProtocol()).getStoredValues().getHistoricalValue(offlineRegister.getObisCode());
            RegisterValue registerValue = new RegisterValue(
                    offlineRegister.getObisCode(),
                    historicalValue.getQuantityValue(),
                    historicalValue.getEventTime(), // event time
                    null, // from time
                    historicalValue.getBillingDate(), // to time
                    historicalValue.getCaptureTime(),  // read time
                    0,
                    null);

            return createCollectedRegister(registerValue, offlineRegister);
        } catch (NoSuchRegisterException e) {
            return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported, e.getMessage());
        } catch (NotInObjectListException e) {
            return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        } catch (IOException e) {
            return handleIOException(offlineRegister, e);
        }
    }

    protected CollectedRegister handleIOException(OfflineRegister offlineRegister, IOException e) {
        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSession().getProperties().getRetries())) {
            if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
            } else {
                return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
            }
        } else {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getMeterProtocol().getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    /**
     * Fixes up the billing register time stamps.
     */
    private static void fixupBillingRegisterTimestamps(final List<CollectedRegister> registers) {
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
     * Create a collected register that indicates no data is available at this time for the particular register.
     *
     * @param offlineRegister The {@link OfflineRegister}.
     * @return The corresponding {@link CollectedRegister}.
     */
    private CollectedRegister dataNotAvailable(final OfflineRegister offlineRegister) {
        final CollectedRegister collectedRegister = collectedDataFactory.createDefaultCollectedRegister(this.getRegisterIdentifier(offlineRegister));
        @SuppressWarnings("unchecked") final Issue issue = getIssueFactory().createWarning(offlineRegister.getObisCode(), "noDataFound", new Object[0]);
        collectedRegister.setFailureInformation(ResultType.DataIncomplete, issue);

        return collectedRegister;
    }

    protected CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, getIssueFactory().createWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            if (errorMessage.length == 0) {
                collectedRegister.setFailureInformation(ResultType.NotSupported, getIssueFactory().createWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
            } else {
                collectedRegister.setFailureInformation(ResultType.NotSupported, getIssueFactory().createWarning(register.getObisCode(), "registerXnotsupportedBecause", register.getObisCode(), errorMessage[0]));
            }
        }
        return collectedRegister;
    }

    protected CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        validateRegisterResult(offlineRegister, deviceRegister, registerValue.getReadTime());
        return deviceRegister;
    }

    protected RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }

    /**
     * Indicates whether or not this concerns a P+- billing register.
     *
     * @param logicalName The logical name.
     * @param index       The index.
     * @return <code>true</code> if this is a P+ or P- register, <code>false</code> if not.
     */
    private static boolean isPBillingRegister(final ObisCode logicalName, final int index) {
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
    private static boolean isAPlusBillingRegister(final ObisCode logicalName, final int index) {
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
    private static boolean isAMinusBillingRegister(final ObisCode logicalName, final int index) {
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
    private boolean isNotMirroredOnDC() {
        // Don't map these if we are using a mirror, because they have already been mapped by the Beacon itself (as it runs the AM540 protocol).
        return !((HS3300) this.getMeterProtocol()).getDlmsSessionProperties().useBeaconMirrorDeviceDialect();
    }

    private AbstractDlmsProtocol getMeterProtocol() {
        return hs3300;
    }

    private IssueFactory getIssueFactory() {
        return issueFactory;
    }
}