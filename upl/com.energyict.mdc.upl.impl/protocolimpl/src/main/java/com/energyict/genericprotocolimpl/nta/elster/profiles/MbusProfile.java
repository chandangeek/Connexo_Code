package com.energyict.genericprotocolimpl.nta.elster.profiles;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.nta.abstractnta.MbusObisCodeProvider;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTAObisCodeProvider;
import com.energyict.genericprotocolimpl.nta.elster.MbusDevice;
import com.energyict.genericprotocolimpl.nta.elster.obiscodeproviders.OMSGasObisCodeProvider;
import com.energyict.genericprotocolimpl.nta.elster.obiscodeproviders.OMSWaterObisCodeProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.*;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 8-jun-2010
 * Time: 13:17:28
 * </p>
 */
public class MbusProfile extends com.energyict.genericprotocolimpl.nta.profiles.MbusProfile {

    /**
     * Static ObisCode for the Status
     */
    private final static ObisCode OBISCODE_STATUS = ObisCode.fromString("0.0.96.10.3.255");

    /**
     * Provides NTA ObisCodes related to Mbus Objects (gas and water)
     */
    private final NTAObisCodeProvider ntaObisCodeProvider = new NTAObisCodeProvider();
    /**
     * Provides OMS ObisCodes related to Gas Objects
     */
    private final OMSGasObisCodeProvider omsGasObisCodeProvider = new OMSGasObisCodeProvider();
    /**
     * Provides OMS ObisCodes related to Water Objects
     */
    private final OMSWaterObisCodeProvider omsWaterObisCodeProvider = new OMSWaterObisCodeProvider();

    /**
     * Constructor
     */
    public MbusProfile(MbusDevice mbusDevice) {
        super(mbusDevice);
    }

    @Override
    protected void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg) throws IOException {

        Calendar cal = null;
        IntervalData currentInterval = null;
        int profileStatus = 0;
        if (dc.getRoot().getElements().length != 0) {

            for (int i = 0; i < dc.getRoot().getElements().length; i++) {
                if (dc.getRoot().getStructure(i).isOctetString(0)) {
                    cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(mbusDevice.getWebRTU().getTimeZone());
                } else {
                    if (cal != null) {
                        cal.add(Calendar.SECOND, pg.getCapturePeriod());
                    }
                }
                if (cal != null) {

                    if (getProfileStatusChannelIndex(pg) != -1) {
                        profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
                    } else {
                        profileStatus = 0;
                    }

                    currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
                    if (currentInterval != null) {
                        pd.addInterval(currentInterval);
                    }
                }
            }
        } else {
            mbusDevice.getLogger().info("No entries in MbusLoadProfile");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getProfileStatusChannelIndex(final ProfileGeneric pg) throws IOException {
        try {
            for (int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++) {
                if (isProfileStatusObisCode(((CapturedObject) (pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())) {
                    return i;
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IOException("Could not retrieve the index of the profileData's status attribute.");
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isProfileStatusObisCode(final ObisCode oc) {
        return getChannelCorrectedObiscode(OBISCODE_STATUS).equals(oc);
    }

    /**
     * Check all the ObisCodeProviders for possible channels in the ProfileObject
     *
     * @param oc the ObisCode to check
     * @return true if it is a possible MbusValue Obiscode
     */
    @Override
    protected boolean isMbusRegisterObisCode(ObisCode oc) {
        return isNTARegisterObisCode(oc) || isOMSGasRegisterObisCode(oc) || isOMSWaterRegisterObisCode(oc);
    }

    /**
     * Check if the given obisCode matches the NTA spec
     *
     * @param oc the obisCode to check
     * @return true if it is an NTA register obisCode, false otherwise
     */
    private boolean isNTARegisterObisCode(ObisCode oc) {
        return this.ntaObisCodeProvider.getMasterRegisterTotal(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.ntaObisCodeProvider.getMasterRegisterValue1(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.ntaObisCodeProvider.getMasterRegisterValue2(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.ntaObisCodeProvider.getMasterRegisterValue3(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.ntaObisCodeProvider.getMasterRegisterValue4(this.mbusDevice.getPhysicalAddress()).equals(oc);
    }

    /**
     * Check if the given obisCode matches the OMS spec for gas
     *
     * @param oc the obisCode to check
     * @return true if the obisCode is an OMS gas register, false otherwise
     */
    private boolean isOMSGasRegisterObisCode(ObisCode oc) {
        return this.omsGasObisCodeProvider.getMasterRegisterTotal(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsGasObisCodeProvider.getMasterRegisterValue1(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsGasObisCodeProvider.getMasterRegisterValue2(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsGasObisCodeProvider.getMasterRegisterValue3(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsGasObisCodeProvider.getMasterRegisterValue4(this.mbusDevice.getPhysicalAddress()).equals(oc);
    }

    /**
     * Check if the given obisCode matches the OMS spec for water
     *
     * @param oc the obisCode to check
     * @return true if the obisCode is an OM water register, false otherwise
     */
    private boolean isOMSWaterRegisterObisCode(ObisCode oc) {
        return this.omsWaterObisCodeProvider.getMasterRegisterTotal(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsWaterObisCodeProvider.getMasterRegisterValue1(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsWaterObisCodeProvider.getMasterRegisterValue2(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsWaterObisCodeProvider.getMasterRegisterValue3(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.omsWaterObisCodeProvider.getMasterRegisterValue4(this.mbusDevice.getPhysicalAddress()).equals(oc);
    }

    /**
     * Change the default obisCode the the Mbus 'channeled' obiscode
     *
     * @param oc - the obisCode to convert
     * @return the converted obisCode
     */
    private ObisCode getChannelCorrectedObiscode(ObisCode oc) {
        oc = new ObisCode(oc.getA(), mbusDevice.getPhysicalAddress() + 1, oc.getC(), oc.getD(), oc.getE(), oc.getF());
        return oc;
    }
}
