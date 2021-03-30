package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class should be used in order to read scaler and unit for a channel in a LB. Reading should be done from a mapping obis code.
 */
public class MappableUnitChannelReader extends AbstractChannelInfoReader {

    private Map<ObisCode, Unit> channelInfoMap;

    /**
     * Load profile information summary object, holding scaler and unit of each channel
     */
    private final ObisCode unitMapObisCode;

    public MappableUnitChannelReader(ObisCode unitMapObisCode) {
        this.unitMapObisCode = unitMapObisCode;
    }

    @Override
    protected Unit getUnit(AbstractDlmsProtocol protocol, ObisCode obisCode) throws IOException {
        read(protocol);
        return channelInfoMap.getOrDefault(obisCode, Unit.getUndefined());
    }

    private void read(AbstractDlmsProtocol protocol) throws IOException {
        // map should be read only once. This is kind of ugly we should split this class into 2 separate ones: one for reading and one for mapping...reading one can be an actual interface with one cache
        if (channelInfoMap != null) {
            return;
        }
        ProfileGeneric loadProfileInformation = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(unitMapObisCode);
        DataContainer buffer = loadProfileInformation.getBuffer();
        DataStructure dataStructure = buffer.getRoot().getStructure(0);

        int index = 0;
        channelInfoMap = new HashMap<>();
        while (dataStructure.isOctetString(index) && dataStructure.isStructure(index + 1)) {
            ObisCode obisCode = dataStructure.getOctetString(index).toObisCode();
            ScalerUnit scalerUnit = new ScalerUnit(dataStructure.getStructure(index + 1).getInteger(0),
                    dataStructure.getStructure(index + 1).getInteger(1));
            channelInfoMap.put(obisCode, scalerUnit.getEisUnit());
            index = index + 2;
        }
    }
}
