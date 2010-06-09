package com.energyict.genericprotocolimpl.nta.elster.profiles;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.nta.elster.AM100;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.Calendar;

/**
 * The electricityProfile is based on the profile NTA version.
 * Only the status-ObisCodes are not in the ObjectList so we needed to hardcoded them
 * <p/>
 * Copyrights EnergyICT
 * Date: 2-jun-2010
 * Time: 17:07:33
 */
public class ElectricityProfile extends com.energyict.genericprotocolimpl.nta.profiles.ElectricityProfile {

    /**
     * Static ObisCode for the Status
     */
    private final static ObisCode OBISCODE_STATUS = ObisCode.fromString("0.0.96.10.1.255");

    /**
     * Constructor with the subclass
     */
    public ElectricityProfile(AM100 am100) {
        super(am100);
    }

    /**
     * <b><u>Note:</u></b><br>
     * We override this method because the deviation specified in the returned byteArray contains a positive signed amount of minutes.
     * If we add that to the calculated time then we get an incorrect GMT time. The Iskra meter and the Kamstrup meter return a negative
     * signed amount of minutes in the deviation...
     *
     * @param dc the datacontainer constructed from the received byteArray
     * @param pd the {@link ProfileData} object to put in the data
     * @param pg the {@link com.energyict.dlms.cosem.ProfileGeneric} object that contains profile information
     * @throws IOException
     */
    @Override
    protected void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg) throws IOException {


        Calendar cal = null;
        IntervalData currentInterval = null;
        int profileStatus = 0;
        if (dc.getRoot().getElements().length != 0) {

            for (int i = 0; i < dc.getRoot().getElements().length; i++) {

                //Test
                if (dc.getRoot().getStructure(i) == null) {
                    dc.printDataContainer();
                    System.out.println("Element: " + i);
                }

                if (dc.getRoot().getStructure(i).isOctetString(0)) {
					cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(webrtu.getTimeZone());
                } else {
                    if (cal != null) {
                        cal.add(Calendar.SECOND, webrtu.getMeter().getIntervalInSeconds());
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
            webrtu.getLogger().info("No entries in LoadProfile");
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
    @Override
    protected boolean isProfileStatusObisCode(final ObisCode oc) throws IOException {
        return OBISCODE_STATUS.equals(oc);
    }

}
