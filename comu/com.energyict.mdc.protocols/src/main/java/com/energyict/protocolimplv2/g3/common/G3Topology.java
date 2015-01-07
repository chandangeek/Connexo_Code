package com.energyict.protocolimplv2.g3.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Will serve functionality for G3 topology related actions
 */
public class G3Topology {

    private final DeviceIdentifier masterDeviceIdentifier;
    private final IdentificationService identificationService;
    private final IssueService issueService;
    private final PropertySpecService propertySpecService;
    private final DlmsSession dlmsSession;
    private final G3Properties g3Properties;
    private final CollectedDataFactory collectedDataFactory;
    private CollectedTopology collectedTopology;

    public G3Topology(DeviceIdentifier masterDeviceIdentifier, IdentificationService identificationService, IssueService issueService, PropertySpecService propertySpecService, DlmsSession dlmsSession, G3Properties g3Properties, CollectedDataFactory collectedDataFactory) {
        this.masterDeviceIdentifier = masterDeviceIdentifier;
        this.identificationService = identificationService;
        this.issueService = issueService;
        this.propertySpecService = propertySpecService;
        this.dlmsSession = dlmsSession;
        this.g3Properties = g3Properties;
        this.collectedDataFactory = collectedDataFactory;
    }

    public CollectedTopology collectTopology() {
        CollectedTopology deviceTopology = getDeviceTopology();

        collectAndUpdateMacAddresses(deviceTopology);
        collectAndUpdateShortAddresses(deviceTopology);
        collectAndUpdatePathSegments(deviceTopology);
        collectAndUpdateNeighbours(deviceTopology);
        collectAndUpdateG3IdentificationInformation(deviceTopology);

        return deviceTopology;
    }

    private void collectAndUpdateG3IdentificationInformation(CollectedTopology deviceTopology) {
        try {
            Array unicastIPv6Addresses = getDlmsSession().getCosemObjectFactory().getIPv6Setup().getUnicastIPv6Addresses();
            System.out.println(unicastIPv6Addresses);
        } catch (IOException e) {
            deviceTopology.setFailureInformation(ResultType.Supported, issueService.newWarning("getIPv6Setup", e.getMessage()));
        }
    }

    private CollectedTopology getDeviceTopology() {
        if (collectedTopology == null) {
            collectedTopology = this.collectedDataFactory.createCollectedTopology(masterDeviceIdentifier);
        }
        return collectedTopology;
    }

    private void collectAndUpdateNeighbours(CollectedTopology deviceTopology) {
        try {
            Array neighbourTable = (Array) getDlmsSession().getCosemObjectFactory().getPLCOFDMType2MACSetup().readNeighbourTable();
            for (AbstractDataType neighbour : neighbourTable) {
                Structure structure = neighbour.getStructure();
                if (structure != null) {
                    //Skip empty elements
                    if (structure.getDataType(0).intValue() == 0xFFFF && structure.getDataType(1).intValue() == 0xFFFF) {
                        continue;
                    }

                    if (structure.nrOfDataTypes() == 11) {
                        deviceTopology.addTopologyNeighbour(
                                identificationService.createDeviceIdentifierByProperty(G3Properties.G3_SHORT_ADDRESS_PROP_NAME, Integer.toString(structure.getDataType(0).intValue())),
                                structure.getDataType(1).intValue(),
                                structure.getDataType(2).longValue(),
                                structure.getDataType(3).intValue(),
                                structure.getDataType(4).intValue(),
                                structure.getDataType(5).intValue(),
                                structure.getDataType(6).intValue(),
                                structure.getDataType(7).intValue(),
                                structure.getDataType(8).intValue(),
                                structure.getDataType(9).intValue(),
                                structure.getDataType(10).intValue()
                        );
                    }
                }
            }
        } catch (IOException e) {
            deviceTopology.setFailureInformation(ResultType.Supported, issueService.newWarning("getPLCOFDMType2MACSetup", e.getMessage()));
        }
    }

    private void collectAndUpdatePathSegments(CollectedTopology deviceTopology) {
        try {
            Array adpRoutingTable = getDlmsSession().getCosemObjectFactory().getSixLowPanAdaptationLayerSetup().readAdpRoutingTable();

            for (AbstractDataType abstractStructure : adpRoutingTable) {
                Structure structure = abstractStructure.getStructure();
                if (structure != null) {
                    //Skip empty elements
                    if (structure.getDataType(0).intValue() == 0xFFFF && structure.getDataType(1).intValue() == 0xFFFF) {
                        continue;
                    }

                    if (structure.nrOfDataTypes() == 6) {
                        deviceTopology.addPathSegmentFor(masterDeviceIdentifier,

                                identificationService.createDeviceIdentifierByProperty(G3Properties.G3_SHORT_ADDRESS_PROP_NAME, Integer.toString(structure.getDataType(0).intValue())),
                                identificationService.createDeviceIdentifierByProperty(G3Properties.G3_SHORT_ADDRESS_PROP_NAME, Integer.toString(structure.getDataType(1).intValue())),
                                Duration.ofSeconds(structure.getDataType(5).intValue()),
                                structure.getDataType(2).intValue());
                    }
                }
            }
        } catch (IOException e) {
            deviceTopology.setFailureInformation(ResultType.Supported, issueService.newWarning("SixLowPanAdaptationLayerSetup", e.getMessage()));
        }
    }

    private void collectAndUpdateShortAddresses(CollectedTopology deviceTopology) {
        try {
            Array nodeList = this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement().getNodeList();
            for (AbstractDataType abstractDataType : nodeList) {
                if (abstractDataType != null && abstractDataType instanceof Structure) {
                    DeviceIdentifier nodeIdentifier = getSlaveMacAddressPropertyIdentifier(ProtocolTools.getHexStringFromBytes(((Structure) abstractDataType).getDataType(0).getOctetString().getOctetStr()));
                    deviceTopology.addAdditionalCollectedDeviceInfo(
                            this.collectedDataFactory.createCollectedDeviceProtocolProperty(
                                    nodeIdentifier,
                                    getSlaveShortAddressPropertySpec(),
                                    getSlaveShortAddressPropertySpec().getValueFactory().fromStringValue(String.valueOf(((Structure) abstractDataType).getDataType(2).getInteger32().intValue()))));
                }
            }
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    private void collectAndUpdateMacAddresses(CollectedTopology deviceTopology) {
        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
        for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {
            if (!isGatewayNode(sapAssignmentItem)) {
                DeviceIdentifier slaveDeviceIdentifier = getSlaveMacAddressPropertyIdentifier(sapAssignmentItem.getLogicalDeviceName());
                deviceTopology.addSlaveDevice(slaveDeviceIdentifier);
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        this.collectedDataFactory.createCollectedDeviceProtocolProperty(
                                slaveDeviceIdentifier,
                                getSlaveLogicalDeviceIdPropertySpec(),
                                getSlaveLogicalDeviceIdPropertySpec().getValueFactory().fromStringValue(String.valueOf(sapAssignmentItem.getSap()))));
            } else {
                deviceTopology.addAdditionalCollectedDeviceInfo(
                        this.collectedDataFactory.createCollectedDeviceProtocolProperty(
                                masterDeviceIdentifier,
                                this.g3Properties.getLogicalDeviceIdPropertySpec(),
                                BigDecimal.valueOf(sapAssignmentItem.getSap())));
            }
        }
    }

    private PropertySpec getSlaveShortAddressPropertySpec() {
        return this.g3Properties.getShortAddressPropertySpec();
    }

    private PropertySpec getSlaveLogicalDeviceIdPropertySpec() {
        return this.g3Properties.getLogicalDeviceIdPropertySpec();
    }

    private DeviceIdentifier getSlaveMacAddressPropertyIdentifier(String macAddress) {
        return this.identificationService.createDeviceIdentifierByProperty(G3Properties.G3_MAC_ADDRESS_PROP_NAME, macAddress);
    }

    private DlmsSession getDlmsSession() {
        return this.dlmsSession;
    }

    private boolean isGatewayNode(SAPAssignmentItem sapAssignmentItem) {
        return sapAssignmentItem.getSap() == 1;
    }

    public CollectedTopology doPathRequestFor(String macAddress) {
        CollectedTopology deviceTopology = getDeviceTopology();
        try {
            Array pathRequestArray = getDlmsSession().getCosemObjectFactory().getG3NetworkManagement().getPathRequestArray(macAddress);

            if (pathRequestArray.nrOfDataTypes() != 2) {
                throw new IOException("Expected the response to the path request method to be a structure with 2 array elements");
            }

            Array forwardPath = pathRequestArray.getDataType(0).getArray();
            Array reversePath = pathRequestArray.getDataType(1).getArray();

            if (forwardPath != null) {
                for (AbstractDataType forwardPathMacAddress : forwardPath) {
                    deviceTopology.addPathSegmentFor(masterDeviceIdentifier,
                            getSlaveMacAddressPropertyIdentifier(macAddress),
                            getSlaveMacAddressPropertyIdentifier(DLMSUtils.getHexStringFromBytes(((OctetString) forwardPathMacAddress).getOctetStr(), "")),
                            Duration.ofSeconds(0),
                            0);
                }
            }

            if (reversePath != null) {
                for (AbstractDataType reversePathMacAddress : reversePath) {
                    deviceTopology.addPathSegmentFor(
                            getSlaveMacAddressPropertyIdentifier(macAddress),
                            masterDeviceIdentifier,
                            getSlaveMacAddressPropertyIdentifier(DLMSUtils.getHexStringFromBytes(((OctetString) reversePathMacAddress).getOctetStr(), "")),
                            Duration.ofSeconds(0),
                            0);
                }
            }

        } catch (IOException e) {
            deviceTopology.setFailureInformation(ResultType.Supported, issueService.newWarning("getG3NetworkManagement", e.getMessage()));
        }
        return deviceTopology;
    }
}
