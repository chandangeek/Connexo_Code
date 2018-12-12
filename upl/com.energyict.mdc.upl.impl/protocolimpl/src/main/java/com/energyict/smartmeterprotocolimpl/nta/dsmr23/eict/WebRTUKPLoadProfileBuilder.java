package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.upl.LoadProfileConfigurationException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The WebRTU is misusing the registers in the LoadProfile. They don' use the ones that
 * are defined by DSMR, but created others to get more precision in the data.
 * By doing this, we actually need to create a different LoadProfileType in EIServer.
 * But they want to prevent this so we make a hack in our protocol.
 * If we have CapturedObject definitions with a B-field set to <i>128</i>,
 * then we map it to 0
 * <p/>
 * Copyrights EnergyICT
 * Date: 30/11/12
 * Time: 12:08
 */
public class WebRTUKPLoadProfileBuilder extends LoadProfileBuilder {

    public static final ObisCode MBUS_OBISCODE = ObisCode.fromString("0.x.24.2.1.255");

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public WebRTUKPLoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    /**
     * Create a list of <CODE>Registers</CODE> from all the dataChannels from all the receive CapturedObject list from all the expected LoadProfiles
     *
     * @param ccoLpConfigs provides the captured objects from the meters
     * @return a list of Registers
     * @throws java.io.IOException if an error occurred during dataFetching or -Parsing
     */
    protected List<CapturedRegisterObject> createCapturedObjectRegisterList(ComposedCosemObject ccoLpConfigs) throws IOException {
        List<CapturedRegisterObject> channelRegisters = new ArrayList<CapturedRegisterObject>();
        if (getExpectedLoadProfileReaders() != null) {
            for (LoadProfileReader lpr : getExpectedLoadProfileReaders()) {
                ComposedProfileConfig cpc = getLpConfigMap().get(lpr);
                if (cpc != null) {
                    // Fetch the raw captured object list from the device
                    DLMSAttribute colAttribute = cpc.getLoadProfileCapturedObjects();
                    byte[] rawCapturedObjectList = ccoLpConfigs.getAttribute(colAttribute).getBEREncodedByteArray();

                    // Store the data in a new data container, so it can be used further on
                    DataContainer dc = new DataContainer();
                    dc.parseObjectList(rawCapturedObjectList, getMeterProtocol().getLogger());

                    // Use a dummy profile generic object to get a list of CapturedObjects from the previously created data container
                    ProfileGeneric pg = new ProfileGeneric(getMeterProtocol().getDlmsSession(), null);
                    List<CapturedObject> capturedObjects = pg.getCapturedObjectsFromDataContainter(dc);

                    // Convert each captured object to a register (DLMSAttribute + device serial number)
                    List<CapturedRegisterObject> coRegisters = new ArrayList<CapturedRegisterObject>();
                    for (CapturedObject co : capturedObjects) {
                        String deviceSerialNumber = getMeterProtocol().getSerialNumberFromCorrectObisCode(co.getLogicalName().getObisCode());
                        DLMSAttribute dlmsAttribute = new DLMSAttribute(getCorrectedChannelInfoObisCode(co), co.getAttributeIndex(), co.getClassId());
                        CapturedRegisterObject reg = new CapturedRegisterObject(dlmsAttribute, deviceSerialNumber);

                        // Prepare each register only once. This way we don't get duplicate registerRequests in one getWithList
                        if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode(), reg.getSerialNumber())) {
                            channelRegisters.add(reg);
                        }
                        coRegisters.add(reg); // we always add it to the list of registers for this CapturedObject
                    }
                    getCapturedObjectRegisterListMap().put(lpr, coRegisters);
                } //TODO should we log this if we didn't get it???
            }
        } else {
            throw new LoadProfileConfigurationException("ExpectedLoadProfileReaders may not be null");
        }
        return channelRegisters;
    }

    private ObisCode getCorrectedChannelInfoObisCode(CapturedObject co) {
        ObisCode obisCode = co.getLogicalName().getObisCode();
        if (obisCode.getB() == 128) {
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0x00);
        }
        return obisCode;
    }
}