package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.GasDevice;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jme
 *
 */
public class GProfileBuilder {

	private final AS220	as220;
	private final CapturedObjectsHelper coh;
	private static final long MASK_VALIDITY     = 0x80000000;
	private static final long MASK_ENCRYPTED    = 0x40000000;
    private static final long MASK_VALUE        = 0x3FFFFFFF;


	public GProfileBuilder(AS220 as220, CapturedObjectsHelper coh) {
		this.as220 = as220;
		this.coh = coh;
	}

	private AS220 getGasDevice() {
		return as220;
	}

	/**
	 * Builder for the scalerUnits.
	 * 
	 * Currently we hardcoded the scalerUnit as a Liter
	 * 
	 * @return a ScalerUnit array of 1 element, being Liter
	 * 
	 * @throws IOException
	 */
	public ScalerUnit[] buildScalerUnits() throws IOException {
		ScalerUnit[] scalerUnits = new ScalerUnit[coh.getNrOfchannels()];
		for (int i = 0; i < scalerUnits.length; i++) {
//	        ObisCode obisCode = coh.getProfileDataChannelObisCode(i);
//	        scalerUnits[i] = getGasDevice().getCosemObjectFactory().getGenericRead(getGasDevice().getCorrectedChannelObisCode(obisCode), (byte)0x08, 4).getScalerUnit();
	        scalerUnits[i] = new ScalerUnit(Unit.get(BaseUnit.LITER));

		}
		return scalerUnits;
	}

	/**
	 * Build a list of {@link ChannelInfo}s
	 *
	 * @param scalerunit
	 * 				- an array of ScalerUnits
	 *
	 * @return a list of ChannelInfos
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public List<ChannelInfo> buildChannelInfos(ScalerUnit[] scalerunit){
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		for (int i = 0; i < scalerunit.length; i++) {
			ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getGasDevice().getDeviceID() + "_channel_" + i, scalerunit[i].getEisUnit());
			
			// Setting the cumulative value is the old way of doing, Eandis has an old way of doing so ...
			channelInfo.setCumulativeWrapValue(new BigDecimal(100000000));
            // the setCumulative() is only from 8.5
			channelInfo.setCumulative();
			channelInfos.add(channelInfo);
		}
		return channelInfos;
	}

	/**
	 * Build a list of {@link IntervalData}
	 *
	 * @param scalerunit
	 * 				- an array of ScalerUnits
	 * @param dc
	 * 				- a {@link DataContainer} with the profileData from the device
	 *
	 * @return a list a IntervalData
	 * @throws IOException
	 */
	public List<IntervalData> buildIntervalData(ScalerUnit[] scalerunit, DataContainer dc ) throws IOException {
        int capturePeriod = getGasDevice().getgMeter().getMbusProfile().getCapturePeriod();

		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		Calendar cal = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){

			for(int i = 0; i < dc.getRoot().getElements().length; i++){

                if(dc.getRoot().getStructure(i) != null){
                    if(dc.getRoot().getStructure(i).isOctetString(0)){	// it is a date
                        cal = new DateTime(OctetString.fromByteArray(dc.getRoot().getStructure(i).getOctetString(0).getArray()), getGasDevice().getTimeZone()).getValue();
                    } else {
                        if(cal != null){
                            cal.add(Calendar.SECOND, capturePeriod);
                        }
                    }

                    if(cal != null){

                        profileStatus = GasStatusCodes.intervalStateBits(dc.getRoot().getStructure(i).getInteger(1));

                        int value = dc.getRoot().getStructure(i).getInteger(2);

                        if (isBitSet(value, MASK_VALIDITY)) {
                            profileStatus = IntervalStateBits.MISSING;
                        }
                        if (!isBitSet(value, MASK_ENCRYPTED)) {
                            profileStatus |= IntervalStateBits.CORRUPTED;
                        }

                        value &= MASK_VALUE;

                        if (ProtocolTools.isCorrectIntervalBoundary(cal, capturePeriod)) {
                            IntervalData id = new IntervalData(cal.getTime(), profileStatus);
                            id.addValue(value);
                            intervalDatas.add(id);
                        } else {
                            getGasDevice().getLogger().severe("Removing interval from profile data [" + cal.getTime() + " = " + value + "]. Not on interval boundary.");
                        }
                    }

                } else {
                    getGasDevice().getLogger().info("GasProfile contained a 'NULL' structure.");
                }

			}
		} else {
			getGasDevice().getLogger().info("No entries in LoadProfile");
		}
		return intervalDatas;
	}

    private boolean isBitSet(int value, long mask) {
        return (value & mask) == mask;
    }

    public static void main(String args[]){
		byte[] b = new byte[] { 1, 45, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 9, 28, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6,
				-128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 11, 22, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0,
				0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 12, 40, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2,
				3, 9, 12, 7, -38, 2, 17, 4, 15, 13, 33, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12,
				7, -38, 2, 17, 4, 15, 14, 8, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2,
				17, 4, 15, 14, 33, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4,
				15, 14, 49, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 14,
				59, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 20, 1, 0,
				-128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 21, 38, 0, -128, 0,
				0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -116, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 25, 32, 0, -128, 0, 0, 6, 0,
				0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 27, 7, 0, -128, 0, 0, 6, 0, 0, -64, 0,
				6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 28, 8, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0,
				0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 28, 50, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2,
				3, 9, 12, 7, -38, 2, 17, 4, 15, 29, 21, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12,
				7, -38, 2, 17, 4, 15, 29, 41, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2,
				17, 4, 15, 29, 53, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15,
				35, 0, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 36, 39, 0,
				-128, 0, 0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -116, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 40, 33, 0, -128, 0,
				0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 42, 8, 0, -128, 0, 0, 6, 0, 0,
				-64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 43, 11, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6,
				-128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 43, 53, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0,
				0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 44, 21, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2,
				3, 9, 12, 7, -38, 2, 17, 4, 15, 44, 42, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12,
				7, -38, 2, 17, 4, 15, 44, 56, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2,
				17, 4, 15, 52, 3, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15,
				54, 44, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 56, 33, 0,
				-128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 57, 48, 0, -128, 0,
				0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 58, 38, 0, -128, 0, 0, 6, 0,
				0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 59, 13, 0, -128, 0, 0, 6, 0, 0, -64, 0,
				6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 59, 35, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128,
				0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 59, 50, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0,
				2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 3, 5, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -116, 2, 3, 9, 12,
				7, -38, 2, 17, 4, 16, 8, 16, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2,
				17, 4, 16, 10, 33, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16,
				12, 6, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 13, 10, 0,
				-128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 13, 52, 0, -128, 0,
				0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 14, 21, 0, -128, 0, 0, 6, 0,
				0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 14, 42, 0, -128, 0, 0, 6, 0, 0, -64, 0,
				6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 14, 55, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128,
				0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 18, 5, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -106, 2,
				3, 9, 12, 7, -38, 2, 17, 4, 16, 23, 17, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0 };

		try {
			GasDevice gDevice = new GasDevice();
			gDevice.init(null, null, TimeZone.getTimeZone("GMT"), Logger.getAnonymousLogger());
			
			GProfileBuilder builder = new GProfileBuilder(gDevice, null);
			int bit = GasStatusCodes.intervalStateBits(49407);
			Logger.getAnonymousLogger().log(Level.INFO, String.valueOf(bit));

			DataContainer dc = new DataContainer();
			dc.parseObjectList(b, Logger.getAnonymousLogger());
			builder.buildIntervalData(null, dc);

		} catch (IOException e) {
            //Absorb exception
		}
	}

}
