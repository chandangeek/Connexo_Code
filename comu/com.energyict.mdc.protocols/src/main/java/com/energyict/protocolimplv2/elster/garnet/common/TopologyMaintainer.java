package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.support.DeviceTopologySupport;

import com.energyict.protocolimplv2.elster.garnet.GarnetConcentrator;
import com.energyict.protocolimplv2.elster.garnet.GarnetProperties;
import com.energyict.protocolimplv2.elster.garnet.RequestFactory;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.exception.NotExecutedException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.NotExecutedError;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.SerialNumberDeviceIdentifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 19/06/2014 - 15:17
 */
public class TopologyMaintainer implements DeviceTopologySupport {
    private static final String INVALID_METER_SERIAL = "vazio";

    private final GarnetConcentrator deviceProtocol;
    private final IssueService issueService;

    public TopologyMaintainer(GarnetConcentrator deviceProtocol, IssueService issueService) {
        this.deviceProtocol = deviceProtocol;
        this.issueService = issueService;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        DeviceIdentifier deviceIdentifierOfMaster = new DeviceIdentifierById(getMasterDevice().getId());
        CollectedTopology collectedTopology = CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedTopology(deviceIdentifierOfMaster);
        try {
            List<DeviceIdentifier> slaveMeters = readListOfSlaveDevices();
            for (DeviceIdentifier slave : slaveMeters) {
                collectedTopology.addSlaveDevice(slave);
            }
        } catch (NotExecutedException e) {
            if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.COMMAND_NOT_IMPLEMENTED)) {
                collectedTopology.setFailureInformation(ResultType.NotSupported, this.issueService.newWarning(getMasterDevice(), "commandNotSupported"));
            } else if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.SLAVE_DOES_NOT_EXIST)) {
                collectedTopology.setFailureInformation(ResultType.ConfigurationMisMatch, this.issueService.newWarning(getMasterDevice(), "topologyMismatch"));
            } else {
                collectedTopology.setFailureInformation(ResultType.InCompatible, this.issueService.newProblem(getMasterDevice(), "CouldNotParseTopologyData"));
            }
        } catch (GarnetException e) {
            collectedTopology.setFailureInformation(ResultType.InCompatible, this.issueService.newProblem(getMasterDevice(), "CouldNotParseTopologyData"));
        }
        return collectedTopology;
    }

    private List<DeviceIdentifier> readListOfSlaveDevices() throws GarnetException {
        List<DeviceIdentifier> slaveMeters = new ArrayList<>();
        try {
            List<MeterSerialNumber> serialsOfEMeterSlaves = getRequestFactory().discoverMeters().getMeterSerialNumberCollection();
            for (MeterSerialNumber serialOfSlave : serialsOfEMeterSlaves) {
                if (!serialOfSlave.getSerialNumber().toLowerCase().equals(INVALID_METER_SERIAL)) {
                    slaveMeters.add(new SerialNumberDeviceIdentifier(serialOfSlave.getSerialNumber()));
                }
            }
        } catch(NotExecutedException e) {
            // When executing this command for a UCR concentrator (who has no direct E-meter salves under it),
            // then a 'Command not implemented' exception is returned
            if (!e.getMessage().contains(NotExecutedError.ErrorCode.COMMAND_NOT_IMPLEMENTED.getErrorDescription())) {
                throw e;
            }
        }
        return slaveMeters;
    }

    public int getDeviceIdOfMaster() {
        return getMasterDevice().getAllProperties().<BigDecimal>getTypedProperty(GarnetProperties.DEVICE_ID).intValue();
    }

    public GarnetConcentrator getDeviceProtocol() {
        return deviceProtocol;
    }

    public RequestFactory getRequestFactory() {
        return getDeviceProtocol().getRequestFactory();
    }

    /**
     * Getter for the OfflineDevice of the master
     */
    public OfflineDevice getMasterDevice() {
        return getDeviceProtocol().getOfflineDevice();
    }
}