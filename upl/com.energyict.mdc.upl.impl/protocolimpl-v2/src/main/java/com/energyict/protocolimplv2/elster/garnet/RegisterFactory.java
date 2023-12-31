package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.elster.garnet.common.InstallationConfig;
import com.energyict.protocolimplv2.elster.garnet.common.ReadingResponse;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.exception.NotExecutedException;
import com.energyict.protocolimplv2.elster.garnet.exception.UnableToExecuteException;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorStatusResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorVersionResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverMetersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.RadioParametersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ReadingRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.NotExecutedError;
import com.energyict.protocolimplv2.elster.garnet.structure.field.RepeaterDiagnostic;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterInstallationStatusBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterModel;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterReadingStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterRelayStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterSensorStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterTariffStatus;
import com.energyict.mdc.identifiers.RegisterIdentifierById;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 19/06/2014 - 10:38
 */
public class RegisterFactory implements DeviceRegisterSupport {

    // Concentrator registers
    private static final ObisCode ACTIVE_FIRMWARE_OBIS = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode HARDWARE_MODEL_OBIS = ObisCode.fromString("1.1.0.2.0.255");
    private static final ObisCode SERIAL_OBIS = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode CLIENT_OBIS = ObisCode.fromString("0.0.96.2.0.255");
    private static final ObisCode CONCENTRATOR_STATUS_OBIS = ObisCode.fromString("0.0.96.3.0.255");
    private static final ObisCode REPEATER_DIAGNOSTIC_OBIS = ObisCode.fromString("0.0.96.4.0.255");
    private static final ObisCode RADIO_NET_OBIS = ObisCode.fromString("0.0.96.5.0.255");

    // E-meter registers
    private static final ObisCode ACTIVE_ENERGY_PHASE_1_OBIS = ObisCode.fromString("1.1.1.8.0.255");
    private static final ObisCode ACTIVE_ENERGY_PHASE_2_OBIS = ObisCode.fromString("1.2.1.8.0.255");
    private static final ObisCode ACTIVE_ENERGY_PHASE_3_OBIS = ObisCode.fromString("1.3.1.8.0.255");
    private static final ObisCode REACTIVE_ENERGY_PHASE_1_OBIS = ObisCode.fromString("1.1.3.8.0.255");
    private static final ObisCode REACTIVE_ENERGY_PHASE_2_OBIS = ObisCode.fromString("1.2.3.8.0.255");
    private static final ObisCode REACTIVE_ENERGY_PHASE_3_OBIS = ObisCode.fromString("1.3.3.8.0.255");

    private static final ObisCode SLAVE_RELAY_STATUS_OBIS = ObisCode.fromString("0.0.96.10.1.255");
    private static final ObisCode SLAVE_SENSOR_STATUS_OBIS = ObisCode.fromString("0.0.96.10.2.255");
    private static final ObisCode SLAVE_INSTALLATION_STATUS_OBIS = ObisCode.fromString("0.0.96.10.3.255");
    private static final ObisCode SLAVE_COMMUNICATION_STATUS_OBIS = ObisCode.fromString("0.0.96.10.4.255");
    private static final ObisCode SLAVE_TARIFF_MODE_OBIS = ObisCode.fromString("0.0.96.14.0.255");

    private static final Unit ACTIVE_ENERGY_UNIT = Unit.get(BaseUnit.WATTHOUR);
    private static final Unit REACTIVE_ENERGY_UNIT = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);

    private final GarnetConcentrator meterProtocol;
    private String firmwareVersion = null;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public RegisterFactory(GarnetConcentrator meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.meterProtocol = meterProtocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>(registers.size());

        for (OfflineRegister register : registers) {
            CollectedRegister collectedRegister;
            try {
                if (isRegisterOfMaster(register)) {
                    collectedRegister = readConcentratorRegister(register);
                } else {
                    collectedRegister = readEMeterRegister(register);
                }
            } catch (NotExecutedException e) {
                if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.COMMAND_NOT_IMPLEMENTED)) {
                    collectedRegister = createNotSupportedCollectedRegister(register);
                } else if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.SLAVE_DOES_NOT_EXIST)) {
                    collectedRegister = createTopologyMisMatchCollectedRegister(register);
                } else {
                    collectedRegister = createCouldNotParseCollectedRegister(register, e.getMessage());
                }
            } catch (GarnetException e) {
                collectedRegister = createCouldNotParseCollectedRegister(register, e.getMessage());
            }
            collectedRegisters.add(collectedRegister);
        }
        return collectedRegisters;
    }

    private CollectedRegister readConcentratorRegister(OfflineRegister register) throws GarnetException {
        CollectedRegister collectedRegister = createDeviceRegister(register);
        int concentratorId = getMeterProtocol().getTopologyMaintainer().getDeviceIdOfMaster();
        if (register.getObisCode().equals(SERIAL_OBIS)) {
            ConcentratorVersionResponseStructure concentratorVersion = getRequestFactory().readConcentratorVersion();
            collectedRegister.setCollectedData(concentratorVersion.getSerialNumber().getSerialNumber());
        } else if (register.getObisCode().equals(ACTIVE_FIRMWARE_OBIS)) {
            ConcentratorVersionResponseStructure concentratorVersion = getRequestFactory().readConcentratorVersion();
            collectedRegister.setCollectedData(concentratorVersion.getFirmwareVersion().getFirmwareVersion());
        } else if (register.getObisCode().equals(HARDWARE_MODEL_OBIS)) {
            ConcentratorVersionResponseStructure concentratorVersion = getRequestFactory().readConcentratorVersion();
            collectedRegister.setCollectedData(concentratorVersion.getConcentratorModel().getVersionInfo());
        } else if (register.getObisCode().equals(CLIENT_OBIS)) {
            ConcentratorVersionResponseStructure concentratorVersion = getRequestFactory().readConcentratorVersion();
            collectedRegister.setCollectedData(concentratorVersion.getCustomerCode().getCustomerCode());
        } else if (register.getObisCode().equals(CONCENTRATOR_STATUS_OBIS)) {
            ConcentratorStatusResponseStructure concentratorStatus = getRequestFactory().readConcentratorStatus();
            collectedRegister.setCollectedData(
                    new Quantity(concentratorStatus.getConcentratorConfiguration().getConfigurationCode(), Unit.getUndefined()),
                    concentratorStatus.getConcentratorConfiguration().getMeterConfigurationInfo()
            );
        } else if (register.getObisCode().equals(REPEATER_DIAGNOSTIC_OBIS)) {
            Map<Integer, RepeaterDiagnostic> repeaterDiagnosticMap = getRequestFactory().discoverRepeaters();
            if (!repeaterDiagnosticMap.containsKey(concentratorId)) {
                return createNotSupportedCollectedRegister(register);
            }

            collectedRegister.setCollectedData(
                    new Quantity(repeaterDiagnosticMap.get(concentratorId).getDiagnosticId(), Unit.getUndefined()),
                    repeaterDiagnosticMap.get(concentratorId).getDiagnosticInfo()
            );
        } else if (register.getObisCode().equals(RADIO_NET_OBIS)) {
            RadioParametersResponseStructure radioParameters = getRequestFactory().readRadioParameters();
            collectedRegister.setCollectedData(
                    new Quantity(radioParameters.getRadioNET(), Unit.getUndefined())
            );
        } else {
            collectedRegister = createNotSupportedCollectedRegister(register);
        }
        return collectedRegister;
    }

    public String readFirmwareVersion() throws GarnetException {
        if(this.firmwareVersion == null){
            firmwareVersion = getRequestFactory().readConcentratorVersion().getFirmwareVersion().getFirmwareVersion();
        }
        return firmwareVersion;
    }

    private CollectedRegister readEMeterRegister(OfflineRegister register) throws GarnetException {
        CollectedRegister collectedRegister = createDeviceRegister(register);
        if (register.getObisCode().getF() != 255 && register.getObisCode().getF() != 0) {
            return createNotSupportedCollectedRegister(register);
        }

        if (isMeterReadingRegister(register)) {
            collectedRegister = readMeterReadingRegister(register);
        } else if (register.getObisCode().equals(HARDWARE_MODEL_OBIS)) {
            DiscoverMetersResponseStructure meterStatuses = getRequestFactory().discoverMeters();
            int meterIndex = getMeterIndex(meterStatuses, register.getSerialNumber());
            MeterModel meterModel = meterStatuses.getMeterModelCollection().getAllBitMasks().get(meterIndex);
            collectedRegister.setCollectedData(
                    new Quantity(meterModel.getModelCode(), Unit.getUndefined()),
                    meterModel.getModelInfo()
            );
        } else if (register.getObisCode().equals(SLAVE_RELAY_STATUS_OBIS)) {
            DiscoverMetersResponseStructure meterStatuses = getRequestFactory().discoverMeters();
            int meterIndex = getMeterIndex(meterStatuses, register.getSerialNumber());
            MeterRelayStatus relayStatus = meterStatuses.getMeterRelayStatusCollection().getAllBitMasks().get(meterIndex);
            collectedRegister.setCollectedData(
                    new Quantity(relayStatus.getRelayStatusCode(), Unit.getUndefined()),
                    relayStatus.getRelayStatusInfo()
            );
        } else if (register.getObisCode().equals(SLAVE_SENSOR_STATUS_OBIS)) {
            DiscoverMetersResponseStructure meterStatuses = getRequestFactory().discoverMeters();
            int meterIndex = getMeterIndex(meterStatuses, register.getSerialNumber());
            MeterSensorStatus sensorStatus = meterStatuses.getMeterSensorStatusCollection().getAllBitMasks().get(meterIndex);
            collectedRegister.setCollectedData(
                    new Quantity(sensorStatus.getSensorStatusCode(), Unit.getUndefined()),
                    sensorStatus.getSensorStatusInfo()
            );
        } else if (register.getObisCode().equals(SLAVE_INSTALLATION_STATUS_OBIS)) {
            DiscoverMetersResponseStructure meterStatuses = getRequestFactory().discoverMeters();
            int meterIndex = getMeterIndex(meterStatuses, register.getSerialNumber());
            MeterInstallationStatusBitMaskField installationStatus = meterStatuses.getMeterInstallationStatusCollection().getAllBitMasks().get(meterIndex);
            collectedRegister.setCollectedData(
                    new Quantity(installationStatus.getInstallationStatusCode(), Unit.getUndefined()),
                    createInstallationStatusInfo(meterStatuses, meterIndex)
            );
        } else if (register.getObisCode().equals(SLAVE_COMMUNICATION_STATUS_OBIS)) {
            DiscoverMetersResponseStructure meterStatuses = getRequestFactory().discoverMeters();
            int meterIndex = getMeterIndex(meterStatuses, register.getSerialNumber());
            MeterReadingStatus readingStatus = meterStatuses.getMeterReadingStatusCollection().getAllBitMasks().get(meterIndex);
            collectedRegister.setCollectedData(
                    new Quantity(readingStatus.getReadingStatusCode(), Unit.getUndefined()),
                    readingStatus.getReadingStatusInfo()
            );
        } else if (register.getObisCode().equals(SLAVE_TARIFF_MODE_OBIS)) {
            DiscoverMetersResponseStructure meterStatuses = getRequestFactory().discoverMeters();
            int meterIndex = getMeterIndex(meterStatuses, register.getSerialNumber());
            MeterTariffStatus tariffStatus = meterStatuses.getMeterTariffStatusCollection().getAllBitMasks().get(meterIndex);
            collectedRegister.setCollectedData(
                    new Quantity(tariffStatus.getTariffStatusCode(), Unit.getUndefined()),
                    tariffStatus.getTariffStatusInfo()
            );
        } else {
            collectedRegister = createNotSupportedCollectedRegister(register);
        }
        return collectedRegister;
    }

    private CollectedRegister readMeterReadingRegister(OfflineRegister register) throws GarnetException {
        DiscoverMetersResponseStructure meterStatuses = getRequestFactory().discoverMeters();
        int meterIndex = getMeterIndex(meterStatuses, register.getSerialNumber());
        List<MeterInstallationStatusBitMaskField> installationStatuses = meterStatuses.getMeterInstallationStatusCollection().getAllBitMasks();
        List<Integer> installationConfig = new InstallationConfig(installationStatuses).getConfigForMeter(meterIndex);

        int meterIndexToRead;
        if (register.getObisCode().getB() == 1) {
            if (installationConfig.size() < 1) {
                throw new UnableToExecuteException("Failed to read out the register. This register can only be used for mono-phase configured meters.");
            } else {
                meterIndexToRead = meterIndex;
            }
        } else if (register.getObisCode().getB() == 2) {
            if (installationConfig.size() < 2) { // Expecting poly-phase config, but is mono phase
                throw new UnableToExecuteException("Failed to read out the register. This register can only be used for poly-phase configured meters.");
            } else {
                meterIndexToRead = installationConfig.get(register.getObisCode().getB() - 1);
            }
        } else if (register.getObisCode().getB() == 3) {
            if (installationConfig.size() < 3) { // Expecting three-phase config, but is mono-phase or bi-phase
                throw new UnableToExecuteException("Failed to read out the register. This register can only be used for three-phase configured meters.");
            } else {
                meterIndexToRead = installationConfig.get(register.getObisCode().getB() - 1);
            }
        } else {
            return createNotSupportedCollectedRegister(register);
        }

        CollectedRegister collectedRegister;
        boolean isBilling = register.getObisCode().getF() == 0;
        ReadingResponse readingResponse = getRequestFactory().readRegisterReading(
                meterIndexToRead,
                isBilling ? ReadingRequestStructure.ReadingMode.CHECKPOINT_READING : ReadingRequestStructure.ReadingMode.ONLINE_READING
        );
        collectedRegister = createDeviceRegister(register, isBilling ? readingResponse.getReadingDateTime().getDate() : null);
        boolean activeEnergyRegister = register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_1_OBIS)
                || register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_2_OBIS)
                || register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_3_OBIS);
        collectedRegister.setCollectedData(
                new Quantity(
                        activeEnergyRegister
                                ? readingResponse.getConsumption().getActiveEnergy()
                                : readingResponse.getConsumption().getReactiveEnergy(),
                        activeEnergyRegister
                                ? ACTIVE_ENERGY_UNIT
                                : REACTIVE_ENERGY_UNIT
                )
        );
        return collectedRegister;
    }

    private boolean isMeterReadingRegister(OfflineRegister register) {
        return register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_1_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_2_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_3_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(REACTIVE_ENERGY_PHASE_1_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(REACTIVE_ENERGY_PHASE_2_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(REACTIVE_ENERGY_PHASE_3_OBIS);
    }

    private String createInstallationStatusInfo(DiscoverMetersResponseStructure meterStatuses, int meterIndex) {
        List<MeterInstallationStatusBitMaskField> installationStatuses = meterStatuses.getMeterInstallationStatusCollection().getAllBitMasks();
        List<MeterSerialNumber> serialNumberCollection = meterStatuses.getMeterSerialNumberCollection();
        InstallationConfig installationConfig = new InstallationConfig(installationStatuses);
        List<Integer> config = installationConfig.getConfigForMeter(meterIndex);

        StringBuilder builder = new StringBuilder();
        builder.append(installationStatuses.get(meterIndex).getInstallationStatusInfo());
        if (config.size() > 1) {
            builder.append(" - ");
            builder.append("[");
            Iterator<Integer> it = config.iterator();
            while (it.hasNext()) {
                builder.append(serialNumberCollection.get(it.next()).getSerialNumber());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append("]");
        }

        return builder.toString();
    }

    private int getMeterIndex(DiscoverMetersResponseStructure meterStatuses, String slaveSerialNumber) throws GarnetException {
        int meterIndex = 0;
        for (MeterSerialNumber serialNumber : meterStatuses.getMeterSerialNumberCollection()) {
            if (serialNumber.getSerialNumber().equals(slaveSerialNumber)) {
                return meterIndex;
            }
            meterIndex++;
        }
        throw new UnableToExecuteException("Slave " + slaveSerialNumber + " not found on the concentrator. The EIMaster topology is probably wrong.");
    }

    private CollectedRegister createDeviceRegister(OfflineRegister register) {
        return this.createDeviceRegister(register, null);
    }

    private CollectedRegister createDeviceRegister(OfflineRegister register, Date eventTime) {
        CollectedRegister deviceRegister = this.collectedDataFactory.createDefaultCollectedRegister(new RegisterIdentifierById(register.getRegisterId(), register.getObisCode(), register.getDeviceIdentifier()));
        deviceRegister.setCollectedTimeStamps(new Date(), null, eventTime != null ? eventTime : new Date(), eventTime); // If eventTime != null, then it contains the billing timestamp
        return deviceRegister;
    }

    private CollectedRegister createNotSupportedCollectedRegister(OfflineRegister register) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(register, "registerXnotsupported", register.getObisCode()));
        return failedRegister;
    }

    private CollectedRegister createTopologyMisMatchCollectedRegister(OfflineRegister register) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.ConfigurationMisMatch, this.issueFactory.createWarning(register, "topologyMismatch"));
        return failedRegister;
    }

    private CollectedRegister createCouldNotParseCollectedRegister(OfflineRegister register, String errorMessage) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.InCompatible, this.issueFactory.createProblem(register, "CouldNotParseRegisterData", errorMessage));
        return failedRegister;
    }

    private boolean isRegisterOfMaster(OfflineRegister register) {
        return register.getSerialNumber().equals(getMeterProtocol().getOfflineDevice().getSerialNumber());
    }

    public GarnetConcentrator getMeterProtocol() {
        return meterProtocol;
    }

    public RequestFactory getRequestFactory() {
        return getMeterProtocol().getRequestFactory();
    }
}