package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifierById;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.MdcManager;
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
    private static final ObisCode ACTIVE_ENERGY_OBIS = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode ACTIVE_ENERGY_PHASE_1_OBIS = ObisCode.fromString("1.1.1.8.0.255");
    private static final ObisCode ACTIVE_ENERGY_PHASE_2_OBIS = ObisCode.fromString("1.2.1.8.0.255");
    private static final ObisCode ACTIVE_ENERGY_PHASE_3_OBIS = ObisCode.fromString("1.3.1.8.0.255");
    private static final ObisCode REACTIVE_ENERGY_OBIS = ObisCode.fromString("1.0.3.8.0.255");
    private static final ObisCode REACTIVE_ENERGY_PHASE_1_OBIS = ObisCode.fromString("1.1.3.8.0.255");
    private static final ObisCode REACTIVE_ENERGY_PHASE_2_OBIS = ObisCode.fromString("1.2.3.8.0.255");
    private static final ObisCode REACTIVE_ENERGY_PHASE_3_OBIS = ObisCode.fromString("1.3.3.8.0.255");

    private static final ObisCode SLAVE_RELAY_STATUS_OBIS = ObisCode.fromString("0.0.96.10.1.255");
    private static final ObisCode SLAVE_SENSOR_STATUS_OBIS = ObisCode.fromString("0.0.96.10.2.255");
    private static final ObisCode SLAVE_INSTALLATION_STATUS_OBIS = ObisCode.fromString("0.0.96.10.3.255");
    private static final ObisCode SLAVE_COMMUNICATION_STATUS_OBIS = ObisCode.fromString("0.0.96.10.4.255");
    private static final ObisCode SLAVE_TARIFF_MODE_OBIS = ObisCode.fromString("0.0.96.14.0.255");

    private static final Unit ACTIVE_ENERGY_UNIT = Unit.get(BaseUnit.WATTHOUR, 3);
    private static final Unit REACTIVE_ENERGY_UNIT = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3);

    private final GarnetConcentrator meterProtocol;

    public RegisterFactory(GarnetConcentrator meterProtocol) {
        this.meterProtocol = meterProtocol;
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
                    collectedRegister = createCouldNotParseCollectedRegister(register);
                }
            } catch (GarnetException e) {
                collectedRegister = createCouldNotParseCollectedRegister(register);
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
        if (register.getObisCode().getB() == 0) {
            if (installationConfig.size() == 1) {
                meterIndexToRead = meterIndex;
            } else {
                throw new UnableToExecuteException("Failed to read out the register. This register can only be used for mono-phase configured meters.");
            }
        } else if ((register.getObisCode().getB() == 1 || register.getObisCode().getB() == 2)) {
            if (installationConfig.size() == 2 || installationConfig.size() == 3) { // Expecting poly phase config, but is mono phase
                meterIndexToRead = installationConfig.get(register.getObisCode().getB() - 1);
            } else {
                throw new UnableToExecuteException("Failed to read out the register. This register can only be used for poly-phase configured meters.");
            }
        } else {
            return createNotSupportedCollectedRegister(register);
        }

        CollectedRegister collectedRegister;
        ReadingResponse readingResponse = getRequestFactory().readRegisterReading(
                meterIndexToRead,
                register.getObisCode().getF() == 0 ? ReadingRequestStructure.ReadingMode.CHECKPOINT_READING : ReadingRequestStructure.ReadingMode.ONLINE_READING
        );
        collectedRegister = createDeviceRegister(register);
        boolean activeEnergyRegister = register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_OBIS)
                || register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_1_OBIS)
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
        return register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_1_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_2_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(ACTIVE_ENERGY_PHASE_3_OBIS) ||
                register.getObisCode().equalsIgnoreBillingField(REACTIVE_ENERGY_OBIS) ||
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
        throw new UnableToExecuteException("Slave " + slaveSerialNumber +" not found on the concentrator. The EIMaster topology is probably wrong.");
    }

    private CollectedRegister createDeviceRegister(OfflineRegister register) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterIdentifierById(register.getRegisterId(), register.getObisCode()));
        deviceRegister.setCollectedTimeStamps(new Date(), null, new Date(), null);
        return deviceRegister;
    }

    private CollectedRegister createNotSupportedCollectedRegister(OfflineRegister register) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addProblem(register, "registerXnotsupported", register.getObisCode()));
        return failedRegister;
    }

    private CollectedRegister createTopologyMisMatchCollectedRegister(OfflineRegister register) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.ConfigurationMisMatch, MdcManager.getIssueCollector().addProblem(register, "topologyMismatch"));
        return failedRegister;
    }

    private CollectedRegister createCouldNotParseCollectedRegister(OfflineRegister register) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(register, "CouldNotParseRegisterData"));
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

    private class InstallationConfig {

        private List<List<Integer>> installationConfigs;

        private InstallationConfig(List<MeterInstallationStatusBitMaskField> installationStatuses) {
            this.installationConfigs = new ArrayList<>();
            buildInstallationConfig(installationStatuses);
        }

        private void buildInstallationConfig(List<MeterInstallationStatusBitMaskField> installationStatuses) {
            int i = 0;
            while (i < installationStatuses.size()) {
                List<Integer> config = new ArrayList<>();
                switch (installationStatuses.get(i).getInstallationStatus()) {
                    case TWO_PHASE:
                        config.add(i);
                        config.add(i + 1);
                        i += 2;
                        break;
                    case THREE_PHASE:
                        config.add(i);
                        config.add(i + 1);
                        config.add(i + 2);
                        i += 3;
                        break;
                    default:
                        config.add(i);
                        i++;
                        break;
                }

                installationConfigs.add(config);
            }
        }

        public List<Integer> getConfigForMeter(int meterNumber) {
            for (List<Integer> config : installationConfigs) {
                if (config.contains(meterNumber)) {
                    return config;
                }
            }
            return new ArrayList<>();
        }
    }
}