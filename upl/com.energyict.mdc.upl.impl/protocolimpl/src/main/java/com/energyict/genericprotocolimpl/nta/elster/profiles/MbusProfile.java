package com.energyict.genericprotocolimpl.nta.elster.profiles;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.nta.elster.MbusDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.Calendar;

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
                        cal.add(Calendar.SECOND, mbusDevice.getMbus().getIntervalInSeconds());
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
    protected boolean isProfileStatusObisCode(final ObisCode oc){
        return getChannelCorrectedObiscode(OBISCODE_STATUS).equals(oc);
    }

    /**
     * Change the default obisCode the the Mbus 'channeled' obiscode
     * @param oc - the obisCode to convert
     *
     * @return the converted obisCode
     */
    private ObisCode getChannelCorrectedObiscode(ObisCode oc){
        oc = new ObisCode(oc.getA(), mbusDevice.getPhysicalAddress() + 1, oc.getC(), oc.getD(), oc.getE(), oc.getF());
        return oc;
    }
}
